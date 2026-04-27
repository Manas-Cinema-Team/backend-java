package kg.manasuniversity.cinema.repository;

import kg.manasuniversity.cinema.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Найти все брони конкретного пользователя (для истории)
    List<Booking> findAllByUserId(Long userId);

    // Найти все брони на конкретный сеанс (чтобы знать занятые места)
    List<Booking> findAllBySessionId(Long sessionId);
}