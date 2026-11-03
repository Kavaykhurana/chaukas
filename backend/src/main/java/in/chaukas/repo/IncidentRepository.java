package in.chaukas.repo;

import in.chaukas.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByUserIdOrderByCreatedAtDesc(Long userId);
}
