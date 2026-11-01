package com.studyaid.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.studyaid.dto.ChatDtos.AttachmentDto;
import com.studyaid.dto.ChatDtos.GeminiStreamRequest;
import com.studyaid.entity.ChatMessage;
import com.studyaid.entity.MessageRole;
import com.studyaid.entity.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    private final String apiKey;
    private final String defaultModel;
    private final String baseUrl;
    private final PromptBuilderService promptBuilder;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GeminiService(
            @Value("${app.gemini.api-key:}") String apiKey,
            @Value("${app.gemini.model:gemini-2.5-flash}") String defaultModel,
            @Value("${app.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl,
            PromptBuilderService promptBuilder,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.defaultModel = defaultModel;
        this.baseUrl = baseUrl;
        this.promptBuilder = promptBuilder;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public void streamGeminiResponse(
            Profile profile,
            GeminiStreamRequest request,
            List<ChatMessage> history,
            ResponseBodyEmitter emitter,
            java.util.function.Consumer<String> onCompletion) {

        CompletableFuture.runAsync(() -> {
            StringBuilder fullResponseAccumulator = new StringBuilder();
            try {
                String systemPrompt = promptBuilder.buildSystemPrompt(
                        profile, request.subjectName(), request.sectionType());

                if (apiKey.isBlank()) {
                    // Helpful graceful response when API key is not yet set
                    String simulated = "Hello " + (profile != null ? profile.getFullName() : "Student") + "! 🎓\n\n"
                            + "Your Spring Boot backend successfully received your query: **\"" + request.message() + "\"**\n\n"
                            + "### Adaptive System Prompt Generated:\n"
                            + "```\n" + systemPrompt + "\n```\n\n"
                            + "*To enable live Gemini streaming, provide your `GEMINI_API_KEY` in `application.yml` or as an environment variable.*";

                    for (String word : simulated.split(" ")) {
                        emitter.send(word + " ");
                        fullResponseAccumulator.append(word).append(" ");
                        Thread.sleep(30);
                    }
                    emitter.complete();
                    if (onCompletion != null) onCompletion.accept(fullResponseAccumulator.toString());
                    return;
                }

                String model = (request.model() != null && !request.model().isBlank())
                        ? request.model() : defaultModel;

                String requestJson = buildGeminiRequestBody(systemPrompt, history, request.message(), request.attachments());
                String endpointUrl = baseUrl + "/models/" + model + ":streamGenerateContent?alt=sse&key=" + apiKey;

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(endpointUrl))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofMinutes(2))
                        .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<InputStream> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() >= 400) {
                    try (InputStream errorStream = response.body()) {
                        String errorBody = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                        log.error("Gemini API error ({}): {}", response.statusCode(), errorBody);
                        emitter.send("Error calling Gemini API: " + errorBody);
                    }
                    emitter.complete();
                    return;
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String dataPayload = line.substring(6).trim();
                            if (dataPayload.isEmpty() || dataPayload.equals("[DONE]")) continue;

                            try {
                                JsonNode rootNode = objectMapper.readTree(dataPayload);
                                JsonNode candidates = rootNode.path("candidates");
                                if (candidates.isArray() && !candidates.isEmpty()) {
                                    JsonNode parts = candidates.get(0).path("content").path("parts");
                                    if (parts.isArray()) {
                                        for (JsonNode part : parts) {
                                            String text = part.path("text").asText("");
                                            if (!text.isEmpty()) {
                                                emitter.send(text);
                                                fullResponseAccumulator.append(text);
                                            }
                                        }
                                    }
                                }
                            } catch (Exception parseEx) {
                                log.warn("Failed to parse SSE JSON chunk: {}", parseEx.getMessage());
                            }
                        }
                    }
                }

                emitter.complete();
                if (onCompletion != null) {
                    onCompletion.accept(fullResponseAccumulator.toString());
                }

            } catch (Exception ex) {
                log.error("Error during Gemini stream processing", ex);
                try {
                    emitter.send("\n\n[Error streaming response: " + ex.getMessage() + "]");
                    emitter.complete();
                } catch (Exception ignored) {}
            }
        });
    }

    private String buildGeminiRequestBody(
            String systemPrompt,
            List<ChatMessage> history,
            String currentMessage,
            List<AttachmentDto> attachments) {

        ObjectNode root = objectMapper.createObjectNode();

        // 1. System instruction
        ObjectNode systemInstruction = objectMapper.createObjectNode();
        ArrayNode systemParts = systemInstruction.putArray("parts");
        systemParts.addObject().put("text", systemPrompt);
        root.set("system_instruction", systemInstruction);

        // 2. Contents (Chat history + current message)
        ArrayNode contents = root.putArray("contents");

        if (history != null) {
            for (ChatMessage msg : history) {
                ObjectNode messageNode = contents.addObject();
                messageNode.put("role", msg.getRole() == MessageRole.USER ? "user" : "model");
                ArrayNode parts = messageNode.putArray("parts");
                parts.addObject().put("text", msg.getContent());
            }
        }

        // Current user message
        ObjectNode currentUserMessage = contents.addObject();
        currentUserMessage.put("role", "user");
        ArrayNode currentParts = currentUserMessage.putArray("parts");

        // Attachments
        if (attachments != null) {
            for (AttachmentDto att : attachments) {
                if ("image".equalsIgnoreCase(att.type()) && att.base64() != null && !att.base64().isBlank()) {
                    ObjectNode inlineData = currentParts.addObject().putObject("inline_data");
                    inlineData.put("mime_type", att.mimeType() != null ? att.mimeType() : "image/jpeg");
                    inlineData.put("data", att.base64());
                } else if ("document".equalsIgnoreCase(att.type()) && att.extractedText() != null) {
                    currentParts.addObject().put("text", "[DOCUMENT CONTENT: " + (att.name() != null ? att.name() : "file") + "]:\n" + att.extractedText());
                }
            }
        }

        // User message text
        currentParts.addObject().put("text", currentMessage != null ? currentMessage : "");

        // 3. Safety settings
        ArrayNode safetySettings = root.putArray("safety_settings");
        addSafetySetting(safetySettings, "HARM_CATEGORY_HARASSMENT", "BLOCK_MEDIUM_AND_ABOVE");
        addSafetySetting(safetySettings, "HARM_CATEGORY_HATE_SPEECH", "BLOCK_MEDIUM_AND_ABOVE");
        addSafetySetting(safetySettings, "HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_LOW_AND_ABOVE");
        addSafetySetting(safetySettings, "HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_MEDIUM_AND_ABOVE");

        return root.toString();
    }

    private void addSafetySetting(ArrayNode array, String category, String threshold) {
        ObjectNode setting = array.addObject();
        setting.put("category", category);
        setting.put("threshold", threshold);
    }
}

