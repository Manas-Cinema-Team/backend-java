package kg.manasuniversity.cinema.dto.response;

import kg.manasuniversity.cinema.entity.Role;
import java.time.Instant;

public record UserResponse(
        Long id,
        String email,
        Role role,
        Instant createdAt
) {}