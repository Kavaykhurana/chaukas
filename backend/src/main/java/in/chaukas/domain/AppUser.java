package in.chaukas.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "app_user")
@Getter @Setter @NoArgsConstructor
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 190)
    private String email;
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(name = "preferred_lang", nullable = false, length = 2)
    private String preferredLang = "en";
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
