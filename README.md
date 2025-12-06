# GGAnalyzer - Esports Stats Platform

## Project Structure
- `backend`: Spring Boot application (API)
- `frontend`: React + Vite application (UI)

## Setup Instructions

### 1. Database Configuration (Supabase)
Open `backend/src/main/resources/application.properties` and update the following lines with your Supabase credentials:
```properties
spring.datasource.url=jdbc:postgresql://<YOUR_SUPABASE_HOST>:5432/postgres
spring.datasource.password=<YOUR_SUPABASE_PASSWORD>
```

### 2. Run Backend
Open a terminal in the `backend` directory:
```bash
./mvnw spring-boot:run
```
(Or `mvn spring-boot:run` if you have Maven installed globally)

### 3. Run Frontend
Open a terminal in the `frontend` directory:
```bash
npm install
npm run dev
```

The frontend will be available at `http://localhost:5173`.
The backend API will be available at `http://localhost:8080`.
