package in.chaukas.service;

import in.chaukas.config.ApiException;
import in.chaukas.domain.AppUser;
import in.chaukas.domain.GuardianAlert;
import in.chaukas.domain.GuardianLink;
import in.chaukas.repo.GuardianAlertRepository;
import in.chaukas.repo.GuardianLinkRepository;
import in.chaukas.repo.UserRepository;
import in.chaukas.web.Dto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

/**
 * Family guardian alerts. A ward creates an invite code, the guardian redeems it, and from then on every HIGH-risk
 * check by the ward drops an alert in the guardian's inbox. Alerts carry the scam type only, never the message.
 */
@Service
public class GuardianService {
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom rnd = new SecureRandom();
    private final GuardianLinkRepository links;
    private final GuardianAlertRepository alerts;
    private final UserRepository users;

    public GuardianService(GuardianLinkRepository links, GuardianAlertRepository alerts, UserRepository users) {
        this.links = links;
        this.alerts = alerts;
        this.users = users;
    }

    @Transactional
    public Dto.InviteView createInvite(Long wardId) {
        GuardianLink l = new GuardianLink();
        l.setWardId(wardId);
        l.setInviteCode(code());
        links.save(l);
        return new Dto.InviteView(l.getInviteCode());
    }

    @Transactional
    public Dto.LinkView accept(Long guardianId, String inviteCode) {
        GuardianLink l = links.findByInviteCode(inviteCode.trim().toUpperCase())
            .orElseThrow(() -> ApiException.notFound("Invite code not found."));
        if (!GuardianLink.PENDING.equals(l.getStatus())) throw ApiException.conflict("This invite was already used.");
        if (l.getWardId().equals(guardianId)) throw ApiException.badRequest("You cannot be your own guardian.");
        l.setGuardianId(guardianId);
        l.setStatus(GuardianLink.ACTIVE);
        links.save(l);
        return view(l);
    }

    @Transactional(readOnly = true)
    public Dto.Family family(Long userId) {
        List<Dto.LinkView> myGuardians = links.findByWardIdOrderByCreatedAtDesc(userId).stream().map(this::view).toList();
        List<Dto.LinkView> myWards = links.findByGuardianIdOrderByCreatedAtDesc(userId).stream().map(this::view).toList();
        return new Dto.Family(myGuardians, myWards);
    }

    @Transactional(readOnly = true)
    public List<Dto.AlertView> inbox(Long guardianId) {
        return alerts.findTop50ByGuardianIdOrderByCreatedAtDesc(guardianId).stream().map(Dto.AlertView::of).toList();
    }

    @Transactional(readOnly = true)
    public long unread(Long guardianId) {
        return alerts.countByGuardianIdAndReadFalse(guardianId);
    }

    @Transactional
    public void markAllRead(Long guardianId) {
        List<GuardianAlert> list = alerts.findTop50ByGuardianIdOrderByCreatedAtDesc(guardianId);
        list.forEach(a -> a.setRead(true));
        alerts.saveAll(list);
    }

    @Transactional
    public void notifyGuardians(AppUser ward, Long checkId, String scamType, String scamLabel) {
        for (GuardianLink l : links.findByWardIdAndStatus(ward.getId(), GuardianLink.ACTIVE)) {
            GuardianAlert a = new GuardianAlert();
            a.setGuardianId(l.getGuardianId());
            a.setWardId(ward.getId());
            a.setWardName(ward.getName());
            a.setCheckId(checkId);
            a.setScamType(scamType);
            a.setMessage(ward.getName() + " just checked a message that looks like: " + scamLabel
                + ". Call them before they pay or share anything.");
            alerts.save(a);
        }
    }

    private Dto.LinkView view(GuardianLink l) {
        String ward = users.findById(l.getWardId()).map(AppUser::getName).orElse("?");
        String guardian = l.getGuardianId() == null ? null : users.findById(l.getGuardianId()).map(AppUser::getName).orElse("?");
        return new Dto.LinkView(l.getId(), l.getInviteCode(), l.getStatus(), ward, guardian, l.getCreatedAt());
    }

    private String code() {
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 8; i++) sb.append(ALPHABET.charAt(rnd.nextInt(ALPHABET.length())));
            String c = sb.toString();
            if (links.findByInviteCode(c).isEmpty()) return c;
        }
        throw new IllegalStateException("Could not allocate invite code");
    }
}
