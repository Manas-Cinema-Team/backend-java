package kg.manasuniversity.cinema.controller.api;

import kg.manasuniversity.cinema.dto.request.SessionRequest;
import kg.manasuniversity.cinema.dto.response.SessionResponse;
import kg.manasuniversity.cinema.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<SessionResponse> createSession(@RequestBody SessionRequest request) {
        SessionResponse response = sessionService.createSession(
                request.movieId(),
                request.hallId(),
                request.startTime(),
                request.price()
        );

        // Возвращаем 201 Created и DTO вместо Entity
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}