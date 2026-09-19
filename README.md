# Placement Application Tracker (Java / Spring Boot)

Browser-based CRUD app where a student tracks job/internship applications:
company, role, status, applied date, job URL, notes, creation date.

Built from scratch in Java — no Python/Flask code reused.

## Technology stack (and why)

| Tech | Role |
|---|---|
| Java 17 | Language |
| Spring Boot 3.2.5 | Auto-configures Tomcat, MVC, JPA, Thymeleaf |
| Spring MVC (`@Controller`) | Browser routes (`GET`/`POST`), form binding |
| Thymeleaf | Dynamic HTML (`th:each`, `th:object`, `th:field`, `th:errors`) |
| Spring Data JPA + Hibernate | `JpaRepository` (`save`/`findAll`/`findById`/`deleteById`), no SQL |
| SQLite (`sqlite-jdbc` + `hibernate-community-dialects`) | Local file DB, persists after restart |
| HTML + CSS only | Simple UI, no frameworks |
| Maven | Build + dependencies (`pom.xml`) |
| JUnit 5 + Mockito | Isolated tests (mocks, real DB never touched) |

## Project structure

```text
placement-tracker/
├── pom.xml
├── placement_tracker.db            # auto-created on first run
├── src/main/java/com/example/placementtracker/
│   ├── PlacementTrackerApplication.java
│   ├── controller/ApplicationController.java
│   ├── model/Application.java
│   ├── repository/ApplicationRepository.java
│   ├── service/ApplicationService.java
│   ├── service/ResourceNotFoundException.java
│   └── config/DataInitializer.java # 3 sample rows if table empty
├── src/main/resources/
│   ├── application.properties
│   ├── templates/base.html, index.html, application-form.html,
│   │             application-detail.html, 404.html
│   └── static/styles.css
└── src/test/.../service + controller tests
```

## How to install / run

Requirements: JDK 17+, Maven 3.9+.

```powershell
cd placement-tracker
mvn spring-boot:run
# open http://localhost:8080
```

Database file `placement_tracker.db` appears in the project folder.
Delete it any time to start fresh (sample data returns on next run).

## How to run tests

```powershell
mvn test
```

18 tests: service unit tests (Mockito) + controller MockMvc tests.
They mock the repository/service, so the real `placement_tracker.db` is untouched.

## Main routes

| Method + path | Page |
|---|---|
| `GET /` | Dashboard table / empty state |
| `GET /applications/new` | Blank form |
| `POST /applications` | Create → redirect to details |
| `GET /applications/{id}` | Details (404 page if missing) |
| `GET /applications/{id}/edit` | Pre-filled form |
| `POST /applications/{id}/edit` | Update → redirect to details |
| `POST /applications/{id}/delete` | Delete (POST + `confirm()`) → redirect to dashboard |

## Basic request flow (viva)

```text
Browser → Controller → Service → Repository → Hibernate → SQLite
  → Repository → Service → Controller → Thymeleaf → HTML → Browser
```

* **Browser** sends HTTP + form data.
* **Controller** (`@GetMapping`/`@PostMapping`, `@PathVariable`, `@ModelAttribute`) handles web concerns, validation errors, redirects.
* **Service** owns rules (status whitelist, appliedOn-unless-Wishlist).
* **Repository** (`JpaRepository`) offers CRUD without SQL.
* **Hibernate** generates SQLite SQL from the `@Entity`.
* **SQLite** stores rows in `placement_tracker.db`.
* **Thymeleaf** renders HTML; browser displays it.
