package in.chaukas.service;

import in.chaukas.core.Lang;
import in.chaukas.core.Models.AiResult;

/** Step 3 of the pipeline. Implementations must never throw: failure means {@link AiResult#none()}. */
public interface AiAnalyzer {
    AiResult analyze(String text, byte[] image, String mimeType, Lang lang);

    /** Reads text off a payment/chat screenshot only (used by the golden-hour flow). */
    default String readText(byte[] image, String mimeType) {
        return analyze("", image, mimeType, Lang.EN).extractedText();
    }
}
