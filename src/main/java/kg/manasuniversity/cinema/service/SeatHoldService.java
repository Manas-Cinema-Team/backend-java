package kg.manasuniversity.cinema.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import kg.manasuniversity.cinema.dto.request.SeatHoldRequest;
import kg.manasuniversity.cinema.dto.response.SeatHoldResponse;
import kg.manasuniversity.cinema.entity.BookingSeat;
import kg.manasuniversity.cinema.entity.SeatHold;
import kg.manasuniversity.cinema.entity.SeatStatus;
import kg.manasuniversity.cinema.entity.Session;
import kg.manasuniversity.cinema.entity.User;
import kg.manasuniversity.cinema.repository.BookingSeatRepository;
import kg.manasuniversity.cinema.repository.SeatHoldRepository;
import kg.manasuniversity.cinema.repository.SessionRepository;
import kg.manasuniversity.cinema.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import kg.manasuniversity.cinema.dto.response.HallSchemaResponse;
import kg.manasuniversity.cinema.dto.response.SeatMapResponse;
import kg.manasuniversity.cinema.dto.response.SeatResponse;
import kg.manasuniversity.cinema.dto.response.PriceResponse;
import kg.manasuniversity.cinema.entity.Hall;
import kg.manasuniversity.cinema.entity.TicketPrice;
import kg.manasuniversity.cinema.repository.TicketPriceRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatHoldService {

    private final SeatHoldRepository seatHoldRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final TicketPriceRepository ticketPriceRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public SeatHold holdSeat(Long sessionId, User user, Integer row, Integer seat) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Сеанс не найден"));

        Optional<SeatHold> existingHold = seatHoldRepository
                .findBySessionAndSeatRowAndSeatNumber(session, row, seat);

        if (existingHold.isPresent() && existingHold.get().getExpiresAt().isAfter(Instant.now())) {
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
        hold.setExpiresAt(Instant.now().plus(Duration.ofMinutes(10)));

        return seatHoldRepository.save(hold);
    }

    /**
     * Метод для HTTP-поллинга (Пункт 6.7 ТЗ).
     * Теперь метод в Repository перестанет быть серым!
     */
    public List<SeatHold> getActiveHolds(Long sessionId) {
        return seatHoldRepository.findAllBySessionIdAndExpiresAtAfter(sessionId, Instant.now());
    }

    /**
     * Автоматическая очистка просроченных броней (Пункт 6.7 ТЗ).
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseExpiredHolds() {
        Instant now = Instant.now();

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

    @Transactional
    public void releaseExpiredHoldsForSession(Long sessionId) {
        List<SeatHold> expired = seatHoldRepository
                .findExpiredHoldsBySession(sessionId, Instant.now());

        if (!expired.isEmpty()) {
            seatHoldRepository.deleteAll(expired);
        }
    }

    public SeatMapResponse getSeatMap(Long sessionId, String currentUserEmail) {
        releaseExpiredHoldsForSession(sessionId);
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Сеанс не найден"));

        Hall hall = session.getHall();
        List<SeatHold> activeHolds = seatHoldRepository
                .findAllBySessionIdAndExpiresAtAfter(sessionId, Instant.now());

        List<BookingSeat> confirmedSeats = bookingSeatRepository.findAllConfirmedBySessionId(sessionId);

        TicketPrice ticketPrice = ticketPriceRepository.findBySessionId(sessionId).orElse(null);
        BigDecimal amount = ticketPrice != null ? ticketPrice.getAmount() : BigDecimal.ZERO;
        String currency = ticketPrice != null ? ticketPrice.getCurrency() : "KGS";

        HallSchemaResponse schema = buildSchema(hall);
        Set<String> disabledKeys = new HashSet<>();
        for (HallSchemaResponse.Coordinate c : schema.disabledSeats()) {
            disabledKeys.add(c.row() + ":" + c.number());
        }

        List<SeatResponse> seats = new ArrayList<>();
        int availableSeats = 0;

        for (int row = 1; row <= hall.getRows(); row++) {
            for (int number = 1; number <= hall.getSeatsPerRow(); number++) {
                final int r = row;
                final int n = number;

                Optional<SeatHold> hold = activeHolds.stream()
                        .filter(h -> h.getSeatRow().equals(r) && h.getSeatNumber().equals(n))
                        .findFirst();

                boolean booked = confirmedSeats.stream()
                        .anyMatch(b -> b.getSeatRow() == r && b.getSeatNumber() == n);

                String status = "available";
                boolean heldByMe = false;
                Instant expiresAt = null;
                String type = resolveSeatType(schema, r, n);

                if (disabledKeys.contains(r + ":" + n)) {
                    status = "disabled";
                } else if (booked) {
                    status = "booked";
                } else if (hold.isPresent()) {
                    SeatHold h = hold.get();
                    status = "held";
                    expiresAt = h.getExpiresAt();
                    heldByMe = currentUserEmail != null &&
                            h.getUser().getEmail().equals(currentUserEmail);
                }

                if ("available".equals(status)) {
                    availableSeats++;
                }

                seats.add(new SeatResponse(
                        row, number, type,
                        status, heldByMe, expiresAt,
                        new PriceResponse(amount, currency)
                ));
            }
        }

        return new SeatMapResponse(
                hall.getId(),
                hall.getName(),
                schema,
                seats,
                5,
                Instant.now(),
                availableSeats
        );
    }

    private String resolveSeatType(HallSchemaResponse schema, int row, int number) {
        if (schema == null) return "standard";
        for (HallSchemaResponse.SeatMeta meta : schema.seats()) {
            if (meta.row() == row && meta.number() == number) {
                return meta.type();
            }
        }
        return "standard";
    }

    private HallSchemaResponse buildSchema(Hall hall) {
        if (hall.getSchemaMetadata() != null && !hall.getSchemaMetadata().isBlank()) {
            try {
                JsonNode root = objectMapper.readTree(hall.getSchemaMetadata());
                List<HallSchemaResponse.Row> rows = new ArrayList<>();
                List<HallSchemaResponse.SeatMeta> seats = new ArrayList<>();
                List<HallSchemaResponse.Coordinate> disabled = new ArrayList<>();

                JsonNode rowsNode = root.path("rows");
                if (rowsNode.isArray()) {
                    for (JsonNode rowNode : rowsNode) {
                        int rowNumber = rowNode.path("row").asInt();
                        List<HallSchemaResponse.RowSeat> rowSeats = new ArrayList<>();
                        JsonNode seatsArr = rowNode.path("seats");
                        if (seatsArr.isArray()) {
                            for (JsonNode s : seatsArr) {
                                rowSeats.add(new HallSchemaResponse.RowSeat(
                                        s.path("number").asInt(),
                                        s.path("type").asText("standard")
                                ));
                            }
                        }
                        rows.add(new HallSchemaResponse.Row(rowNumber, rowSeats));
                    }
                }

                JsonNode seatsNode = root.path("seats");
                if (seatsNode.isArray()) {
                    for (JsonNode s : seatsNode) {
                        seats.add(new HallSchemaResponse.SeatMeta(
                                s.path("row").asInt(),
                                s.path("number").asInt(),
                                s.path("type").asText("standard")
                        ));
                    }
                }

                JsonNode disabledNode = root.path("disabled_seats");
                if (disabledNode.isArray()) {
                    for (JsonNode d : disabledNode) {
                        disabled.add(new HallSchemaResponse.Coordinate(
                                d.path("row").asInt(),
                                d.path("number").asInt()
                        ));
                    }
                }

                if (rows.isEmpty()) {
                    rows = defaultRows(hall);
                }
                if (seats.isEmpty()) {
                    seats = defaultSeatMeta(hall);
                }

                return new HallSchemaResponse(rows, seats, disabled);
            } catch (Exception ignored) {
                // fallback to default
            }
        }
        return new HallSchemaResponse(defaultRows(hall), defaultSeatMeta(hall), List.of());
    }

    private List<HallSchemaResponse.Row> defaultRows(Hall hall) {
        List<HallSchemaResponse.Row> rows = new ArrayList<>();
        for (int row = 1; row <= hall.getRows(); row++) {
            List<HallSchemaResponse.RowSeat> rowSeats = new ArrayList<>();
            for (int number = 1; number <= hall.getSeatsPerRow(); number++) {
                rowSeats.add(new HallSchemaResponse.RowSeat(number, "standard"));
            }
            rows.add(new HallSchemaResponse.Row(row, rowSeats));
        }
        return rows;
    }

    private List<HallSchemaResponse.SeatMeta> defaultSeatMeta(Hall hall) {
        List<HallSchemaResponse.SeatMeta> seats = new ArrayList<>();
        for (int row = 1; row <= hall.getRows(); row++) {
            for (int number = 1; number <= hall.getSeatsPerRow(); number++) {
                seats.add(new HallSchemaResponse.SeatMeta(row, number, "standard"));
            }
        }
        return seats;
    }
}