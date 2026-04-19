// src/main/java/kg/manasuniversity/cinema/repository/SeatHoldRepository.java
package kg.manasuniversity.cinema.repository;

import kg.manasuniversity.cinema.entity.SeatHold;
import kg.manasuniversity.cinema.entity.SeatStatus;
import kg.manasuniversity.cinema.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {

    // Найти удержание для конкретного места на конкретном сеансе
    // Это нужно для проверки перед созданием нового удержания
    Optional<SeatHold> findBySessionAndSeatRowAndSeatNumber(Session session, Integer seatRow, Integer seatNumber);

    // Найти все "протухшие" удержания (где время истекло, а статус всё еще HELD)
    // Это понадобится для нашего автоматического чистильщика по ТЗ
    List<SeatHold> findAllByExpiresAtBeforeAndStatus(LocalDateTime dateTime, SeatStatus status);

    // Найти все активные удержания для конкретного сеанса
    // Нужно для того самого HTTP-поллинга (пункт 6.7 ТЗ)
    List<SeatHold> findAllBySessionIdAndExpiresAtAfter(Long sessionId, LocalDateTime now);
}