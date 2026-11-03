package in.chaukas.service;

import in.chaukas.core.Models.Hit;
import in.chaukas.core.Models.Identifiers;
import in.chaukas.domain.ScamIntel;
import in.chaukas.repo.ScamIntelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** The community scam-intel store: identifiers people have reported, queried on every check. */
@Service
public class IntelService {
    public static final String UPI = "UPI";
    public static final String PHONE = "PHONE";
    public static final String DOMAIN = "DOMAIN";

    private final ScamIntelRepository repo;

    public IntelService(ScamIntelRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<Hit> lookup(Identifiers ids) {
        List<Hit> hits = new ArrayList<>();
        for (String u : ids.upiIds()) find(UPI, u, hits);
        for (String p : ids.phones()) find(PHONE, p, hits);
        for (String d : ids.domains()) find(DOMAIN, d, hits);
        return hits;
    }

    @Transactional(readOnly = true)
    public List<Hit> lookup(String kind, String value) {
        List<Hit> hits = new ArrayList<>();
        find(kind, value, hits);
        return hits;
    }

    private void find(String kind, String value, List<Hit> out) {
        String v = norm(kind, value);
        repo.findByKindAndValue(kind, v).ifPresent(i -> out.add(new Hit(kind, v, i.getReportCount(), i.isConfirmed())));
    }

    /** Adds one report. {@code confirmed} is set when it comes from a filed golden-hour incident. */
    @Transactional
    public void report(String kind, String value, boolean confirmed) {
        if (value == null || value.isBlank()) return;
        String v = norm(kind, value);
        ScamIntel i = repo.findByKindAndValue(kind, v).orElseGet(() -> {
            ScamIntel n = new ScamIntel();
            n.setKind(kind);
            n.setValue(v);
            return n;
        });
        i.setReportCount(i.getReportCount() + 1);
        i.setConfirmed(i.isConfirmed() || confirmed);
        i.setLastSeen(Instant.now());
        repo.save(i);
    }

    static String norm(String kind, String value) {
        String v = value.trim().toLowerCase(Locale.ROOT);
        if (PHONE.equals(kind)) {
            String d = v.replaceAll("\\D", "");
            return d.length() > 10 ? d.substring(d.length() - 10) : d;
        }
        return v;
    }
}
