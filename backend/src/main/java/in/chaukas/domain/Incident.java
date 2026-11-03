package in.chaukas.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "incident")
@Getter @Setter @NoArgsConstructor
public class Incident {
    public static final String OPEN = "OPEN";
    public static final String REPORTED = "REPORTED";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(precision = 14, scale = 2)
    private BigDecimal amount;
    @Column(length = 20)
    private String utr;
    @Column(name = "payee_upi", length = 190)
    private String payeeUpi;
    @Column(name = "payee_phone", length = 20)
    private String payeePhone;
    @Column(length = 60)
    private String bank;
    @Column(name = "paid_at", nullable = false)
    private Instant paidAt;
    @Column(columnDefinition = "TEXT")
    private String narrative;
    @Column(nullable = false, length = 2)
    private String lang = "en";
    @Column(nullable = false, length = 10)
    private String status = OPEN;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "reported_at")
    private Instant reportedAt;
}
