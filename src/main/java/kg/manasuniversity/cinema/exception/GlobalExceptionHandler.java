package kg.manasuniversity.cinema.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;


import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 409 — место занято
    @ExceptionHandler(SeatHeldException.class)
    public ResponseEntity<Map<String, Object>> handleSeatHeld(SeatHeldException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "SEAT_HELD",
                "message", "Одно или несколько мест уже заняты",
                "details", Map.of("seats", ex.getConflictSeats())
        ));
    }

    // 404 — не найдено
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        System.out.println("Exception caught: " + ex.getMessage());
        if (ex.getMessage().equals("FORBIDDEN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "FORBIDDEN",
                    "message", "Нет доступа"
            ));
        }
        if (ex.getMessage().equals("PASSWORDS_DONT_MATCH")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "VALIDATION_ERROR",
                    "message", "Пароли не совпадают"
            ));
        }
        if (ex.getMessage().contains("не найден")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "NOT_FOUND",
                    "message", ex.getMessage()
            ));
        }
        if (ex.getMessage().equals("EMAIL_TAKEN")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "EMAIL_TAKEN",
                    "message", "Email уже используется/ Email is already taken"
            ));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "BAD_REQUEST",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {

        Map<String, String> details = new java.util.HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> details.put(err.getField(), err.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "VALIDATION_ERROR",
                "message", "Ошибка валидации/ Valdation error",
                "details", details
        ));
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "error", "FORBIDDEN",
                "message", "Нет прав доступа/ no access rights"
        ));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "INVALID_CREDENTIALS",
                "message", "Неверный email или пароль"
        ));
    }
}