# Cinema Backend

Spring Boot backend for a simple cinema API.

## Requirements

- Java 21
- Maven 3.8+

Check your local versions:

```bash
java -version
mvn -version
```

## Run Locally

From the project root:

```bash
mvn spring-boot:run
```

The application starts on:

```text
http://127.0.0.1:8080
```

## Database

The project is configured to use an in-memory H2 database for local development.
No external database is required for the first run.

H2 console:

```text
http://127.0.0.1:8080/h2-console
```

Default JDBC URL:

```text
jdbc:h2:mem:cinema
```

Credentials:

```text
username: sa
password:
```

Data is reset every time the application restarts.

## API Smoke Test

List movies:

```bash
curl http://127.0.0.1:8080/api/movies
```

Create a movie:

```bash
curl -X POST http://127.0.0.1:8080/api/movies \
  -H 'Content-Type: application/json' \
  -d '{"name":"Inception"}'
```

Get a movie by ID:

```bash
curl http://127.0.0.1:8080/api/movies/1
```

## Run Tests

```bash
mvn test
```

## Project Structure

```text
src/main/java/kg/manasuniversity/cinema
  controller/   HTTP API endpoints
  service/      business logic
  repository/   database access through Spring Data JPA
  entity/       JPA entities
  dto/          request and response objects
  mapper/       entity-to-DTO mapping
  security/     Spring Security configuration
```

Build output is generated in `target/` and should not be committed.
