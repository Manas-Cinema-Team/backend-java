package kg.manasuniversity.cinema.repository;

import kg.manasuniversity.cinema.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {
    // Получить список всех купленных мест для конкретного заказа
    List<BookingSeat> findAllByBookingSessionId(Long bookingId);
}