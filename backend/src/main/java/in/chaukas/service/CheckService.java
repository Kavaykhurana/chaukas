package in.chaukas.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.chaukas.config.ApiException;
import in.chaukas.core.CheckPipeline;
import in.chaukas.core.Lang;
import in.chaukas.core.Models.AiResult;
import in.chaukas.core.Models.Risk;
import in.chaukas.domain.AppUser;
import in.chaukas.domain.CheckRecord;
import in.chaukas.repo.CheckRecordRepository;
import in.chaukas.repo.UserRepository;
import in.chaukas.web.Dto;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

/** Mode 1: paste or upload something suspicious, get a verdict with reasons. */
@Service
public class CheckService {
    public static final int MAX_TEXT = 6000;

    private final AiAnalyzer ai;
    private final IntelService intel;
    private final CheckRecordRepository records;
    private final UserRepository users;
    private final GuardianService guardians;
    private final ObjectMapper mapper = new ObjectMapper();

    public CheckService(AiAnalyzer ai, IntelService intel, CheckRecordRepository records,
                        UserRepository users, GuardianService guardians) {
        this.ai = ai;
        this.intel = intel;
        this.records = records;
        this.users = users;
        this.guardians = guardians;
    }

    public Dto.CheckResponse check(Long userId, String text, byte[] image, String mime, String langCode) {
        boolean hasText = text != null && !text.isBlank();
        boolean hasImage = image != null && image.length > 0;
        if (!hasText && !hasImage) throw ApiException.badRequest("Paste a message, a link, a UPI ID, or upload a screenshot.");
        if (hasText && text.length() > MAX_TEXT) throw ApiException.badRequest("That is too long. Paste up to " + MAX_TEXT + " characters.");
        if (hasImage && (mime == null || !mime.startsWith("image/"))) throw ApiException.badRequest("Only image files are supported.");

        AppUser user = userId == null ? null : users.findById(userId).orElse(null);
        Lang lang = Lang.parse(langCode != null ? langCode : user != null ? user.getPreferredLang() : "en");

        // Step 3 runs first so its OCR text feeds steps 1, 2 and 4. It is optional and bounded by a timeout.
        AiResult aiResult = ai.analyze(hasText ? text : "", image, mime, lang);

        CheckPipeline.Output out = CheckPipeline.run(text, aiResult.extractedText(), aiResult, intel::lookup, lang);
        var v = out.verdict();

        Long checkId = persist(userId, hasText, hasImage, out, v.risk(), v.scamType(), v.score(), v.aiUsed(), lang, text, image);

        if (user != null && v.risk() == Risk.HIGH) {
            guardians.notifyGuardians(user, checkId, v.scamType(), v.scamLabel());
        }
        return new Dto.CheckResponse(checkId, v, out.identifiers());
    }

    private Long persist(Long userId, boolean hasText, boolean hasImage, CheckPipeline.Output out, Risk risk,
                           String type, int score, boolean aiUsed, Lang lang, String text, byte[] image) {
        CheckRecord r = new CheckRecord();
        r.setUserId(userId);
        r.setInputKind(hasText && hasImage ? "TEXT_IMAGE" : hasImage ? "IMAGE" : "TEXT");
        r.setContentHash(sha256(out.analysedText(), image));
        r.setRisk(risk.name());
        r.setScamType(type);
        r.setScore(score);
        r.setAiUsed(aiUsed);
        r.setLang(lang.code());
        r.setIdentifiersJson(json(Map.of(
            "urls", out.identifiers().urls(), "domains", out.identifiers().domains(),
            "upiIds", out.identifiers().upiIds(), "phones", out.identifiers().phones())));
        return records.save(r).getId();
    }

    private String json(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (Exception e) {
            return "{}";
        }
    }

    private static String sha256(String text, byte[] image) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            if (text != null) md.update(text.getBytes(StandardCharsets.UTF_8));
            if (image != null) md.update(image);
            return HexFormat.of().formatHex(md.digest());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
