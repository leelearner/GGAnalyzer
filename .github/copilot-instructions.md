# GGAnalyzer AI Agent Instructions

## Project Overview
GGAnalyzer is an esports statistics platform with a Spring Boot backend and React/Vite frontend.
- **Backend**: Java 21, Spring Boot 3.2.3, PostgreSQL (Supabase), JPA/Hibernate.
- **Frontend**: React 18, Vite, Tailwind CSS, React Query, Axios.

## Architecture & Data Flow
- **API-First**: Frontend consumes REST endpoints at `http://localhost:8080/api/...`.
- **Data Ingestion**:
  - Primary data source is CSV import via `CsvImportService`.
  - `DataSeeder.java` runs on startup to seed initial data and trigger CSV import.
  - **Critical**: `DataSeeder.java` currently uses a hardcoded absolute path for the CSV file. When working on this, ensure the path matches the environment or refactor to be relative.
- **Database**:
  - Uses PostgreSQL (Supabase) in production/dev.
  - H2 configuration is available but commented out in `application.properties`.

## Backend Development (Java/Spring Boot)
- **Location**: `backend/`
- **Build & Run**: `./mvnw spring-boot:run`
- **Key Components**:
  - `Controller`: REST endpoints (`api/`).
  - `Service`: Business logic (`service/`). Note: `CrawarlService` (typo intended?) exists for scraping.
  - `Repository`: JPA interfaces (`repository/`).
  - `Model`: JPA entities (`model/`). Uses Lombok (`@Data`, `@Builder`).
- **Conventions**:
  - Use Lombok for boilerplate.
  - Use `DataSeeder` for initial data population.

## Frontend Development (React/Vite)
- **Location**: `frontend/`
- **Build & Run**: `npm run dev` (runs on port 5173).
- **State Management**: Uses `@tanstack/react-query` for server state.
- **Styling**: Tailwind CSS.
- **API Calls**:
  - Uses `axios` directly in components or query functions.
  - Base URL is currently hardcoded to `http://localhost:8080`.
- **Structure**:
  - `pages/`: Route components (e.g., `Stats.jsx`, `Standings.jsx`).
  - `components/`: Reusable UI components.

## Common Tasks & Gotchas
- **CSV Import**: If modifying data ingestion, check `CsvImportService` and `DataSeeder`. The CSV file `LCK 2025 Rounds 1-2...` is in the `backend/` root.
- **CORS**: Ensure backend allows requests from `http://localhost:5173`.
- **Typo**: `CrawarlService` is likely a typo for `CrawlService`. Be aware when referencing it.
