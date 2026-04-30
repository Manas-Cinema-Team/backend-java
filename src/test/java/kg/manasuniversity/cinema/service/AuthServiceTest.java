package kg.manasuniversity.cinema.service;

import kg.manasuniversity.cinema.dto.auth.AuthRequest;
import kg.manasuniversity.cinema.dto.auth.AuthResponse;
import kg.manasuniversity.cinema.entity.Role;
import kg.manasuniversity.cinema.entity.User;
import kg.manasuniversity.cinema.repository.UserRepository;
import kg.manasuniversity.cinema.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User mockUser;
    private AuthRequest mockRequest;
    private final String testEmail = "test@manas.edu.kg";

    @BeforeEach
    void setUp() {
        mockUser = new User(testEmail, "encodedPassword", Role.USER);
        mockUser.setId(1L);

        mockRequest = new AuthRequest();
        mockRequest.setEmail(testEmail);
        mockRequest.setPassword("password123");
        mockRequest.setPasswordConfirm("password123");
    }

    // ==========================================
    // Register Tests
    // ==========================================

    @Test
    void register_WhenValidRequest_ShouldSaveUserAndReturnTokens() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(mockRequest.getPassword())).thenReturn("encodedPassword");
        when(jwtService.generateAccessToken(testEmail, Role.USER.name())).thenReturn("mockAccessToken");
        when(jwtService.generateRefreshToken(testEmail)).thenReturn("mockRefreshToken");

        // Act
        AuthResponse response = authService.register(mockRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mockAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("mockRefreshToken");
        assertThat(response.getUser().getEmail()).isEqualTo(testEmail);

        // Verify the user was constructed correctly before saving
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(testEmail);
        assertThat(savedUser.getPasswordHash()).isEqualTo("encodedPassword");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void register_WhenEmailAlreadyTaken_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(mockUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.register(mockRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("EMAIL_TAKEN");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_WhenPasswordsDoNotMatch_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        mockRequest.setPasswordConfirm("differentPassword");

        // Act & Assert
        assertThatThrownBy(() -> authService.register(mockRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("PASSWORDS_DONT_MATCH");

        verify(userRepository, never()).save(any(User.class));
    }

    // ==========================================
    // Login Tests
    // ==========================================

    @Test
    void login_WhenCredentialsValid_ShouldReturnTokens() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(mockUser));
        when(jwtService.generateAccessToken(testEmail, Role.USER.name())).thenReturn("mockAccessToken");
        when(jwtService.generateRefreshToken(testEmail)).thenReturn("mockRefreshToken");

        // Act
        AuthResponse response = authService.login(mockRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mockAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("mockRefreshToken");

        // Verify AuthenticationManager was actually called to validate credentials
        verify(authenticationManager).authenticate(
            new UsernamePasswordAuthenticationToken(mockRequest.getEmail(), mockRequest.getPassword())
        );
    }

    @Test
    void login_WhenCredentialsInvalid_ShouldThrowException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(mockRequest))
            .isInstanceOf(BadCredentialsException.class);

        // Ensure token generation is never reached
        verify(jwtService, never()).generateAccessToken(anyString(), anyString());
    }

    @Test
    void login_WhenUserNotFoundAfterAuth_ShouldThrowException() {
        // Edge Case: Auth succeeds in AuthenticationManager (e.g., via LDAP/Cache) but user doesn't exist in local DB.

        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(mockRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Пользователь не найден");
    }

    // ==========================================
    // Refresh Tests
    // ==========================================

    @Test
    void refresh_WhenTokenValid_ShouldReturnNewTokens() {
        // Arrange
        String oldRefreshToken = "oldValidRefreshToken";
        when(jwtService.getEmailFromToken(oldRefreshToken)).thenReturn(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(mockUser));
        when(jwtService.generateAccessToken(testEmail, Role.USER.name())).thenReturn("newAccessToken");
        when(jwtService.generateRefreshToken(testEmail)).thenReturn("newRefreshToken");

        // Act
        AuthResponse response = authService.refresh(oldRefreshToken);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("newRefreshToken");
        assertThat(response.getUser().getEmail()).isEqualTo(testEmail);
    }

    @Test
    void refresh_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        String oldRefreshToken = "oldValidRefreshToken";
        when(jwtService.getEmailFromToken(oldRefreshToken)).thenReturn(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.refresh(oldRefreshToken))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Пользователь не найден");

        verify(jwtService, never()).generateAccessToken(anyString(), anyString());
    }
}