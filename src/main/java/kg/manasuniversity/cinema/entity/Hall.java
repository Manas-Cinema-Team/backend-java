package kg.manasuniversity.cinema.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "halls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Hall {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer rows;

    @Column(name = "seats_per_row", nullable = false)
    private Integer seatsPerRow;

    /**
     * JSON-объект из ТЗ:
     * { "rows": [...], "seats": [...], "disabled_seats": [...] }
     */
    @Column(name = "schema_metadata", columnDefinition = "jsonb")
    private String schemaMetadata;

}