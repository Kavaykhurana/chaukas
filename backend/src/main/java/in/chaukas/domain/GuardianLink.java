package in.chaukas.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "guardian_link")
@Getter @Setter @NoArgsConstructor
public class GuardianLink {
    public static final String PENDING = "PENDING";
    public static final String ACTIVE = "ACTIVE";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "ward_id", nullable = false)
    private Long wardId;
    @Column(name = "guardian_id")
    private Long guardianId;
    @Column(name = "invite_code", nullable = false, unique = true, length = 16)
    private String inviteCode;
    @Column(nullable = false, length = 10)
    private String status = PENDING;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
