package in.chaukas.repo;

import in.chaukas.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuardianAlertRepository extends JpaRepository<GuardianAlert, Long> {
    List<GuardianAlert> findTop50ByGuardianIdOrderByCreatedAtDesc(Long guardianId);
    long countByGuardianIdAndReadFalse(Long guardianId);
}
