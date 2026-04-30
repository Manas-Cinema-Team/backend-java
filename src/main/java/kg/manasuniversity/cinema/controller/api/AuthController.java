package kg.manasuniversity.cinema.controller.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kg.manasuniversity.cinema.dto.auth.AuthRequest;
import kg.manasuniversity.cinema.dto.auth.AuthResponse;
import kg.manasuniversity.cinema.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${cinema.refresh-cookie.name:refresh_token}")
    private String refreshCookieName;

    @Value("${cinema.refresh-cookie.max-age:604800}")
    private long refreshCookieMaxAge;

    @Value("${cinema.refresh-cookie.secure:false}")
    private boolean refreshCookieSecure;

    @Value("${cinema.refresh-cookie.same-site:Lax}")
    private String refreshCookieSameSite;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request,
                                                 HttpServletResponse response) {
        AuthResponse body = authService.register(request);
        setRefreshCookie(response, body.getRefreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request,
                                              HttpServletResponse response) {
        AuthResponse body = authService.login(request);
        setRefreshCookie(response, body.getRefreshToken());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody(required = false) Map<String, String> body,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        String refreshToken = null;
        if (body != null) {
            refreshToken = body.get("refresh_token");
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            refreshToken = readRefreshCookie(request);
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "TOKEN_EXPIRED",
                            "message", "Refresh token отсутствует"));
        }

        AuthResponse refreshed = authService.refresh(refreshToken);
        setRefreshCookie(response, refreshed.getRefreshToken());
        return ResponseEntity.ok(refreshed);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        clearRefreshCookie(response);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    private void setRefreshCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(refreshCookieName, token)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path("/")
                .maxAge(refreshCookieMaxAge)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private String readRefreshCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (refreshCookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
