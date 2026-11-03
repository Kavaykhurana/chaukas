package in.chaukas.config;

import in.chaukas.domain.AppUser;
import in.chaukas.domain.GuardianLink;
import in.chaukas.repo.GuardianLinkRepository;
import in.chaukas.repo.UserRepository;
import in.chaukas.service.IntelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Demo-only data for judges: two linked accounts and a few previously reported identifiers so the
 * "seen before" path is visible. Controlled by SEED_DEMO and off by default in the prod profile.
 */
@Component
public class DemoDataSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    public static final String DEMO_PASSWORD = "chaukas-demo";

    private final AppProps props;
    private final UserRepository users;
    private final GuardianLinkRepository links;
    private final IntelService intel;
    private final PasswordEncoder encoder;

    public DemoDataSeeder(AppProps props, UserRepository users, GuardianLinkRepository links,
                          IntelService intel, PasswordEncoder encoder) {
        this.props = props;
        this.users = users;
        this.links = links;
        this.intel = intel;
        this.encoder = encoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!props.seedDemo() || users.findByEmailIgnoreCase("parent@demo.chaukas.in").isPresent()) return;

        AppUser parent = user("Sunita Sharma", "parent@demo.chaukas.in", "hi");
        AppUser child = user("Aarav Sharma", "child@demo.chaukas.in", "en");

        GuardianLink l = new GuardianLink();
        l.setWardId(parent.getId());
        l.setGuardianId(child.getId());
        l.setInviteCode("DEMO2026");
        l.setStatus(GuardianLink.ACTIVE);
        links.save(l);

        // Reports as if from earlier victims; clearly demo values on example-style handles.
        for (int i = 0; i < 3; i++) intel.report(IntelService.UPI, "sbi.kyc.helpdesk@oksbi", i == 2);
        intel.report(IntelService.UPI, "refund.support@ybl", true);
        intel.report(IntelService.DOMAIN, "sbi-kyc-update.xyz", true);
        intel.report(IntelService.PHONE, "9876501234", false);
        log.info("Demo data seeded. Logins: parent@demo.chaukas.in / child@demo.chaukas.in, password '{}'", DEMO_PASSWORD);
    }

    private AppUser user(String name, String email, String lang) {
        AppUser u = new AppUser();
        u.setName(name);
        u.setEmail(email);
        u.setPreferredLang(lang);
        u.setPasswordHash(encoder.encode(DEMO_PASSWORD));
        return users.save(u);
    }
}
