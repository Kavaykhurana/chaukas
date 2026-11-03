package in.chaukas.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "check_record")
@Getter @Setter @NoArgsConstructor
public class CheckRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "input_kind", nullable = false, length = 12)
    private String inputKind;
    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;
    @Column(nullable = false, length = 8)
    private String risk;
    @Column(name = "scam_type", nullable = false, length = 24)
    private String scamType;
    @Column(nullable = false)
    private int score;
    @Column(name = "ai_used", nullable = false)
    private boolean aiUsed;
    @Column(nullable = false, length = 2)
    private String lang;
    @Column(name = "identifiers_json", nullable = false, columnDefinition = "TEXT")
    private String identifiersJson;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
