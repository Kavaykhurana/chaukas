package in.chaukas.core;

import java.math.BigDecimal;
import java.util.List;

/** Plain value types shared by the rule engine, the AI layer and the verdict merger. */
public final class Models {
    private Models() {}

    public enum Risk { HIGH, MEDIUM, LOW }

    /** One red flag: the exact phrase found in the message and why it matters. */
    public record Signal(String id, String phrase, String reason, int weight) {}

    /** A known-bad identifier that already exists in the community scam-intel store. */
    public record Hit(String kind, String value, int reports, boolean confirmed) {}

    /** Result of the deterministic rule pass (step 1). */
    public record RuleResult(int score, String scamType, List<Signal> signals) {}

    /** What Gemini returned (step 3). Empty when AI is disabled or failed. */
    public record AiResult(String scamType, double confidence, List<String> phrases,
                           String explanation, String extractedText) {
        public static AiResult none() {
            return new AiResult(null, 0, List.of(), null, null);
        }
        public boolean present() {
            return scamType != null || (extractedText != null && !extractedText.isBlank());
        }
    }

    /** Everything the identifier extractors can pull out of free text. */
    public record Identifiers(List<String> urls, List<String> domains, List<String> upiIds,
                              List<String> phones) {
        public boolean isEmpty() {
            return urls.isEmpty() && upiIds.isEmpty() && phones.isEmpty();
        }
    }

    /** Facts needed for a golden-hour report. Any field may be null if it could not be found. */
    public record Facts(BigDecimal amount, String utr, String payeeUpi, String payeePhone,
                        String bank, String paidAtText, String scamType) {}

    /** The final merged answer shown to the user (step 4). */
    public record Verdict(Risk risk, String scamType, String scamLabel, int score,
                          String headline, String advice, List<Signal> signals,
                          List<Hit> seenBefore, boolean aiUsed, String aiExplanation,
                          Double aiConfidence, String lang) {}

    /** Drafts a victim can send, generated from the extracted facts. */
    public record Kit(String complaintText, String callScript, String bankEmailSubject,
                      String bankEmailBody, String scammerReport, List<String> checklist) {}
}
