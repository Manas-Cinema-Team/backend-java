package kg.manasuniversity.cinema.service;

import kg.manasuniversity.cinema.dto.response.UserResponse;
import kg.manasuniversity.cinema.entity.Role;
import kg.manasuniversity.cinema.entity.User;
import kg.manasuniversity.cinema.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User mockUser;
    private Role mockRole;

    @BeforeEach
    void setUp() {
        // Assume Role is an enum or class. We use a mock here to avoid compilation issues
        // without knowing your exact Role implementation. If it's an enum (e.g., Role.USER),
        // you can safely replace mock(Role.class) with Role.USER.
        mockRole = mock(Role.class);

        mockUser = new User("test@manas.edu.kg", "password", mockRole);
        mockUser.setId(1L);
    }

    @Test
    void getAllUsers_ShouldReturnListOfUserResponses() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of(mockUser));

        // Act
        List<UserResponse> actualResponses = userService.getAllUsers();

        // Assert
        assertThat(actualResponses)
            .isNotNull()
            .hasSize(1);

        UserResponse response = actualResponses.get(0);
        assertThat(response.id()).isEqualTo(mockUser.getId());
        assertThat(response.email()).isEqualTo(mockUser.getEmail());
        assertThat(response.role()).isEqualTo(mockUser.getRole());
        assertThat(response.createdAt()).isEqualTo(mockUser.getCreatedAt());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUserResponse() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act
        UserResponse actualResponse = userService.getUserById(userId);

        // Assert
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.id()).isEqualTo(userId);
        assertThat(actualResponse.email()).isEqualTo("test@manas.edu.kg");

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(userId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Пользователь не найден c id: " + userId);

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void updateRole_WhenUserExists_ShouldUpdateRoleAndReturnResponse() {
        // Arrange
        Long userId = 1L;
        Role newRole = mock(Role.class); // A new role to update to
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act
        UserResponse actualResponse = userService.updateRole(userId, newRole);

        // Assert
        assertThat(mockUser.getRole()).isEqualTo(newRole); // Ensure the entity's role was updated
        assertThat(actualResponse.role()).isEqualTo(newRole); // Ensure the DTO reflects the new role

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(mockUser); // Ensure save() was called
    }

    @Test
    void updateRole_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        Long userId = 99L;
        Role newRole = mock(Role.class);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateRole(userId, newRole))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Пользователь не найден c id: " + userId);

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class)); // Ensure save() is skipped if not found
    }
}