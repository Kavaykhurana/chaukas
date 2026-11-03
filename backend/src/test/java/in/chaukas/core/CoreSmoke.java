package in.chaukas.core;

import in.chaukas.core.Models.AiResult;
import in.chaukas.core.Models.Facts;
import in.chaukas.core.Models.Hit;
import in.chaukas.core.Models.Identifiers;
import in.chaukas.core.Models.Kit;
import in.chaukas.core.Models.Risk;
import in.chaukas.core.Models.Verdict;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Dependency-free test runner for the framework-free core. Run it with plain javac/java:
 * <pre>
 *   javac -d /tmp/core-out backend/src/main/java/in/chaukas/core/*.java backend/src/test/java/in/chaukas/core/CoreSmoke.java
 *   java -cp /tmp/core-out in.chaukas.core.CoreSmoke
 * </pre>
 * {@link CoreSmokeTest} runs the same checks under JUnit in the Maven build.
 */
public final class CoreSmoke {

    private static final List<String> FAILURES = new ArrayList<>();
    private static int checks = 0;

    private static void check(boolean ok, String what) {
        checks++;
        if (!ok) FAILURES.add(what);
        System.out.println((ok ? "  ok   " : "  FAIL ") + what);
    }

    private static CheckPipeline.Output run(String text, Lang lang) {
        return CheckPipeline.run(text, null, AiResult.none(), ids -> List.of(), lang);
    }

