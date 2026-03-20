# PopularVote Junie Guidelines

This project consists of a Kotlin Spring Boot API and a React TypeScript UI.

## Tech Stack

### API
- **Language**: Kotlin 2.1.10+
- **Framework**: Spring Boot 3.4.2 (Reactive Webflux)
- **Database**: R2DBC with MySQL/Aurora
- **Style**: ktlint (via `org.jlleitschuh.gradle.ktlint`)
- **Testing**: JUnit 5, Mockito Kotlin, Testcontainers, Reactor Test

### UI
- **Language**: TypeScript, React 19
- **UI Framework**: Material UI (MUI) v7
- **Testing**: Jest, React Testing Library
- **Formatting**: Prettier

## Coding Guidelines

### General
- Follow the existing project structure: `api` for backend, `ui` for frontend.
- Maintain consistent indentation and formatting as defined by ktlint and Prettier.

### API (Kotlin/Spring)
- Use functional/reactive programming patterns with Project Reactor.
- Prefer constructor injection.
- Use `kapt` for MapStruct processors.
- Follow Kotlin idiomatic style.

### UI (React/TypeScript)
- Use functional components and hooks.
- Use MUI components for consistent UI/UX.
- Ensure all new components have corresponding tests in `App.test.tsx` or similar files.
- Run `npm run format` (which uses Prettier) before submitting UI changes.

## Commands

### API
- Build: `./gradlew build`
- Test: `./gradlew test`
- Lint: `./gradlew ktlintCheck`
- Format: `./gradlew ktlintFormat`

### UI
- Start: `npm start`
- Test: `npm test`
- Format: `npm run format`
