# AI Study Companion — Spring Boot Backend

An enterprise-ready, student-adaptive Java & Spring Boot backend powering the **AI Study Companion** application.

## 🚀 Features
- **Student-Adaptive AI**: Implements Google Gemini streaming integration via Server-Sent Events (SSE) with context-aware system prompts dynamically crafted from student profile (High School, Intermediate Science/Commerce/Arts, Under Graduation Engineering/Management/Science) and section context (Tutorial, Assignment, Homework).
- **Stateless JWT Security**: Spring Security 6 with token-based authentication and BCrypt password encryption.
- **Relational Persistence**: Spring Data JPA with entities for Users, Profiles, Subjects, Chat Sessions, Chat Messages, and Study Materials.
- **Dual-Database Support**: Runs locally on zero-setup **H2 Database** with persistence, and switches seamlessly to hosted **Supabase PostgreSQL** via Spring profiles.
- **File Management**: Local file storage service for user avatars and study material uploads (PDFs, DOCX, images) with inline streaming and download.
- **Interactive Documentation**: OpenAPI 3 / Swagger UI enabled out-of-the-box.

---

## 🛠️ Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.3.5
- **ORM / Database**: Hibernate 6 / Spring Data JPA + H2 / PostgreSQL
- **Security**: Spring Security + JJWT 0.12.6
- **AI Integration**: Google Gemini API via Java 17 HTTP Client & Server-Sent Events (SSE)
- **Documentation**: Springdoc OpenAPI 2.6.0

---

## 🏃 Getting Started

### 1. Run with Maven Wrapper (Local Profile - H2 Database)
```bash
./mvnw spring-boot:run
```
Or on Windows:
```cmd
.\mvnw.cmd spring-boot:run
```

Once running:
- **API Base URL**: `http://localhost:8080/api`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **H2 Web Console**: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:file:./data/studyaiddb`
  - User Name: `sa`
  - Password: *(leave empty)*

---

### 2. Switch to Supabase PostgreSQL Database
When you are ready to connect to your hosted Supabase PostgreSQL database:

1. Open `src/main/resources/application.yml` and set active profile:
   ```yaml
   spring:
     profiles:
       active: supabase
   ```
2. In `src/main/resources/application-supabase.yml` (or via environment variables):
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://db.<PROJECT_REF>.supabase.co:5432/postgres?sslmode=require
       username: postgres
       password: <YOUR_SUPABASE_DB_PASSWORD>
   ```

---

### 3. Configure Gemini AI API Key
Set your Gemini API key in `application.yml` or as an environment variable:
```bash
export GEMINI_API_KEY=AIzaSy...
```
*(If no API key is provided, the backend returns an educational simulated response with the exact adaptive prompt that was generated).*

---

## 📡 REST API Reference

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register student user |
| `POST` | `/api/auth/login` | Login and receive JWT token |
| `GET` | `/api/auth/me` | Check authenticated user & profile |
| `GET` | `/api/profile` | Get student profile & education details |
| `PUT` | `/api/profile` | Update education tier & details |
| `POST` | `/api/profile/avatar` | Upload profile image (multipart) |
| `GET` | `/api/subjects` | List subjects |
| `POST` | `/api/subjects` | Add new subject |
| `PUT` | `/api/subjects/{id}` | Update subject |
| `DELETE` | `/api/subjects/{id}` | Delete subject |
| `GET` | `/api/chat/sessions` | List chat sessions |
| `POST` | `/api/chat/sessions` | Get or create chat session |
| `GET` | `/api/chat/sessions/{id}/messages` | Get session messages |
| `POST` | `/api/chat/sessions/{id}/messages` | Add message to session |
| `POST` | `/api/chat/stream` | Stream AI response via Server-Sent Events |
| `GET` | `/api/materials` | List study materials |
| `POST` | `/api/materials/upload` | Upload PDF / image / document |
| `DELETE` | `/api/materials/{id}` | Delete study material |
| `GET` | `/api/analytics/dashboard` | Dashboard metrics |
| `GET` | `/api/analytics/insights` | Detailed study insights |

