package kg.manasuniversity.cinema.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "booking_seats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "seat_row", nullable = false)
    private Integer seatRow;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    // ПУНКТ 10: Фиксируем цену именно на момент бронирования
    @Column(name = "price_at_booking", nullable = false)
    private BigDecimal priceAtBooking;
}