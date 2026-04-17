package kg.manasuniversity.cinema.service;

import kg.manasuniversity.cinema.dto.response.SessionResponse;
import kg.manasuniversity.cinema.entity.Hall;
import kg.manasuniversity.cinema.entity.Movie;
import kg.manasuniversity.cinema.entity.Session;
import kg.manasuniversity.cinema.entity.TicketPrice;
import kg.manasuniversity.cinema.repository.HallRepository;
import kg.manasuniversity.cinema.repository.MovieRepository;
import kg.manasuniversity.cinema.repository.SessionRepository;
import kg.manasuniversity.cinema.repository.TicketPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepository sessionRepository;
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;
    private final TicketPriceRepository ticketPriceRepository;

    @Transactional
    public SessionResponse createSession(Long movieId, Long hallId, LocalDateTime startTime, BigDecimal price) {
        // 1. Достаем фильм, чтобы узнать длительность
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Фильм не найден"));

        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new RuntimeException("Зал не найден"));

        // 2. Рассчитываем время окончания (длительность в минутах + 15 мин перерыв)
        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration()).plusMinutes(15);

        // 3. Проверка на пересечения в зале
        var overlaps = sessionRepository.findOverlappingSessions(hallId, startTime, endTime);
        if (!overlaps.isEmpty()) {
            throw new RuntimeException("Зал занят в это время другим сеансом!");
        }

        // 4. Сохраняем
        Session session = new Session();
        session.setMovie(movie);
        session.setHall(hall);
        session.setStartDatetime(startTime);
        session.setEndDatetime(endTime);
        session.setIsActive(true);

        Session savedSession = sessionRepository.save(session);

        TicketPrice ticketPrice = new TicketPrice();
        ticketPrice.setSession(savedSession);
        ticketPrice.setAmount(price);
        ticketPrice.setCurrency("SOM");
        ticketPrice.setPricingSource("MANUAL");

        ticketPriceRepository.save(ticketPrice);

        return new SessionResponse(
                savedSession.getId(),
                movie.getTitle(),
                hall.getName(),
                savedSession.getStartDatetime(),
                savedSession.getEndDatetime(),
                price,
                "SOM"
        );
    }
}
//asdkasd