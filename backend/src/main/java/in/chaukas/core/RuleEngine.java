package in.chaukas.core;

import in.chaukas.core.Models.RuleResult;
import in.chaukas.core.Models.Signal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Step 1 of the check pipeline: fast, deterministic, explainable. Every signal carries the exact
 * phrase it matched so the UI can highlight it. English, romanised Hindi, Devanagari and Gurmukhi
 * patterns live side by side.
 */
public final class RuleEngine {

    private record Rule(String id, String scamType, int weight, Pattern pattern) {}

    private static Rule r(String id, String type, int weight, String regex) {
        return new Rule(id, type, weight, Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
    }

    private static final List<Rule> RULES = List.of(
        r("urgency", "GENERIC", 18,
            "\\b(today|immediately|urgent(?:ly)?|right now|act now|final notice|last chance|within \\d+ ?(?:hours?|hrs?|minutes?|mins?)|expires? (?:today|soon|tonight)|turant|abhi)\\b|तुरंत|आज ही|अभी|ਤੁਰੰਤ|ਅੱਜ ਹੀ"),
        r("threat", "GENERIC", 22,
            "\\b(?:(?:will be|has been|is being|gets?) )?(?:blocked|suspended|frozen|deactivated|closed|disconnected|cancelled|seized) (?:today|tonight|immediately|soon)\\b|\\b(?:will be|has been|is being|gets?) (?:blocked|suspended|frozen|deactivated|closed|disconnected|cancelled|seized)\\b|\\b(?:band ho jayega|block ho jayega)\\b|ब्लॉक|बंद हो जाएगा|ਬਲੌਕ|ਬੰਦ ਹੋ ਜਾਵੇਗਾ"),
        r("kyc", "FAKE_KYC", 32,
            "\\bkyc\\b|\\b(?:pan|aadhaar|aadhar) (?:card )?(?:update|verification|verify|link(?:ing)?|expired?)\\b|\\b(?:update|verify|complete) (?:your )?(?:bank |account )?details\\b|केवाईसी|ਕੇਵਾਈਸੀ"),
        r("authority", "DIGITAL_ARREST", 36,
            "\\bdigital arrest\\b|\\b(?:cbi|ncb|narcotics|enforcement directorate|cyber ?crime (?:branch|cell)|trai|customs (?:officer|department)|supreme court|arrest warrant|money laundering|police (?:officer|inspector|station|case))\\b|\\b(?:fir|case) (?:has been )?(?:registered|filed) against you\\b|गिरफ्तार|सीबीआई|डिजिटल अरेस्ट|ਗ੍ਰਿਫ਼ਤਾਰ|ਗ੍ਰਿਫਤਾਰ|ਡਿਜੀਟਲ ਅਰੈਸਟ"),
        r("secrecy", "DIGITAL_ARREST", 24,
            "\\bdo(?:n'?t| not) (?:tell|inform|share (?:this )?with) (?:anyone|anybody|family|your bank|police)\\b|\\bdo(?:n'?t| not) disconnect\\b|\\bstay on (?:the )?(?:call|line|video)\\b|\\bkeep (?:this )?(?:confidential|secret)\\b|किसी को न बताएं|ਕਿਸੇ ਨੂੰ ਨਾ ਦੱਸੋ"),
        r("collect", "COLLECT_REQUEST", 50,
            "\\bcollect request\\b|\\bapprove (?:the |this )?(?:collect )?request\\b|\\benter (?:your )?(?:upi )?pin to (?:receive|get|claim)\\b|\\bpin to (?:receive|get)\\b|\\bscan (?:this )?(?:qr|code) to (?:receive|get|claim)\\b|कलेक्ट रिक्वेस्ट|ਕਲੈਕਟ ਰਿਕਵੈਸਟ"),
        r("investment", "INVESTMENT_FRAUD", 32,
            "\\bguaranteed (?:returns?|profit)\\b|\\bdouble your (?:money|investment)\\b|\\b\\d{2,3} ?% (?:daily|weekly|monthly|returns?|profit)\\b|\\b(?:stock|trading|ipo) (?:tips?|signals?|allotment)\\b|\\bvip (?:group|channel)\\b|\\b(?:crypto|bitcoin|usdt) (?:bonus|giveaway|investment)\\b|\\binsider (?:tip|info)\\b|गारंटीशुदा|ਪੱਕਾ ਮੁਨਾਫ਼ਾ"),
        r("task", "TASK_JOB", 32,
            "\\bpart[- ]time (?:job|work)\\b|\\bwork from home\\b|\\bearn (?:₹|rs\\.?|inr)? ?\\d[\\d,]*\\s?(?:per|a|/) ?(?:day|hour|task)\\b|\\b(?:like|subscribe|rate) (?:and|&) (?:subscribe|share|review)\\b|\\bprepaid task\\b|\\btask (?:bonus|commission)\\b|\\bcommission per task\\b|\\bjoin (?:our )?telegram\\b|घर बैठे कमाएं|ਘਰ ਬੈਠੇ ਕਮਾਓ"),
        r("prize", "PRIZE", 30,
            "\\bcongratulations\\b|\\byou(?:'ve| have)? (?:won|been selected)\\b|\\blottery\\b|\\blucky draw\\b|\\bkbc\\b|\\bclaim (?:your )?(?:prize|reward|gift|cashback)\\b|\\breward points? (?:expire|worth|will expire)\\b|इनाम जीता|लॉटरी|ਇਨਾਮ ਜਿੱਤਿਆ|ਲਾਟਰੀ"),
        r("parcel", "PARCEL", 26,
            "\\bparcel\\b|\\bcourier\\b|\\bfedex\\b|\\bdhl\\b|\\bcustoms (?:duty|fee|charges?)\\b|\\bpackage (?:is )?(?:held|seized|stuck)\\b|\\bdelivery (?:failed|attempt)\\b|\\billegal (?:items?|goods|drugs?)\\b|पार्सल|कूरियर|ਪਾਰਸਲ|ਕੂਰੀਅਰ"),
        r("credential", "OTP_PIN", 34,
            "\\botp\\b|\\bcvv\\b|\\bshare (?:your )?(?:pin|password|card number)\\b|\\bcard (?:number|expiry)\\b|ओटीपी|ਓਟੀਪੀ"),
        r("bill", "BILL", 30,
            "\\belectricity (?:bill|connection|supply)\\b|\\bpower (?:supply )?(?:will be|to be|is being) (?:cut|disconnected)\\b|\\bbijli\\b|\\bgas (?:connection|bill)\\b|बिजली|ਬਿਜਲੀ"),
        r("refund", "UPI_REFUND", 28,
            "\\brefund (?:is )?(?:pending|initiated|failed|processing)\\b|\\bexcess (?:amount|payment) (?:sent|credited)\\b|\\b(?:sent|transferred) (?:you )?(?:₹|rs\\.?)? ?\\d[\\d,]* by mistake\\b|\\bwrong (?:transfer|payment|number)\\b|रिफंड|ਰਿਫੰਡ"));

    private static final Pattern LINK_ACTION = Pattern.compile(
        "(?i)\\b(?:click|tap|open|visit|login|log in|update|verify|download|install|pay)\\b[^\\n]{0,60}", Pattern.UNICODE_CASE);

    /** Runs every rule over {@code text}. {@code hasLink} lets the link-action rule fire only when a link exists. */
    public static RuleResult run(String text, boolean hasLink) {
        if (text == null || text.isBlank()) return new RuleResult(0, "NONE", List.of());

        List<Signal> signals = new ArrayList<>();
        Map<String, Integer> byType = new HashMap<>();
        int generic = 0;

        for (Rule rule : RULES) {
            Matcher m = rule.pattern().matcher(text);
            boolean fired = false;
            while (m.find()) {
                String phrase = m.group().trim();
                if (phrase.isEmpty()) continue;
                if (signals.stream().noneMatch(s -> s.phrase().equalsIgnoreCase(phrase))) {
                    signals.add(new Signal(rule.id(), phrase, "", rule.weight()));
                }
                fired = true;
            }
            if (fired) {
                if (rule.scamType().equals("GENERIC")) generic += rule.weight();
                else byType.merge(rule.scamType(), rule.weight(), Integer::sum);
            }
        }

        if (hasLink && LINK_ACTION.matcher(text).find()) {
            Matcher m = Pattern.compile("(?i)\\b(?:update|verify|click|login|log in|tap)\\b[^\\n.]{0,40}").matcher(text);
            if (m.find()) {
                signals.add(new Signal("link_action", m.group().trim(), "", 14));
                generic += 14;
            }
        }

        String best = "NONE";
        int bestScore = 0;
        for (Map.Entry<String, Integer> e : byType.entrySet()) {
            if (e.getValue() > bestScore) {
                best = e.getKey();
                bestScore = e.getValue();
            }
        }
        int total = Math.min(100, generic + byType.values().stream().mapToInt(Integer::intValue).sum());
        if (best.equals("NONE") && total >= 25) best = "GENERIC";
        return new RuleResult(total, best, signals);
    }
}
