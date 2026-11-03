package in.chaukas.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "scam_intel", uniqueConstraints = @UniqueConstraint(columnNames = {"kind", "value"}))
@Getter @Setter @NoArgsConstructor
public class ScamIntel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 8)
    private String kind;
    @Column(nullable = false, length = 190)
    private String value;
    @Column(name = "report_count", nullable = false)
    private int reportCount;
    @Column(nullable = false)
    private boolean confirmed;
    @Column(name = "first_seen", nullable = false)
    private Instant firstSeen = Instant.now();
    @Column(name = "last_seen", nullable = false)
    private Instant lastSeen = Instant.now();
}
