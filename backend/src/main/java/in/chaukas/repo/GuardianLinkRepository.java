package in.chaukas.repo;

import in.chaukas.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuardianLinkRepository extends JpaRepository<GuardianLink, Long> {
    Optional<GuardianLink> findByInviteCode(String inviteCode);
    List<GuardianLink> findByWardIdOrderByCreatedAtDesc(Long wardId);
    List<GuardianLink> findByGuardianIdOrderByCreatedAtDesc(Long guardianId);
    List<GuardianLink> findByWardIdAndStatus(Long wardId, String status);
}
