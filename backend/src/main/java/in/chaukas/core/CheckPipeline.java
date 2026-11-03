package in.chaukas.core;

import in.chaukas.core.Models.AiResult;
import in.chaukas.core.Models.Hit;
import in.chaukas.core.Models.Identifiers;
import in.chaukas.core.Models.RuleResult;
import in.chaukas.core.Models.Signal;
import in.chaukas.core.Models.Verdict;

import java.util.List;
import java.util.function.Function;

/** Steps 1, 2 and 4 wired together with no framework: rules, community intel, then the merge. */
public final class CheckPipeline {
    private CheckPipeline() {}

    public record Output(Verdict verdict, Identifiers identifiers, String analysedText) {}

    /**
     * @param text        what the user typed or pasted (may be empty when only a screenshot was sent)
     * @param ocrText     text Gemini read from a screenshot (may be null)
     * @param ai          Gemini's opinion, or {@link AiResult#none()}
     * @param intelLookup resolves identifiers to previously reported ones
     */
    public static Output run(String text, String ocrText, AiResult ai,
                             Function<Identifiers, List<Hit>> intelLookup, Lang lang) {
        StringBuilder sb = new StringBuilder();
        if (text != null && !text.isBlank()) sb.append(text.trim());
        if (ocrText != null && !ocrText.isBlank()) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(ocrText.trim());
        }
        String combined = sb.toString();

        Identifiers ids = Extractor.identifiers(combined);
        RuleResult rules = RuleEngine.run(combined, !ids.urls().isEmpty());
        List<Signal> link = DomainAnalyzer.analyze(ids.urls());
        List<Hit> hits = ids.isEmpty() ? List.of() : intelLookup.apply(ids);

        Verdict v = VerdictMerger.merge(rules, link, hits, ai == null ? AiResult.none() : ai, combined, lang);
        return new Output(v, ids, combined);
    }
}
