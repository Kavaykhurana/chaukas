package in.chaukas.service;

import in.chaukas.config.ApiException;
import in.chaukas.config.AppProps;
import in.chaukas.core.Extractor;
import in.chaukas.core.KitGenerator;
import in.chaukas.core.Lang;
import in.chaukas.core.Models.Facts;
import in.chaukas.core.Models.Identifiers;
import in.chaukas.core.Models.Kit;
import in.chaukas.core.RuleEngine;
import in.chaukas.domain.AppUser;
import in.chaukas.domain.Incident;
import in.chaukas.repo.IncidentRepository;
import in.chaukas.web.Dto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Mode 2: "I've been scammed". Pulls amount, UTR, payee and time out of the victim's chat and payment screenshots,
 * starts the golden-hour countdown and prepares the drafts. Nothing is ever filed automatically: the victim sends
 * every draft themselves, and "mark reported" only records that they did.
 */
@Service
public class IncidentService {
    private final IncidentRepository incidents;
    private final AuthService auth;
    private final AiAnalyzer ai;
    private final IntelService intel;
    private final AppProps props;

    public IncidentService(IncidentRepository incidents, AuthService auth, AiAnalyzer ai, IntelService intel, AppProps props) {
        this.incidents = incidents;
        this.auth = auth;
        this.ai = ai;
        this.intel = intel;
        this.props = props;
    }

    /** One image per payment/chat screenshot; their OCR text is appended to what the victim typed. */
    @Transactional
    public Dto.IncidentView create(Long userId, String narrative, List<byte[]> images, List<String> mimes,
                                   String langCode, ZonedDateTime now) {
        AppUser user = auth.require(userId);
        Lang lang = Lang.parse(langCode != null ? langCode : user.getPreferredLang());

        StringBuilder all = new StringBuilder();
        if (narrative != null && !narrative.isBlank()) all.append(narrative.trim());
        for (int i = 0; i < images.size(); i++) {
            String ocr = ai.readText(images.get(i), mimes.get(i));
            if (ocr != null && !ocr.isBlank()) all.append('\n').append(ocr.trim());
        }
        String text = all.toString();
        if (text.isBlank()) {
            throw ApiException.badRequest("Describe what happened or upload the payment screenshot so I can find the amount and UTR.");
        }

        Identifiers ids = Extractor.identifiers(text);
        Incident inc = new Incident();
        inc.setUserId(userId);
        inc.setAmount(Extractor.amount(text).orElse(null));
        inc.setUtr(Extractor.utr(text).orElse(null));
        inc.setPayeeUpi(ids.upiIds().isEmpty() ? null : ids.upiIds().get(0));
        inc.setPayeePhone(ids.phones().isEmpty() ? null : ids.phones().get(0));
        inc.setBank(Extractor.bank(text).orElse(null));
        ZonedDateTime paidAt = Extractor.paidAt(text, now).orElse(now);
        inc.setPaidAt(paidAt.toInstant());
        inc.setNarrative(text.length() > 4000 ? text.substring(0, 4000) : text);
        inc.setLang(lang.code());
        incidents.save(inc);
        return view(inc, user, now);
    }

    @Transactional(readOnly = true)
    public Dto.IncidentView get(Long userId, Long id, ZonedDateTime now) {
        return view(owned(userId, id), auth.require(userId), now);
    }

    @Transactional(readOnly = true)
    public List<Dto.IncidentView> list(Long userId, ZonedDateTime now) {
        AppUser user = auth.require(userId);
        return incidents.findByUserIdOrderByCreatedAtDesc(userId).stream().map(i -> view(i, user, now)).toList();
    }

    /** Records that the victim filed the complaint. Optionally feeds the payee into community intel (confirmed). */
    @Transactional
    public Dto.IncidentView markReported(Long userId, Long id, boolean share, ZonedDateTime now) {
        Incident inc = owned(userId, id);
        if (!Incident.REPORTED.equals(inc.getStatus())) {
            inc.setStatus(Incident.REPORTED);
            inc.setReportedAt(Instant.now());
            incidents.save(inc);
            if (share) {
                if (inc.getPayeeUpi() != null) intel.report(IntelService.UPI, inc.getPayeeUpi(), true);
                if (inc.getPayeePhone() != null) intel.report(IntelService.PHONE, inc.getPayeePhone(), true);
            }
        }
        return view(inc, auth.require(userId), now);
    }

    private Incident owned(Long userId, Long id) {
        Incident i = incidents.findById(id).orElseThrow(() -> ApiException.notFound("Incident not found."));
        if (!i.getUserId().equals(userId)) throw ApiException.forbidden("This incident belongs to another account.");
        return i;
    }

    private Dto.IncidentView view(Incident i, AppUser user, ZonedDateTime now) {
        ZonedDateTime paidAt = i.getPaidAt().atZone(Extractor.IST);
        long elapsed = Math.max(0, Duration.between(paidAt, now).toMinutes());
        long left = Math.max(0, props.goldenHourMinutes() - elapsed);
        String type = RuleEngine.run(i.getNarrative() == null ? "" : i.getNarrative(), false).scamType();
        Facts facts = new Facts(i.getAmount(), i.getUtr(), i.getPayeeUpi(), i.getPayeePhone(), i.getBank(), null, type);
        Lang lang = Lang.parse(i.getLang());
        Kit kit = KitGenerator.build(user.getName(), facts, paidAt, i.getNarrative(), lang);

        List<String> missing = new ArrayList<>();
        if (i.getAmount() == null) missing.add("amount");
        if (i.getUtr() == null) missing.add("utr");
        if (i.getPayeeUpi() == null && i.getPayeePhone() == null) missing.add("payee");
        if (i.getBank() == null) missing.add("bank");

        Dto.IncidentFacts f = new Dto.IncidentFacts(i.getAmount(), i.getUtr(), i.getPayeeUpi(), i.getPayeePhone(),
            i.getBank(), i.getPaidAt(), type);
        return Dto.IncidentView.of(i, f, left, elapsed, left > 0, missing, kit);
    }
}
