package in.chaukas.core;

import in.chaukas.core.Models.AiResult;
import in.chaukas.core.Models.Hit;
import in.chaukas.core.Models.Risk;
import in.chaukas.core.Models.RuleResult;
import in.chaukas.core.Models.Signal;
import in.chaukas.core.Models.Verdict;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Step 4: combines rules, link analysis, community intel and (optionally) Gemini into one verdict.
 * Hard guarantees: the verdict is never "safe", only "no known red flags", and it still works
 * when the AI call is missing.
 */
public final class VerdictMerger {
    private VerdictMerger() {}

    public static final int HIGH_AT = 50;
    public static final int MEDIUM_AT = 25;

    public static Verdict merge(RuleResult rules, List<Signal> linkSignals, List<Hit> intel,
                                AiResult ai, String text, Lang lang) {
        int score = rules.score();
        List<Signal> signals = new ArrayList<>();
        for (Signal s : rules.signals()) signals.add(localise(s, lang));
        for (Signal s : linkSignals) {
            signals.add(localise(s, lang));
            score += s.weight();
        }

        String type = rules.scamType();

        for (Hit h : intel) {
            score += h.confirmed() || h.reports() >= 2 ? 45 : 20;
            signals.add(new Signal("intel", h.value(), Messages.reason("intel", lang), 0));
        }

        boolean aiUsed = false;
        String aiNote = null;
        Double aiConf = null;
        if (ai != null && ai.scamType() != null) {
            aiUsed = true;
            aiConf = ai.confidence();
            aiNote = ai.explanation();
            String aiType = normaliseType(ai.scamType());
            if (!aiType.equals("NONE")) {
                if (ai.confidence() >= 0.6) score += (int) Math.round(20 * ai.confidence());
                if (type.equals("NONE") || type.equals("GENERIC")) type = aiType;
            } else if (ai.confidence() >= 0.8 && score < MEDIUM_AT) {
                score = Math.max(0, score - 5); // AI agrees nothing is wrong; never goes below zero
            }
            // Phrases cited by the AI are highlighted only if they really appear in the text we saw.
            if (ai.phrases() != null) {
                for (String p : ai.phrases()) {
                    if (p == null || p.isBlank()) continue;
                    boolean dup = signals.stream().anyMatch(s -> s.phrase().equalsIgnoreCase(p.trim()));
                    boolean real = text != null && text.toLowerCase().contains(p.trim().toLowerCase());
                    if (!dup && real && !aiType(ai).equals("NONE")) {
                        signals.add(new Signal("ai", p.trim(), aiNote == null ? "" : aiNote, 0));
                    }
                }
            }
        }

        // Any link signal with no scam type from phrases still deserves a label.
        if (type.equals("NONE") && score >= MEDIUM_AT) type = "GENERIC";

        score = Math.min(100, score);
        Risk risk = score >= HIGH_AT ? Risk.HIGH : score >= MEDIUM_AT ? Risk.MEDIUM : Risk.LOW;
        if (risk == Risk.LOW) type = "NONE";

        return new Verdict(
            risk, type, Messages.label(type, lang), score,
            Messages.headline(risk, type, lang), Messages.advice(risk, lang),
            signals, intel, aiUsed, aiNote, aiConf, lang.code());
    }

    private static String aiType(AiResult ai) {
        return ai.scamType() == null ? "NONE" : normaliseType(ai.scamType());
    }

    private static Signal localise(Signal s, Lang lang) {
        return new Signal(s.id(), s.phrase(), Messages.reason(s.id(), lang), s.weight());
    }

    /** Maps whatever label Gemini returned onto the closed set the UI knows about. */
    public static String normaliseType(String raw) {
        if (raw == null) return "NONE";
        String v = raw.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        return switch (v) {
            case "FAKE_KYC", "KYC", "KYC_UPDATE" -> "FAKE_KYC";
            case "DIGITAL_ARREST", "ARREST", "IMPERSONATION" -> "DIGITAL_ARREST";
            case "COLLECT_REQUEST", "UPI_COLLECT", "COLLECT" -> "COLLECT_REQUEST";
            case "INVESTMENT_FRAUD", "INVESTMENT", "CRYPTO", "TRADING" -> "INVESTMENT_FRAUD";
            case "TASK_JOB", "JOB", "TASK", "PART_TIME_JOB" -> "TASK_JOB";
            case "PRIZE", "LOTTERY", "REWARD" -> "PRIZE";
            case "PARCEL", "COURIER", "CUSTOMS" -> "PARCEL";
            case "OTP_PIN", "OTP", "CREDENTIAL_THEFT", "PHISHING" -> "OTP_PIN";
            case "BILL", "ELECTRICITY_BILL", "UTILITY" -> "BILL";
            case "UPI_REFUND", "REFUND", "WRONG_TRANSFER" -> "UPI_REFUND";
            case "NONE", "SAFE", "NO_SCAM", "LEGIT", "BENIGN" -> "NONE";
            default -> "GENERIC";
        };
    }
}
