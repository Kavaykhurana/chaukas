package in.chaukas.web;

import in.chaukas.service.GuardianService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/guardians")
public class GuardianController {
    private final GuardianService guardians;

    public GuardianController(GuardianService guardians) {
        this.guardians = guardians;
    }

    @GetMapping
    Dto.Family family(Authentication a) {
        return guardians.family((Long) a.getPrincipal());
    }

    @PostMapping("/invites")
    Dto.InviteView invite(Authentication a) {
        return guardians.createInvite((Long) a.getPrincipal());
    }

    @PostMapping("/accept")
    Dto.LinkView accept(Authentication a, @Valid @RequestBody Dto.AcceptInviteRequest r) {
        return guardians.accept((Long) a.getPrincipal(), r.code());
    }

    @GetMapping("/alerts")
    List<Dto.AlertView> alerts(Authentication a) {
        return guardians.inbox((Long) a.getPrincipal());
    }

    @GetMapping("/alerts/unread-count")
    Map<String, Long> unread(Authentication a) {
        return Map.of("unread", guardians.unread((Long) a.getPrincipal()));
    }

    @PostMapping("/alerts/read")
    Map<String, Boolean> read(Authentication a) {
        guardians.markAllRead((Long) a.getPrincipal());
        return Map.of("ok", true);
    }
}
