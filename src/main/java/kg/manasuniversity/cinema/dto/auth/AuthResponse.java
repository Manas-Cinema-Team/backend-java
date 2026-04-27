package kg.manasuniversity.cinema.dto.auth;

import lombok.Data;

@Data
public class AuthResponse {
    private UserDto user;
    private String accessToken;
    private String refreshToken;

    public AuthResponse(UserDto user, String accessToken, String refreshToken) {
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}