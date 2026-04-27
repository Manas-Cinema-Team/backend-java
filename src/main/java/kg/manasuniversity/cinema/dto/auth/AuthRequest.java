package kg.manasuniversity.cinema.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthRequest {

    @NotBlank(message = "Email обязателен/ Email required")
    @Email(message = "Некорректный email/ Incorrect email")
    private String email;

    @NotBlank(message = "Пароль обязателен/ Password required")
    @Size(min = 8, message = "Пароль минимум 8 символов/ Password minimum 8 characters")
    private String password;

    private String passwordConfirm;
}