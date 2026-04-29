package kg.manasuniversity.cinema.service;

import kg.manasuniversity.cinema.dto.response.*;
import kg.manasuniversity.cinema.entity.*;
import kg.manasuniversity.cinema.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepository sessionRepository;
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;
    private final TicketPriceRepository ticketPriceRepository;
    private final SeatHoldRepository seatHoldRepository;

    @Value("${cinema.session.break-time:15}")
    private int breakTime;

    public List<SessionResponse> getActiveSessions() {
        return sessionRepository.findAllByIsActiveTrue()
                .stream()
                .map(this::toSessionResponse)
                .toList();
    }

    public List<SessionResponse> getSessionsByMovie(Long movieId) {
        return sessionRepository.findAllByMovieIdAndIsActiveTrue(movieId)
                .stream()
                .map(this::toSessionResponse)
                .toList();
    }

    // новый метод
    public SessionResponse getSessionById(Long id) {
        Session session = sessionRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Сеанс не найден"));
        return toSessionResponse(session);
    }

    // новый метод
    public PageResponse<SessionResponse> getSessions(LocalDate date, Long movieId, Long hallId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Session> sessionPage = sessionRepository.findAllByFilters(date, movieId, hallId, pageable);

        List<SessionResponse> results = sessionPage.getContent()
                .stream()
                .map(this::toSessionResponse)
                .toList();

        String next = sessionPage.hasNext() ? "/api/v1/sessions?page=" + (page + 1) + "&page_size=" + pageSize : null;
        String previous = sessionPage.hasPrevious() ? "/api/v1/sessions?page=" + (page - 1) + "&page_size=" + pageSize : null;

        return new PageResponse<>(sessionPage.getTotalElements(), next, previous, results);
    }

    @Transactional
    public SessionResponse createSession(Long movieId, Long hallId, Instant startTime, BigDecimal price) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Фильм не найден"));

        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new RuntimeException("Зал не найден"));

        Instant endTime = startTime.plus(movie.getDuration() + breakTime, ChronoUnit.MINUTES);
        var overlaps = sessionRepository.findOverlappingSessions(hallId, startTime, endTime);
        if (!overlaps.isEmpty()) {
            throw new RuntimeException("Зал занят в это время другим сеансом!");
        }

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
        ticketPrice.setCurrency("KGS");
        ticketPrice.setPricingSource("MANUAL");

        ticketPriceRepository.save(ticketPrice);

        return toSessionResponse(savedSession);
    }

    public Session findById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Сессия не найдена с id: " + id));
    }

    public SessionResponse toSessionResponse(Session session) {
        TicketPrice ticketPrice = ticketPriceRepository.findBySessionId(session.getId())
                .orElse(null);

        PriceResponse price = ticketPrice != null
                ? new PriceResponse(ticketPrice.getAmount(), ticketPrice.getCurrency())
                : new PriceResponse(BigDecimal.ZERO, "KGS");

        int totalSeats = session.getHall().getRows() * session.getHall().getSeatsPerRow();
        int heldOrBooked = seatHoldRepository.countActiveBySessionId(session.getId(), Instant.now());
        int availableSeats = totalSeats - heldOrBooked;

        return new SessionResponse(
                session.getId(),
                new MovieShortResponse(
                        session.getMovie().getId(),
                        session.getMovie().getTitle(),
                        session.getMovie().getPosterUrl(),
                        session.getMovie().getDuration(),
                        session.getMovie().getAgeRating()
                ),
                new HallShortResponse(
                        session.getHall().getId(),
                        session.getHall().getName(),
                        session.getHall().getRows(),
                        session.getHall().getSeatsPerRow()
                ),
                session.getStartDatetime(),
                session.getEndDatetime(),
                price,
                session.getIsActive(),
                availableSeats
        );
    }
}