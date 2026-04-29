package kg.manasuniversity.cinema.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtService {

    @Value("${cinema.jwt.secret}")
    private String secret;

    // access token — 15 минут (по ТЗ)
    public String generateAccessToken(String email, String role) {
        Date expiration = Date.from(Instant.now().plus(Duration.ofMinutes(15)));

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .expiration(expiration)
                .signWith(getSignKey())
                .compact();
    }

    // refresh token — 7 дней (по ТЗ)
    public String generateRefreshToken(String email) {
        Date expiration = Date.from(Instant.now().plus(Duration.ofDays(7)));

        return Jwts.builder()
                .subject(email)
                .expiration(expiration)
                .signWith(getSignKey())
                .compact();
    }

    // достать email из refresh токена
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}