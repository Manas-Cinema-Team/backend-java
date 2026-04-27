package kg.manasuniversity.cinema.controller.api;

import kg.manasuniversity.cinema.dto.request.SessionRequest;
import kg.manasuniversity.cinema.dto.response.PageResponse;
import kg.manasuniversity.cinema.dto.response.SeatMapResponse;
import kg.manasuniversity.cinema.dto.response.SessionResponse;
import kg.manasuniversity.cinema.security.JwtService;
import kg.manasuniversity.cinema.service.SeatHoldService;
import kg.manasuniversity.cinema.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final SeatHoldService seatHoldService;
    private final JwtService jwtService;

    // публичные эндпоинты по ТЗ
    @GetMapping("/api/v1/sessions")
    public ResponseEntity<PageResponse<SessionResponse>> getSessions(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false, name = "movie_id") Long movieId,
            @RequestParam(required = false, name = "hall_id") Long hallId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(sessionService.getSessions(date, movieId, hallId, page, pageSize));
    }

    @GetMapping("/api/v1/sessions/{id}")
    public ResponseEntity<SessionResponse> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getSessionById(id));
    }

    // админский эндпоинт
    @PostMapping("/api/admin/sessions")
    public ResponseEntity<SessionResponse> createSession(@RequestBody SessionRequest request) {
        SessionResponse response = sessionService.createSession(
                request.movieId(),
                request.hallId(),
                request.startTime(),
                request.price()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping("/api/v1/sessions/{id}/seats")
    public ResponseEntity<SeatMapResponse> getSeats(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        // достаём email из токена если пользователь авторизован
        String email = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                email = jwtService.getEmailFromToken(token);
            } catch (Exception e) {
                // токен невалидный — просто не передаём email
            }
        }

        return ResponseEntity.ok(seatHoldService.getSeatMap(id, email));
    }
}