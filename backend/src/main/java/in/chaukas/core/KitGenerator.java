package in.chaukas.core;

import in.chaukas.core.Models.Facts;
import in.chaukas.core.Models.Kit;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Golden-hour kit: turns extracted facts into the drafts a victim needs in the first hour.
 * Nothing here is filed automatically. The person reviews and submits each item themselves.
 */
public final class KitGenerator {
    private KitGenerator() {}

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH);

    public static Kit build(String victimName, Facts f, ZonedDateTime paidAt, String narrative, Lang lang) {
        String name = blank(victimName) ? "the complainant" : victimName.trim();
        String amount = money(f.amount());
        String when = paidAt == null ? "[time not found, please add]" : paidAt.withZoneSameInstant(Extractor.IST).format(WHEN) + " IST";
        String utr = orMissing(f.utr(), "[UTR not found, please add]");
        String payee = payee(f);
        String bank = blank(f.bank()) ? "[your bank]" : f.bank();

        String complaint = """
            Category: Online Financial Fraud (UPI / bank transfer)

            I, %s, was cheated into transferring money through UPI.

            Amount lost: %s
            Date and time of transaction: %s
            Transaction reference (UTR): %s
            Money was sent to: %s
            My bank: %s

            What happened:
            %s

            I request that the receiving account be put on hold and the amount be frozen, and that the matter be investigated. I have the chat and payment screenshots and can share them on request.
            """.formatted(name, amount, when, utr, payee, bank,
            blank(narrative) ? "[Describe in 3-4 lines how the person contacted you and what they asked you to do.]" : narrative.trim());

        String script = callScript(lang, name, amount, when, utr, payee, bank);

        String subject = "URGENT: Request to freeze fraudulent UPI transaction, UTR " + (blank(f.utr()) ? "[add UTR]" : f.utr());
        String body = """
            To the Nodal Officer / Customer Care,

            I am writing about a fraudulent UPI transaction from my account, made under deception.

            Account holder: %s
            Amount: %s
            Date and time: %s
            UTR / reference: %s
            Beneficiary: %s

            I have already called the National Cyber Crime Helpline 1930 and am filing a complaint at cybercrime.gov.in (acknowledgement number: [add once you have it]).

            Please place a hold on the beneficiary account, freeze the amount, and confirm in writing. I am also requesting you to block further debit from my account to the same beneficiary.

            Regards,
            %s
            """.formatted(name, amount, when, utr, payee, name);

        String scammer = blank(f.payeeUpi()) && blank(f.payeePhone())
            ? ""
            : "Scammer identifiers to report to the portal and your bank: "
            + String.join(", ", nonBlank(f.payeeUpi(), f.payeePhone())) + ".";

        List<String> checklist = new ArrayList<>();
        checklist.add("Call 1930 now and read the script. Keep the UTR in front of you.");
        checklist.add("File the complaint at cybercrime.gov.in and note the acknowledgement number.");
        checklist.add("Email your bank's nodal officer the freeze request and attach the screenshots.");
        checklist.add("Stop all contact with the scammer. Do not send any 'recovery' or 'release' fee.");
        checklist.add("Change UPI PIN and banking passwords if you shared any details.");

        return new Kit(complaint, script, subject, body, scammer, checklist);
    }

    private static String callScript(Lang lang, String name, String amount, String when, String utr, String payee, String bank) {
        return switch (lang) {
            case HI -> """
                नमस्ते, मैं %s बोल रहा/रही हूँ। मैं एक साइबर ठगी की रिपोर्ट करना चाहता/चाहती हूँ।
                मेरे बैंक खाते (%s) से %s का UPI ट्रांज़ैक्शन ठगी से कराया गया है।
                समय: %s। UTR नंबर: %s।
                पैसे इस खाते/UPI में गए हैं: %s।
                कृपया इस ट्रांज़ैक्शन को तुरंत होल्ड करवाएं और मेरी शिकायत दर्ज करें। मुझे शिकायत नंबर चाहिए।
                """.formatted(name, bank, amount, when, utr, payee);
            case PA -> """
                ਸਤ ਸ੍ਰੀ ਅਕਾਲ, ਮੈਂ %s ਬੋਲ ਰਿਹਾ/ਰਹੀ ਹਾਂ। ਮੈਂ ਇੱਕ ਸਾਈਬਰ ਠੱਗੀ ਦੀ ਰਿਪੋਰਟ ਕਰਨੀ ਚਾਹੁੰਦਾ/ਚਾਹੁੰਦੀ ਹਾਂ।
                ਮੇਰੇ ਬੈਂਕ ਖਾਤੇ (%s) ਵਿੱਚੋਂ %s ਦਾ UPI ਲੈਣ-ਦੇਣ ਠੱਗੀ ਨਾਲ ਕਰਵਾਇਆ ਗਿਆ ਹੈ।
                ਸਮਾਂ: %s। UTR ਨੰਬਰ: %s।
                ਪੈਸੇ ਇਸ ਖਾਤੇ/UPI ਵਿੱਚ ਗਏ ਹਨ: %s।
                ਕਿਰਪਾ ਕਰਕੇ ਇਸ ਲੈਣ-ਦੇਣ ਨੂੰ ਤੁਰੰਤ ਹੋਲਡ ਕਰਵਾਓ ਅਤੇ ਮੇਰੀ ਸ਼ਿਕਾਇਤ ਦਰਜ ਕਰੋ। ਮੈਨੂੰ ਸ਼ਿਕਾਇਤ ਨੰਬਰ ਚਾਹੀਦਾ ਹੈ।
                """.formatted(name, bank, amount, when, utr, payee);
            default -> """
                Hello, my name is %s. I want to report a cyber fraud.
                An amount of %s was taken from my account at %s through a UPI payment made under deception.
                Time: %s. UTR number: %s.
                The money went to: %s.
                Please put this transaction on hold and register my complaint. I need the complaint number.
                """.formatted(name, amount, bank, when, utr, payee);
        };
    }

    private static String payee(Facts f) {
        List<String> parts = nonBlank(f.payeeUpi(), f.payeePhone());
        return parts.isEmpty() ? "[payee UPI ID or number not found, please add]" : String.join(" / ", parts);
    }

    private static List<String> nonBlank(String... v) {
        List<String> out = new ArrayList<>();
        for (String s : v) if (!blank(s)) out.add(s);
        return out;
    }

    private static String money(BigDecimal a) {
        if (a == null) return "[amount not found, please add]";
        return "₹" + String.format(Locale.forLanguageTag("en-IN"), "%,.2f", a).replace(".00", "");
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static String orMissing(String v, String missing) {
        return blank(v) ? missing : v;
    }
}
