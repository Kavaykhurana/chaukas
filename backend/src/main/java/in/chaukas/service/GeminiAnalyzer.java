package in.chaukas.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.chaukas.config.AppProps;
import in.chaukas.core.Lang;
import in.chaukas.core.Models.AiResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Gemini through Spring AI (OpenAI-compatible endpoint). Returns structured JSON that is parsed defensively;
 * a missing key, timeout, HTTP error or malformed JSON all collapse to {@link AiResult#none()} so the rule
 * engine and community intel still produce a verdict.
 */
@Service
public class GeminiAnalyzer implements AiAnalyzer {
    private static final Logger log = LoggerFactory.getLogger(GeminiAnalyzer.class);

    private static final String SYSTEM = """
        You are a fraud analyst for Indian consumers. You are given a message, a screenshot, or both.
        Reply with ONE JSON object and nothing else, with exactly these keys:
        {"scamType": one of FAKE_KYC | DIGITAL_ARREST | COLLECT_REQUEST | INVESTMENT_FRAUD | TASK_JOB | PRIZE | PARCEL | OTP_PIN | BILL | UPI_REFUND | GENERIC | NONE,
         "confidence": number 0..1,
         "phrases": array of up to 6 short phrases copied VERBATIM from the content that indicate fraud,
         "explanation": one or two plain sentences, in the requested language, saying why,
         "extractedText": all readable text in the screenshot (empty string if none)}
        Rules: never invent phrases that are not in the content. Use NONE when nothing looks fraudulent.
        Never claim a message is safe; absence of red flags is not proof of safety.
        """;

    private final ObjectProvider<ChatClient.Builder> builder;
    private final ExecutorService executor;
    private final ObjectMapper mapper = new ObjectMapper();
    private final boolean enabled;
    private final int timeoutSeconds;
    private final boolean keyPresent;
    private volatile ChatClient client;

    public GeminiAnalyzer(ObjectProvider<ChatClient.Builder> builder, ExecutorService virtualExecutor,
                          AppProps props, @Value("${spring.ai.openai.api-key:}") String apiKey) {
        this.builder = builder;
        this.executor = virtualExecutor;
        this.enabled = props.ai().enabled();
        this.timeoutSeconds = props.ai().timeoutSeconds();
        this.keyPresent = apiKey != null && !apiKey.isBlank() && !apiKey.equals("not-set");
        if (enabled && !keyPresent) {
            log.warn("GEMINI_API_KEY is not set: AI step disabled, rules + community intel only.");
        }
    }

    public boolean available() {
        return enabled && keyPresent;
    }

    @Override
    public AiResult analyze(String text, byte[] image, String mimeType, Lang lang) {
        if (!available()) return AiResult.none();
        boolean hasText = text != null && !text.isBlank();
        boolean hasImage = image != null && image.length > 0;
        if (!hasText && !hasImage) return AiResult.none();

        Future<AiResult> f = executor.submit(() -> call(text, image, mimeType, lang));
        try {
            return f.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            f.cancel(true);
            log.warn("Gemini step skipped: {}", e.toString());
            return AiResult.none();
        }
    }

    private AiResult call(String text, byte[] image, String mimeType, Lang lang) throws Exception {
        ChatClient c = client();
        String instruction = "Write the explanation in " + switch (lang) {
            case HI -> "Hindi (Devanagari)";
            case PA -> "Punjabi (Gurmukhi)";
            default -> "English";
        } + ".\n\nMESSAGE:\n" + (text == null || text.isBlank() ? "(none, see screenshot)" : text);

        String raw = c.prompt()
            .system(SYSTEM)
            .user(u -> {
                u.text(instruction);
                if (image != null && image.length > 0) {
                    u.media(MimeType.valueOf(mimeType == null ? "image/png" : mimeType), new ByteArrayResource(image));
                }
            })
            .call()
            .content();
        return parse(raw);
    }

    private ChatClient client() {
        ChatClient c = client;
        if (c == null) {
            synchronized (this) {
                if (client == null) client = builder.getObject().build();
                c = client;
            }
        }
        return c;
    }

    /** Package-private for tests: tolerant of code fences and surrounding prose. */
    AiResult parse(String raw) {
        if (raw == null || raw.isBlank()) return AiResult.none();
        int a = raw.indexOf('{');
        int b = raw.lastIndexOf('}');
        if (a < 0 || b <= a) return AiResult.none();
        try {
            JsonNode n = mapper.readTree(raw.substring(a, b + 1));
            List<String> phrases = new ArrayList<>();
            if (n.path("phrases").isArray()) {
                n.path("phrases").forEach(p -> {
                    String s = p.asText("").trim();
                    if (!s.isEmpty() && phrases.size() < 6) phrases.add(s);
                });
            }
            String type = n.path("scamType").asText(null);
            double conf = Math.max(0, Math.min(1, n.path("confidence").asDouble(0)));
            String expl = n.path("explanation").asText(null);
            String ocr = n.path("extractedText").asText(null);
            return new AiResult(type, conf, phrases, expl, ocr);
        } catch (Exception e) {
            log.warn("Unparseable Gemini output ignored");
            return AiResult.none();
        }
    }
}
