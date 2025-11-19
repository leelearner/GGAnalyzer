# GGAnalyzer

A full-stack application with a Spring Boot backend and React (Vite + TypeScript) frontend.

## Project Structure

```
GGAnalyzer/
├── backend/          # Spring Boot REST API
└── frontend/         # React + Vite + TypeScript
```

## Prerequisites

- **Java 8+** (for Spring Boot)
- **Maven 3.6+**
- **Node.js 18+** and npm

## Running the Application

### 1. Start the Backend

Navigate to the `backend` directory and run:

```bash
cd backend
mvn spring-boot:run
```

The backend will start on **http://localhost:8080**

### 2. Start the Frontend

In a separate terminal, navigate to the `frontend` directory:

```bash
cd frontend
npm install    # Only needed the first time
npm run dev
```

The frontend dev server will start on **http://localhost:5173** (or the next available port).

The Vite dev server is configured to proxy API requests from `/api` to `http://localhost:8080`, so the frontend can call the backend without CORS issues.

## Building for Production

### Backend
```bash
cd backend
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### Frontend
```bash
cd frontend
npm run build
```

The production build will be output to `frontend/dist/`.

## Tech Stack

- **Backend:** Spring Boot 3.x, Java 17
- **Frontend:** React 19, TypeScript, Vite 7
- **Build Tools:** Maven, npm
