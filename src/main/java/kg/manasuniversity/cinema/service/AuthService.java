package kg.manasuniversity.cinema.service;

import kg.manasuniversity.cinema.dto.auth.AuthRequest;
import kg.manasuniversity.cinema.dto.auth.AuthResponse;
import kg.manasuniversity.cinema.dto.auth.UserDto;
import kg.manasuniversity.cinema.entity.Role;
import kg.manasuniversity.cinema.entity.User;
import kg.manasuniversity.cinema.repository.UserRepository;
import kg.manasuniversity.cinema.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(AuthRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("EMAIL_TAKEN");
        }
        if (request.getPasswordConfirm() != null &&
                !request.getPassword().equals(request.getPasswordConfirm())) {
            throw new RuntimeException("PASSWORDS_DONT_MATCH");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("EMAIL_TAKEN");
        }

        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.USER
        );

        userRepository.save(user);

        return buildAuthResponse(user);

    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(String refreshToken) {
        String email = jwtService.getEmailFromToken(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        return buildAuthResponse(user);
    }

    // общий метод чтобы не дублировать код
    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setCreatedAt(user.getCreatedAt());

        return new AuthResponse(userDto, accessToken, refreshToken);
    }
}