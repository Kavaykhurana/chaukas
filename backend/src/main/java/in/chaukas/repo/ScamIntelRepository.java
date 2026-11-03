package in.chaukas.repo;

import in.chaukas.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScamIntelRepository extends JpaRepository<ScamIntel, Long> {
    Optional<ScamIntel> findByKindAndValue(String kind, String value);
}
