# Repository Guidelines 
 
## Project Structure & Module Organization 
- `api/`: Spring Boot backend. Main code in `api/src/main/java/com/example/api`, configs in `api/src/main/resources/application.yaml`. 
- `web-app/`: Vue 2 frontend. App entry in `web-app/src/main.js`, routes in `web-app/src/router`, state in `web-app/src/store`, UI in `web-app/src/views` and `web-app/src/components`, static assets in `web-app/src/assets`, and `web-app/public`. 
 
## Build, Test, and Development Commands 
- Backend dev server: `mvn -f api/pom.xml spring-boot:run` (runs the API locally). 
- Backend build: `mvn -f api/pom.xml clean package` (creates the runnable jar). 
- Backend tests: `mvn -f api/pom.xml test` (runs tests if added). 
- Frontend install: `npm --prefix web-app install`. 
- Frontend dev server: `npm --prefix web-app run serve`. 
- Frontend build: `npm --prefix web-app run build`. 
 
## Coding Style & Naming Conventions 
- Java 11, Spring Boot, Spring Data, Spring Security. Use PascalCase for classes, camelCase for fields/methods, and keep packages under `com.example.api`. 
- Vue/JS: components and views use PascalCase file names (e.g., `InventoryRecords.vue`), JS modules use camelCase. 
- Indentation follows existing files: 4 spaces for Java, 2 spaces for JS/Vue. No formatter configured; keep diffs minimal. 
 
## Testing Guidelines 
- No test framework or test folders are present. Add backend tests under `api/src/test/java` and frontend tests only if introducing a framework. 
- Prefer naming tests after the class or feature under test (e.g., `InventoryServiceTest`). 
 
## Commit & Pull Request Guidelines 
- Commit messages in history are short and direct (no strict prefix). Keep them concise and descriptive. 
- PRs should include a summary, test steps, and screenshots for UI changes. Link related issues where possible. 
 
## Configuration & Security Notes 
- Backend configuration lives in `api/src/main/resources/application.yaml`. Do not commit real secrets; use local overrides or environment variables.
