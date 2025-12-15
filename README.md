# GGAnalyzer - Esports Statistics Platform

## Project Overview
GGAnalyzer is a comprehensive esports analytics platform designed to provide deep insights into professional League of Legends matches. By aggregating data from various sources, it empowers analysts, fans, and teams to visualize performance metrics, track tournament standings, and analyze player statistics across different stages of competition.

## How It Works
The application follows a modern full-stack architecture:

1.  **Data Ingestion & Processing**:
    -   **Data Source**: Leverages the Google Drive API to download match data directly from [OE Public Match Data](https://drive.google.com/drive/u/1/folders/1gLSw0RLjBbtaNy0dgnGQDAZOHIgCe-HH).
    -   **CSV Import**: Ingests raw match data from industry-standard CSV formats.
    -   **Automated Retrieval**: Utilizes `GoogleDriveService` and scheduled tasks to automatically fetch and update data files.

2.  **Backend (Spring Boot)**:
    -   Processes raw data into structured entities (Matches, Players, Teams).
    -   **Statistical Engine**: The `StatsService` computes complex aggregations, such as team win rates, player KDA, and damage metrics per tournament stage.
    -   Exposes a RESTful API for the frontend to consume.

3.  **Frontend (React + Vite)**:
    -   A responsive single-page application that visualizes the processed data.
    -   **Interactive Dashboards**: Users can filter stats by tournament stage and view dynamic charts. Additionally, users can post comments and rate matches.
    -   **State Management**: Uses React Query for efficient data fetching and caching.

## Advanced Features
-   **Concurrency & Multithreading**: The system employs multithreading to concurrently read data from multiple CSV files and calculate average team/player statistics per stage, significantly improving performance.
-   **Spring Boot & React**: Built with **Spring Boot**, **React**, and **Tailwind CSS**, ensuring high performance and maintainability.
-   **Scalable Database Integration**: Configured for **PostgreSQL** (local or Supabase) for robust data persistence.

---

## How to Run the Project

### Prerequisites
-   **Java 21** or later
-   **Node.js** (v18 or later recommended)
-   **PostgreSQL** (Local installation or Supabase)

### 1. Database Configuration
The project is configured to use a local PostgreSQL database by default.

1.  Ensure PostgreSQL is running locally.
2.  Create a database named `gganalyzer`.
3.  Update `backend/src/main/resources/application.properties` with your database credentials if they differ from the defaults:
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/gganalyzer
    spring.datasource.username=postgres
    spring.datasource.password=your_password
    ```
    *(Note: You can also use Supabase by uncommenting the respective sections in `application.properties`.)*

### 2. Google API Configuration
To enable automatic data updates from Google Drive, you need to configure a Service Account following the instructions in backend/GOOGLE_DRIVE_SETUP.md

I have included the data file in backend/data/resources. As a result, you can still run the project without configuring the Google API service.

### 3. Backend Setup
Navigate to the `backend` directory and run the application:

```bash
cd backend
mvn spring-boot:run
```
The backend server will start at `http://localhost:8080`.

**Data Seeding**: On startup, the application will attempt to download the data file and seed initial data from the downloaded CSV file located in the `backend/` directory.

### 4. Frontend Setup
Open a new terminal, navigate to the `frontend` directory, and start the development server:

```bash
cd frontend
npm install
npm run dev
```
The frontend application will be available at `http://localhost:5173`.

## Project Structure
-   `backend/`: Spring Boot application (API, Services, Data Ingestion)
-   `frontend/`: React + Vite application (UI, Components, Pages)
