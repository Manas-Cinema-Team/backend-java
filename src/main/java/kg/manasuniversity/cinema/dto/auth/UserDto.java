package kg.manasuniversity.cinema.dto.auth;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;
    private String email;
    private LocalDateTime createdAt;
}