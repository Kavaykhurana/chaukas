package in.chaukas.repo;

import in.chaukas.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckRecordRepository extends JpaRepository<CheckRecord, Long> {
}
