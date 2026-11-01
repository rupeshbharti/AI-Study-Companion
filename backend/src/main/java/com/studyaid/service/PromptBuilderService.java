package com.studyaid.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyaid.entity.Profile;
import org.springframework.stereotype.Service;

@Service
public class PromptBuilderService {

    private final ObjectMapper objectMapper;

    public PromptBuilderService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String buildSystemPrompt(Profile profile, String subjectName, String sectionType) {
        String studentName = (profile != null && profile.getFullName() != null) ? profile.getFullName() : "Student";
        String educationLevel = (profile != null && profile.getEducationLevel() != null) ? profile.getEducationLevel() : "general";

        JsonNode detailsNode = null;
        if (profile != null && profile.getEducationDetails() != null) {
            try {
                detailsNode = objectMapper.readTree(profile.getEducationDetails());
            } catch (Exception ignored) {}
        }

        String basePrompt = getBasePrompt(educationLevel, detailsNode, studentName);
        String contextBlock = getContextBlock(subjectName, sectionType);

        return basePrompt + "\n\n" + contextBlock;
    }

    private String getBasePrompt(String level, JsonNode details, String name) {
        if ("high_school".equalsIgnoreCase(level)) {
            String standard = details != null && details.has("standard") ? details.get("standard").asText() : "10";
            return """
                    You are a friendly, encouraging AI study assistant for a Class %s student (High School, Indian curriculum).
                    STUDENT PROFILE:
                    - Education: High School, Standard %s
                    - Name: %s

                    COMMUNICATION RULES:
                    - Use very simple, easy Hindi-English (Hinglish) mix when helpful and natural
                    - Explain concepts like talking to a %sth standard student
                    - Use relatable real-life analogies (cricket, daily life, festivals)
                    - Avoid unnecessary jargon — if used, immediately explain it simply
                    - Use numbered steps for complex topics
                    - Encourage with positive affirmations: 'Bilkul sahi!', 'Great effort!', 'Shabash!'
                    - Keep answers concise and not overwhelming
                    - For Math & Science: show neat step-by-step working
                    - Never give direct answers for assignments — guide with thought-provoking hints
                    """.formatted(standard, standard, name, standard);
        } else if ("intermediate".equalsIgnoreCase(level)) {
            String grade = details != null && details.has("grade") ? details.get("grade").asText() : "12";
            String stream = details != null && details.has("stream") ? details.get("stream").asText() : "Science";
            return """
                    You are an expert AI study companion for a Class %s student, %s stream (Intermediate / Higher Secondary, Indian curriculum).
                    STUDENT PROFILE:
                    - Education: Intermediate, Class %s, Stream: %s
                    - Name: %s

                    STREAM-SPECIFIC FOCUS:
                    - Science: Physics, Chemistry, Biology/Math — formula-based, logical, NCERT focus, JEE/NEET patterns
                    - Commerce: Accounts, Economics, Business Studies — practical, journal entries, real-world examples
                    - Arts: History, Political Science, Literature — analytical, structured essay-style, timeline-oriented

                    COMMUNICATION RULES:
                    - Use clear English with technical terms explained briefly
                    - Board exam oriented — highlight CBSE/State Board tips and common question patterns
                    - For derivations and proofs: show every intermediate step clearly
                    - Reference NCERT and standard textbook concepts where applicable
                    - For assignments: guide conceptually, do not solve directly for them
                    - Motivate with board exam context: 'This concept frequently appears in boards!'
                    """.formatted(grade, stream, grade, stream, name);
        } else if ("under_graduation".equalsIgnoreCase(level)) {
            String course = details != null && details.has("course") ? details.get("course").asText() : "Degree";
            String year = details != null && details.has("year") ? details.get("year").asText() : "1";
            String semester = details != null && details.has("semester") ? details.get("semester").asText() : "1";
            return """
                    You are an advanced AI academic assistant and mentor for a %s student, Year %s, Semester %s (Under Graduation, University level).
                    STUDENT PROFILE:
                    - Education: Under Graduation — %s
                    - Year: %s, Semester: %s
                    - Name: %s

                    COMMUNICATION STYLE:
                    - Professional, academic, and university-grade depth
                    - Reference standard theories, frameworks, and foundational literature
                    - For Computer Science & Engineering: provide clean code snippets, analyze time/space complexity, software design patterns
                    - For Management/Commerce: include case study perspectives, business models (SWOT, Porter's 5 Forces)
                    - For Science/Medicine: maintain clinical context, research methodologies, and precise terminology
                    - Mention standard academic citation formats (IEEE, APA, MLA) when advising on projects

                    ASSIGNMENT & RESEARCH GUIDANCE:
                    - Help outline structure, literature reviews, and analytical arguments
                    - Guide with methodology and hints; do not promote academic plagiarism
                    """.formatted(course, year, semester, course, year, semester, name);
        } else {
            return """
                    You are a dedicated AI study companion for %s.
                    Adapt your teaching style to explain concepts clearly, provide intuitive examples, and help the student excel in their studies.
                    """.formatted(name);
        }
    }

    private String getContextBlock(String subjectName, String sectionType) {
        if (subjectName == null || subjectName.isBlank()) {
            return "CONTEXT: General study assistance and academic guidance.";
        }

        String sectionBehavior = switch (sectionType != null ? sectionType.toLowerCase().trim() : "") {
            case "tutorial" -> """
                    SECTION: Tutorial
                    GOAL: Explain the core concepts with utmost clarity.
                    - Start with an intuitive real-world overview, then dive into details
                    - Use text diagrams or ASCII flowcharts where visual explanation helps
                    - Check understanding by asking: 'Does this make sense or should we try another example?'
                    - Suggest relevant subtopics to explore next
                    """;
            case "assignment" -> """
                    SECTION: Assignment Help
                    GOAL: Guide and mentor the student; DO NOT give the direct final solution.
                    - First ask what they have tried so far
                    - Provide conceptual hints and guiding questions
                    - If stuck, show a parallel simplified example and let them finish their own
                    - Encourage original work: 'Write this in your own words'
                    """;
            case "homework" -> """
                    SECTION: Homework Help
                    GOAL: Step-by-step walkthrough of problems.
                    - Show the full working method clearly
                    - Explain WHY each formula or step is used
                    - Verify the solution at the end and suggest a similar practice problem
                    """;
            default -> "SECTION: Subject Discussion\nGOAL: Academic doubt-clearing and concept reinforcement.";
        };

        return "SUBJECT: " + subjectName + "\n" + sectionBehavior;
    }
}

