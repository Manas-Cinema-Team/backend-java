package kg.manasuniversity.cinema.exception;

import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SeatHeldException.class)
    public ResponseEntity<Map<String, Object>> handleSeatHeld(SeatHeldException ex) {
        return body(HttpStatus.CONFLICT, "SEAT_HELD", "Одно или несколько мест уже заняты",
                Map.of("seats", ex.getConflictSeats()));
    }

    @ExceptionHandler(ActiveHoldExistsException.class)
    public ResponseEntity<Map<String, Object>> handleActiveHold(ActiveHoldExistsException ex) {
        return body(HttpStatus.CONFLICT, "ACTIVE_HOLD_EXISTS",
                "У вас уже есть активное бронирование на этот сеанс",
                Map.of("booking_id", ex.getBookingId()));
    }

    @ExceptionHandler(HoldExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleHoldExpired(HoldExpiredException ex) {
        return body(HttpStatus.CONFLICT, "HOLD_EXPIRED", "Время ожидания истекло", null);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> handleJwt(JwtException ex) {
        return body(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "Токен истёк или недействителен", null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return body(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Неверный email или пароль", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return body(HttpStatus.FORBIDDEN, "FORBIDDEN", "Нет прав доступа", null);
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {

        Map<String, String> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> details.put(err.getField(), err.getDefaultMessage()));

        return body(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Ошибка валидации", details);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        String msg = ex.getMessage() == null ? "" : ex.getMessage();

        if (msg.equals("FORBIDDEN")) {
            return body(HttpStatus.FORBIDDEN, "FORBIDDEN", "Нет доступа", null);
        }
        if (msg.equals("PASSWORDS_DONT_MATCH")) {
            return body(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Пароли не совпадают", null);
        }
        if (msg.equals("EMAIL_TAKEN")) {
            return body(HttpStatus.CONFLICT, "EMAIL_TAKEN", "Email уже используется", null);
        }
        if (msg.equals("TOKEN_EXPIRED")) {
            return body(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "Токен истёк", null);
        }
        if (msg.contains("не найден") || msg.contains("не найдена")) {
            return body(HttpStatus.NOT_FOUND, "NOT_FOUND", msg, null);
        }
        return body(HttpStatus.BAD_REQUEST, "BAD_REQUEST", msg.isEmpty() ? "Bad request" : msg, null);
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String code, String message,
                                                     Object details) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("error", code);
        payload.put("message", message);
        if (details != null) {
            payload.put("details", details);
        }
        return ResponseEntity.status(status).body(payload);
    }
}
