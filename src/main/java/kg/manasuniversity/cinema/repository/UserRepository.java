package kg.manasuniversity.cinema.repository;

import kg.manasuniversity.cinema.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // На будущее: поиск по email для авторизации (Пункт 6.5 ТЗ)
    Optional<User> findByEmail(String email);
}