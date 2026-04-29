package kg.manasuniversity.cinema.service;

import kg.manasuniversity.cinema.dto.response.UserResponse;
import kg.manasuniversity.cinema.entity.Role;
import kg.manasuniversity.cinema.entity.User;
import kg.manasuniversity.cinema.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toUserResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден c id: " + id));
        return toUserResponse(user);
    }

    @Transactional
    public UserResponse updateRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден c id: " + id));
        user.setRole(role);
        userRepository.save(user);
        return toUserResponse(user);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}