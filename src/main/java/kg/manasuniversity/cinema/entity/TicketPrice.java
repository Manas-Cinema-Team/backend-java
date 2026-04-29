package kg.manasuniversity.cinema.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ticket_prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency = "SOM";

    @Column(name = "pricing_source", nullable = false)
    private String pricingSource = "MANUAL";

}
