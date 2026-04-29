# 🎬 Cinema Backend

> REST API for an online cinema — real-time seat booking system

![Java](https://img.shields.io/badge/Java-17+-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Docker](https://img.shields.io/badge/Docker-ready-blue)

---

## 📋 About

Backend service for an online cinema ticketing platform. Implements the full booking lifecycle — from browsing the movie schedule to confirming a reservation. Built as part of a team project (Vue 3 frontend + Spring Boot backend).

---

## 🛠 Tech Stack

| Technology | Purpose |
|-----------|---------|
| Java 21 | Primary language |
| Spring Boot 3.5.11 | Framework |
| Spring Security + OAuth2 Resource Server | JWT authentication |
| PostgreSQL 15 | Database |
| Flyway | Database migrations |
| Hibernate / JPA | ORM |
| jjwt 0.12.3 | JWT token generation |
| Lombok | Code generation |
| Docker + Docker Compose | Containerization |

---

## 🚀 Getting Started

### Prerequisites
- Docker and Docker Compose
- Java 21
- Maven

### Run with Docker

```bash
# Clone the repository
git clone https://github.com/your-repo/cinema-backend.git
cd cinema-backend

# Create environment file
cp .env.example .env

# Start the application
docker-compose up -d
```

### Run Locally

```bash
# Start PostgreSQL via Docker
docker-compose up -d postgres

# Run the application
./mvnw spring-boot:run
```

Application will be available at `http://localhost:8080`

---

## ⚙️ Environment Variables

Create a `.env` file in the project root:

```env
POSTGRES_DB=cinema
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_password
JWT_SECRET=your_base64_encoded_secret
```

> JWT_SECRET must be a valid BASE64 string of at least 32 characters.
> Generate one at: https://www.base64encode.org

---

## 📁 Project Structure

```
src/main/java/kg/manasuniversity/cinema/
├── controller/api/       # REST controllers
├── service/              # Business logic
├── repository/           # JPA repositories
├── entity/               # Database entities
├── dto/
│   ├── auth/             # Auth DTOs
│   ├── request/          # Request DTOs
│   └── response/         # Response DTOs
├── security/             # JWT & Security config
├── exception/            # Global exception handling
└── config/               # App configuration
```

---

## 🗄 Database

| Table | Description |
|-------|-------------|
| `users` | Registered users |
| `movies` | Movies catalog |
| `halls` | Cinema halls with seat layout |
| `sessions` | Movie sessions / screenings |
| `ticket_prices` | Pricing per session |
| `seat_holds` | Temporary seat reservations (10 min) |
| `bookings` | Confirmed bookings |
| `booking_seats` | Seats within a booking |

### Migrations (Flyway)
- `V1__init.sql` — create all tables
- `V2__seed_data.sql` — seed data (3 movies, 2 halls, 5 sessions)
- `V3__alter_timestamps.sql` — migrate to `TIMESTAMP WITH TIME ZONE`
- `V4__admin_user.sql` — create admin user
- `V5__update_admin_password.sql` — update admin password hash

---

## 🔐 Authentication

JWT-based authentication using Spring OAuth2 Resource Server.

| Token | Lifetime |
|-------|---------|
| Access Token | 15 minutes |
| Refresh Token | 7 days |

### Roles
| Role | Access |
|------|--------|
| `GUEST` | Public endpoints only |
| `USER` | Booking endpoints |
| `ADMIN` | Movie & session management |

### Default Admin Credentials
```
Email:    admin@cinema.com
Password: admin123
```

---

## 📡 API Reference

### Base URL
```
http://localhost:8080/api/v1
```

### Auth
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/auth/register` | No | Register new user |
| POST | `/auth/login` | No | Login and get tokens |
| POST | `/auth/refresh` | No | Refresh access token |
| POST | `/auth/logout` | JWT | Logout |

### Movies
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/movies` | No | List movies with pagination |
| GET | `/movies?genre=Action` | No | Filter by genre |
| GET | `/movies?search=inception` | No | Search by title |
| GET | `/movies/{id}` | No | Movie detail with sessions |
| POST | `/movies` | ADMIN | Create movie |
| PUT | `/movies/{id}` | ADMIN | Update movie |

### Sessions
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/sessions` | No | List sessions |
| GET | `/sessions?date=2026-05-01` | No | Filter by date |
| GET | `/sessions?movie_id=1` | No | Filter by movie |
| GET | `/sessions/{id}` | No | Session detail |
| GET | `/sessions/{id}/seats` | No | Seat map with statuses |

### Booking
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/bookings` | JWT | Create seat hold (10 min) |
| POST | `/bookings/{id}/confirm` | JWT | Confirm booking |
| DELETE | `/bookings/{id}` | JWT | Cancel booking |
| GET | `/bookings/{id}` | JWT | Get booking details |

---

## 💺 Seat Statuses

| Status | Description |
|--------|-------------|
| `available` | Seat is free |
| `held` | Temporarily held (10 min timer) |
| `booked` | Confirmed reservation |
| `disabled` | Seat unavailable in this hall |

---

## ❌ Error Format

All errors follow a unified format:

```json
{
  "error": "SEAT_HELD",
  "message": "One or more seats are already taken",
  "details": {
    "seats": [{"row": 1, "number": 1}]
  }
}
```

| Error Code | HTTP | Description |
|-----------|------|-------------|
| `UNAUTHORIZED` | 401 | Missing or expired token |
| `INVALID_CREDENTIALS` | 401 | Wrong email or password |
| `FORBIDDEN` | 403 | Insufficient permissions |
| `NOT_FOUND` | 404 | Resource not found |
| `EMAIL_TAKEN` | 409 | Email already registered |
| `SEAT_HELD` | 409 | Seat already taken |
| `VALIDATION_ERROR` | 400 | Input validation failed |

---

## 📅 Date Format

All dates are returned in **ISO 8601 UTC** format:

```
2026-05-01T10:00:00Z
```

Implemented using Java `Instant` + PostgreSQL `TIMESTAMP WITH TIME ZONE`.

---

## 🔄 Background Tasks

| Task | Interval | Description |
|------|----------|-------------|
| Release expired holds | Every 60 sec | Automatically frees seats where hold has expired |

---

## 🌐 CORS

Allowed origins:
- `http://localhost:5173` (Vue 3 dev server)

---

## 📝 Request Examples

### Register
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "passwordConfirm": "password123"
  }'
```

### Create Booking (Hold)
```bash
curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": 1,
    "seats": [
      {"row": 1, "number": 1},
      {"row": 1, "number": 2}
    ]
  }'
```

---

## 💳 Payment

Payment is implemented as a **mock** — booking confirmation automatically sets `paymentStatus: PAID` without real payment integration. Ready to connect a real payment gateway (Stripe, PayBox, etc.) in future sprints.

---

## 👥 Team

- **Backend (Spring Boot)** — Adilet Toktoberdiev % Birobidcan Karasartov
