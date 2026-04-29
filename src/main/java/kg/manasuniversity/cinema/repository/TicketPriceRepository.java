package kg.manasuniversity.cinema.repository;

import kg.manasuniversity.cinema.entity.TicketPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TicketPriceRepository extends JpaRepository<TicketPrice, Long> {
    // В будущем здесь можно добавить поиск цены по ID сеанса:
     Optional<TicketPrice> findBySessionId(Long sessionId);
}