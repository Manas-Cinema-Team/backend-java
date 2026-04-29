package kg.manasuniversity.cinema.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "seat_holds")
public class SeatHold {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Session session;

    @ManyToOne
    private User user;

    private Integer seatRow;
    private Integer seatNumber;

    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;



}
