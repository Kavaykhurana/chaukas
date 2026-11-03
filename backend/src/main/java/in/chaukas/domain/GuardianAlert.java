package in.chaukas.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "guardian_alert")
@Getter @Setter @NoArgsConstructor
public class GuardianAlert {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "guardian_id", nullable = false)
    private Long guardianId;
    @Column(name = "ward_id", nullable = false)
    private Long wardId;
    @Column(name = "ward_name", nullable = false, length = 120)
    private String wardName;
    @Column(name = "check_id")
    private Long checkId;
    @Column(name = "scam_type", nullable = false, length = 24)
    private String scamType;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    @Column(name = "read_flag", nullable = false)
    private boolean read;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