    public static List<String> runAll() {
        FAILURES.clear();
        checks = 0;

        System.out.println("# Mode 1: the deck's sample message");
        String kyc = "Dear customer, your bank KYC has expired. Your account will be blocked today. Update immediately at kycupdate-help.in/verify";
        Verdict v = run(kyc, Lang.EN).verdict();
        check(v.risk() == Risk.HIGH, "fake KYC message is HIGH risk (score " + v.score() + ")");
        check(v.scamType().equals("FAKE_KYC"), "scam type is FAKE_KYC, got " + v.scamType());
        List<String> phrases = v.signals().stream().map(s -> s.phrase().toLowerCase()).toList();
        check(phrases.stream().anyMatch(p -> p.contains("blocked today")), "cites 'blocked today'");
        check(phrases.stream().anyMatch(p -> p.contains("kycupdate-help.in")), "flags the look-alike domain");
        check(v.signals().stream().noneMatch(s -> s.reason().isBlank() && !s.id().equals("ai")), "every signal has a reason");
        check(v.headline().toLowerCase().contains("kyc"), "headline mentions KYC");

        System.out.println("# Other scam families");
        check(run("This is CBI. A narcotics parcel is in your name. You are under digital arrest. Do not disconnect the video call and don't tell anyone.", Lang.EN).verdict().scamType().equals("DIGITAL_ARREST"), "digital arrest detected");
        check(run("Approve the collect request and enter UPI PIN to receive Rs 5000 refund", Lang.EN).verdict().risk() == Risk.HIGH, "collect request is HIGH");
        check(run("Join our VIP group for guaranteed returns, 40% monthly profit", Lang.EN).verdict().scamType().equals("INVESTMENT_FRAUD"), "investment fraud detected");
        check(run("Part time job, work from home, earn Rs 5000 per day by task complete. Join telegram", Lang.EN).verdict().scamType().equals("TASK_JOB"), "task job detected");
        check(run("Your electricity connection will be disconnected tonight. Pay bill now: bit.ly/3xYz", Lang.EN).verdict().risk() == Risk.HIGH, "electricity threat with shortener is HIGH");

        System.out.println("# Multilingual");
        check(run("आपका KYC अपडेट नहीं है। खाता आज ही बंद हो जाएगा। तुरंत लिंक पर क्लिक करें kyc-sbi.top", Lang.HI).verdict().risk() == Risk.HIGH, "Hindi KYC message is HIGH");
        check(run("ਤੁਹਾਡਾ ਕੇਵਾਈਸੀ ਅੱਪਡੇਟ ਨਹੀਂ ਹੈ। ਖਾਤਾ ਅੱਜ ਹੀ ਬਲੌਕ ਹੋ ਜਾਵੇਗਾ।", Lang.PA).verdict().risk() != Risk.LOW, "Punjabi KYC message is not LOW");
        Verdict hi = run(kyc, Lang.HI).verdict();
        check(hi.headline().contains("KYC") && hi.headline().contains("लिंक"), "Hindi headline rendered");
        check(hi.signals().stream().anyMatch(s -> s.reason().contains("जल्दबाज़ी")), "Hindi signal reason rendered");

        System.out.println("# Never says 'safe'");
        Verdict ok = run("Hi, are we still meeting at 6 for dinner? Mom said she'll bring the cake.", Lang.EN).verdict();
        check(ok.risk() == Risk.LOW, "benign chat is LOW");
        check(ok.headline().toLowerCase().contains("no known red flags"), "LOW headline says 'no known red flags'");
        check(!ok.headline().toLowerCase().matches(".*\\bis safe\\b.*"), "LOW headline never claims safe");
        check(Messages.headline(Risk.LOW, "NONE", Lang.HI).contains("सुरक्षित नहीं"), "Hindi LOW headline also hedges");

        System.out.println("# Community intel");
        List<Hit> seen = List.of(new Hit("UPI", "kyc.help92@upi", 5, true));
        Verdict viaIntel = CheckPipeline.run("Pay the fee to kyc.help92@upi", null, AiResult.none(), ids -> ids.upiIds().contains("kyc.help92@upi") ? seen : List.of(), Lang.EN).verdict();
        check(viaIntel.risk() == Risk.HIGH && !viaIntel.seenBefore().isEmpty(), "known-bad UPI ID escalates to HIGH");

        System.out.println("# AI is optional and never overrides a clean read to 'safe'");
        AiResult ai = new AiResult("FAKE_KYC", 0.93, List.of("Update immediately"), "Classic KYC phishing.", null);
        Verdict withAi = CheckPipeline.run("Update immediately your account details", null, ai, i -> List.of(), Lang.EN).verdict();
        check(withAi.aiUsed() && withAi.scamType().equals("FAKE_KYC"), "AI type used when rules were only generic");
        AiResult liar = new AiResult("FAKE_KYC", 0.99, List.of("not in the text at all"), "x", null);
        Verdict noPhrase = CheckPipeline.run("see you at 6", null, liar, i -> List.of(), Lang.EN).verdict();
        check(noPhrase.signals().stream().noneMatch(s -> s.phrase().contains("not in the text")), "AI-cited phrase absent from the text is dropped");
        Verdict ocr = CheckPipeline.run("", "Your account will be blocked today. Complete KYC now", AiResult.none(), i -> List.of(), Lang.EN).verdict();
        check(ocr.risk() != Risk.LOW, "OCR text from a screenshot is scored by the rules");

        System.out.println("# Identifier extraction");
        Identifiers ids = Extractor.identifiers("Pay to kyc.help92@upi or call +91 98765 43210, or mail me at rahul@gmail.com. Site: https://sbi-kyc-update.xyz/login");
        check(ids.upiIds().equals(List.of("kyc.help92@upi")), "UPI ID found, email ignored: " + ids.upiIds());
        check(ids.phones().equals(List.of("9876543210")), "phone normalised to 10 digits: " + ids.phones());
        check(ids.domains().contains("sbi-kyc-update.xyz"), "domain found: " + ids.domains());
        check(Extractor.identifiers("Total Rs.500. See you e.g. tomorrow").domains().isEmpty(), "no false-positive domain from 'Rs.500' / 'e.g.'");
        check(Extractor.identifiers("pay to ramesh@oksbi.").upiIds().equals(List.of("ramesh@oksbi")), "UPI ID at sentence end");

        System.out.println("# Golden-hour facts (the deck's mock-up)");
        String chat = "Scammer: Pay the KYC fee ₹48,500 to kyc.help92@upi now\nYou: Done. Payment of ₹48,500 successful. UPI Ref No 436122879910 at 14:32 HDFC Bank";
        check(Extractor.amount(chat).orElseThrow().compareTo(new BigDecimal("48500")) == 0, "amount ₹48,500");
        check(Extractor.utr(chat).orElse("").equals("436122879910"), "UTR 436122879910");
        check(Extractor.bank(chat).orElse("").equals("HDFC"), "bank HDFC");
        ZonedDateTime now = ZonedDateTime.of(2026, 11, 3, 14, 51, 0, 0, ZoneId.of("Asia/Kolkata"));
        ZonedDateTime paid = Extractor.paidAt(chat, now).orElseThrow();
        check(paid.getHour() == 14 && paid.getMinute() == 32, "paid at 14:32");
        check(java.time.Duration.between(paid, now).toMinutes() == 19, "19 minutes elapsed -> 41 min left, as in the deck");
        check(Extractor.paidAt("at 11:50 pm", ZonedDateTime.of(2026, 11, 3, 0, 10, 0, 0, ZoneId.of("Asia/Kolkata"))).orElseThrow().getDayOfMonth() == 2, "a time later than now is taken as yesterday");

        System.out.println("# Kit");
        Facts f = new Facts(new BigDecimal("48500"), "436122879910", "kyc.help92@upi", null, "HDFC", null, "FAKE_KYC");
        Kit kit = KitGenerator.build("Asha Khurana", f, paid, "Got a message about KYC and paid the fee.", Lang.HI);
        check(kit.complaintText().contains("436122879910") && kit.complaintText().contains("₹48,500"), "complaint has UTR and amount");
        check(kit.callScript().contains("436122879910") && kit.callScript().contains("नमस्ते"), "1930 script is in Hindi and has the UTR");
        check(KitGenerator.build("A", f, paid, "", Lang.PA).callScript().contains("ਸਤ ਸ੍ਰੀ ਅਕਾਲ"), "1930 script in Punjabi");
        check(kit.bankEmailSubject().contains("436122879910") && kit.bankEmailBody().contains("1930"), "bank email has UTR and mentions 1930");
        check(kit.scammerReport().contains("kyc.help92@upi"), "scammer UPI is in the report");
        Kit sparse = KitGenerator.build(null, new Facts(null, null, null, null, null, null, null), null, null, Lang.EN);
        check(sparse.complaintText().contains("please add"), "missing facts are flagged, never invented");

        System.out.println("\n" + (checks - FAILURES.size()) + "/" + checks + " checks passed");
        return new ArrayList<>(FAILURES);
    }

    public static void main(String[] args) {
        List<String> failed = runAll();
        if (!failed.isEmpty()) {
            System.out.println("FAILED:");
            failed.forEach(x -> System.out.println(" - " + x));
            System.exit(1);
        }
    }
}
