package kg.manasuniversity.cinema.service;

import jakarta.transaction.Transactional;
import kg.manasuniversity.cinema.dto.request.SeatHoldRequest;
import kg.manasuniversity.cinema.dto.response.SeatHoldResponse;
import kg.manasuniversity.cinema.entity.SeatHold;
import kg.manasuniversity.cinema.entity.SeatStatus;
import kg.manasuniversity.cinema.entity.Session;
import kg.manasuniversity.cinema.entity.User;
import kg.manasuniversity.cinema.repository.SeatHoldRepository;
import kg.manasuniversity.cinema.repository.SessionRepository;
import kg.manasuniversity.cinema.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SeatHoldService {

    private final SeatHoldRepository seatHoldRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Transactional
    public SeatHold holdSeat(Long sessionId, User user, Integer row, Integer seat) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Сеанс не найден"));

        Optional<SeatHold> existingHold = seatHoldRepository
                .findBySessionAndSeatRowAndSeatNumber(session, row, seat);

        if (existingHold.isPresent() && existingHold.get().getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Место уже удерживается другим пользователем");
        }

        // Если старое удержание протухло, удалим его перед созданием нового
        existingHold.ifPresent(seatHoldRepository::delete);

        SeatHold hold = new SeatHold();
        hold.setSession(session);
        hold.setUser(user);
        hold.setSeatRow(row);
        hold.setSeatNumber(seat);
        hold.setStatus(SeatStatus.HELD);
        hold.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        return seatHoldRepository.save(hold);
    }

    /**
     * Метод для HTTP-поллинга (Пункт 6.7 ТЗ).
     * Теперь метод в Repository перестанет быть серым!
     */
    public List<SeatHold> getActiveHolds(Long sessionId) {
        return seatHoldRepository.findAllBySessionIdAndExpiresAtAfter(sessionId, LocalDateTime.now());
    }

    /**
     * Автоматическая очистка просроченных броней (Пункт 6.7 ТЗ).
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseExpiredHolds() {
        LocalDateTime now = LocalDateTime.now();

        List<SeatHold> expiredHolds = seatHoldRepository.findAllByExpiresAtBeforeAndStatus(
                now,
                SeatStatus.HELD
        );

        if (!expiredHolds.isEmpty()) {
            seatHoldRepository.deleteAll(expiredHolds);
            System.out.println("Планировщик: Освобождено мест: " + expiredHolds.size());
        }
    }

    /**
     * Основной метод для обработки запроса от фронтенда (SeatHoldRequest)
     */
    @Transactional
    public SeatHoldResponse holdMultipleSeats(SeatHoldRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проходим по списку выбранных мест и для каждого вызываем твой holdSeat
        List<SeatHold> holds = request.selectedSeats().stream()
                .map(s -> holdSeat(request.sessionId(), user, s.row(), s.number()))
                .toList();

        // Формируем красивый Response для фронтенда
        return new SeatHoldResponse(
                request.sessionId(),
                holds.get(0).getExpiresAt(), // У всех мест в одном запросе будет одно время истечения
                holds.stream()
                        .map(h -> new SeatHoldResponse.HeldSeat(h.getSeatRow(), h.getSeatNumber()))
                        .toList()
        );
    }

}