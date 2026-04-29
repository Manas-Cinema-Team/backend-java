📋 About
Backend service for an online cinema ticketing platform. Implements the full booking lifecycle — from browsing the movie schedule to confirming a reservation. Built as part of a team project (Vue 3 frontend + Spring Boot backend).

🛠 Tech Stack
TechnologyPurposeJava 17+Primary languageSpring Boot 3.xFrameworkSpring Security + OAuth2 Resource ServerJWT authenticationPostgreSQL 15DatabaseFlywayDatabase migrationsHibernate / JPAORMjjwt 0.12.3JWT token generationLombokCode generationDocker + Docker ComposeContainerization

🚀 Getting Started
Prerequisites

Docker and Docker Compose
Java 17+
Maven

Run with Docker
bash# Clone the repository
git clone https://github.com/your-repo/cinema-backend.git
cd cinema-backend

# Create environment file
cp .env.example .env

# Start the application
docker-compose up -d
Run Locally
bash# Start PostgreSQL via Docker
docker-compose up -d postgres

# Run the application
./mvnw spring-boot:run
Application will be available at http://localhost:8080

⚙️ Environment Variables
Create a .env file in the project root:
envPOSTGRES_DB=cinema
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_password
JWT_SECRET=your_base64_encoded_secret

JWT_SECRET must be a valid BASE64 string of at least 32 characters.
Generate one at: https://www.base64encode.org


📁 Project Structure
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

🗄 Database
TableDescriptionusersRegistered usersmoviesMovies cataloghallsCinema halls with seat layoutsessionsMovie sessions / screeningsticket_pricesPricing per sessionseat_holdsTemporary seat reservations (10 min)bookingsConfirmed bookingsbooking_seatsSeats within a booking
Migrations (Flyway)

V1__init.sql — create all tables
V2__seed_data.sql — seed data (3 movies, 2 halls, 5 sessions)
V3__alter_timestamps.sql — migrate to TIMESTAMP WITH TIME ZONE
V4__admin_user.sql — create admin user
V5__update_admin_password.sql — update admin password hash


🔐 Authentication
JWT-based authentication using Spring OAuth2 Resource Server.
TokenLifetimeAccess Token15 minutesRefresh Token7 days
Roles
RoleAccessGUESTPublic endpoints onlyUSERBooking endpointsADMINMovie & session management
Default Admin Credentials
Email:    admin@cinema.com
Password: admin123

📡 API Reference
Base URL
http://localhost:8080/api/v1
Auth
MethodEndpointAuthDescriptionPOST/auth/registerNoRegister new userPOST/auth/loginNoLogin and get tokensPOST/auth/refreshNoRefresh access tokenPOST/auth/logoutJWTLogout
Movies
MethodEndpointAuthDescriptionGET/moviesNoList movies with paginationGET/movies?genre=ActionNoFilter by genreGET/movies?search=inceptionNoSearch by titleGET/movies/{id}NoMovie detail with sessionsPOST/moviesADMINCreate moviePUT/movies/{id}ADMINUpdate movie
Sessions
MethodEndpointAuthDescriptionGET/sessionsNoList sessionsGET/sessions?date=2026-05-01NoFilter by dateGET/sessions?movie_id=1NoFilter by movieGET/sessions/{id}NoSession detailGET/sessions/{id}/seatsNoSeat map with statuses
Booking
MethodEndpointAuthDescriptionPOST/bookingsJWTCreate seat hold (10 min)POST/bookings/{id}/confirmJWTConfirm bookingDELETE/bookings/{id}JWTCancel bookingGET/bookings/{id}JWTGet booking details

💺 Seat Statuses
StatusDescriptionavailableSeat is freeheldTemporarily held (10 min timer)bookedConfirmed reservationdisabledSeat unavailable in this hall

❌ Error Format
All errors follow a unified format:
json{
  "error": "SEAT_HELD",
  "message": "One or more seats are already taken",
  "details": {
    "seats": [{"row": 1, "number": 1}]
  }
}
Error CodeHTTPDescriptionUNAUTHORIZED401Missing or expired tokenINVALID_CREDENTIALS401Wrong email or passwordFORBIDDEN403Insufficient permissionsNOT_FOUND404Resource not foundEMAIL_TAKEN409Email already registeredSEAT_HELD409Seat already takenVALIDATION_ERROR400Input validation failed

📅 Date Format
All dates are returned in ISO 8601 UTC format:
2026-05-01T10:00:00Z
Implemented using Java Instant + PostgreSQL TIMESTAMP WITH TIME ZONE.

🔄 Background Tasks
TaskIntervalDescriptionRelease expired holdsEvery 60 secAutomatically frees seats where hold has expired

🌐 CORS
Allowed origins:

http://localhost:5173 (Vue 3 dev server)


📝 Request Examples
Register
bashcurl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "passwordConfirm": "password123"
  }'
Create Booking (Hold)
bashcurl -X POST http://localhost:8080/api/v1/bookings \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": 1,
    "seats": [
      {"row": 1, "number": 1},
      {"row": 1, "number": 2}
    ]
  }'

💳 Payment
Payment is implemented as a mock — booking confirmation automatically sets paymentStatus: PAID without real payment integration. Ready to connect a real payment gateway (Stripe, PayBox, etc.) in future sprints.

👥 Team

Backend (Spring Boot) — Erkin & Adilet
Frontend (Vue 3) — Frontend Team
Backend (Django) — Django Team
