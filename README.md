# BookMatch

BookMatch is a full-stack book recommendation application that helps users discover books based on movies or TV shows they enjoy.

The application analyzes themes, genres, atmosphere, and story elements from a selected title, then returns relevant book recommendations with covers, authors, descriptions, and ratings.

## Features

- Search by movie or TV show title
- Generate related book recommendations
- Display book covers and metadata from Google Books
- AI-assisted thematic matching
- Responsive React interface
- Light and dark themes
- REST API built with Spring Boot
- PostgreSQL database integration

## Technology Stack

### Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- PostgreSQL
- Maven
- Google Books API
- Google Gemini API

### Frontend

- React
- Vite
- JavaScript
- CSS
- Fetch API

## Project Structure

```text
BookMatch/
├── backend/
│   ├── .mvn/
│   ├── src/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
├── frontend/
│   ├── public/
│   ├── src/
│   ├── package.json
│   └── vite.config.js
├── .gitignore
└── README.md
```

## Prerequisites

Install the following tools before running the project:

- Java 21
- Node.js and npm
- PostgreSQL
- Git

## Backend Setup

### 1. Open the backend folder

```bash
cd backend
```

### 2. Configure PostgreSQL

Create a PostgreSQL database named:

```text
bookmatch
```

Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bookmatch
spring.datasource.username=YOUR_POSTGRES_USERNAME
spring.datasource.password=YOUR_POSTGRES_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 3. Configure API keys

Do not commit real API keys to GitHub.

Add the required values to your local configuration:

```properties
gemini.api.key=YOUR_GEMINI_API_KEY
```

Google Books requests can work without a key for basic usage, but a key may be added if required by your implementation.

### 4. Run the backend

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On Git Bash, Linux, or macOS:

```bash
./mvnw spring-boot:run
```

The backend normally runs at:

```text
http://localhost:8080
```

## Frontend Setup

Open a second terminal:

```bash
cd frontend
npm install
npm run dev
```

The frontend normally runs at:

```text
http://localhost:5173
```

## Main API Endpoints

### Get book recommendations

```http
GET /api/recommendations?movie=Interstellar
```

Example:

```text
http://localhost:8080/api/recommendations?movie=Interstellar
```

### Get all books

```http
GET /api/recommendations/all
```

## Example Response

```json
[
  {
    "id": 1,
    "title": "Dune",
    "author": "Frank Herbert",
    "description": "A science-fiction novel about politics, survival, and power.",
    "genre": "Science Fiction",
    "tags": "space,future,desert,politics",
    "rating": 4.8,
    "coverUrl": "https://example.com/dune-cover.jpg"
  }
]
```

## Current MVP

The current version focuses on:

- Movie and TV show input
- Book recommendation generation
- Book information and cover retrieval
- Backend and frontend integration
- Clean responsive user interface

## Roadmap

- Google authentication
- LinkedIn authentication
- User profiles
- Favorites
- Personal ratings
- Recommendation history
- Improved AI matching
- TV show search integration
- Deployment of frontend and backend
- Automated testing

## Security Notes

Never upload the following files or values to GitHub:

- API keys
- Database passwords
- `.env` files
- Local IDE configuration
- Generated build folders
- `node_modules`

Recommended `.gitignore` entries:

```gitignore
# Backend
backend/target/
*.iml
.idea/

# Frontend
frontend/node_modules/
frontend/dist/

# Environment files
.env
.env.local
application-local.properties
```

## Repository

```text
https://github.com/Jaouadechch/BookMatch
```

## Author

**Jaouad Echchaouy**

GitHub: `Jaouadechch`

## License

This project is currently intended for educational and portfolio purposes.
