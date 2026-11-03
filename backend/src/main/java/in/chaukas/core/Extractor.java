package in.chaukas.core;

import in.chaukas.core.Models.Identifiers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Pulls identifiers (links, UPI IDs, phone numbers) and payment facts out of free text. */
public final class Extractor {
    private Extractor() {}

    public static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private static final Set<String> EMAIL_DOMAINS = Set.of(
        "gmail", "yahoo", "outlook", "hotmail", "rediffmail", "proton", "icloud", "live", "ymail");

    private static final Pattern EMAIL = Pattern.compile(
        "(?i)[a-z0-9._%+-]+@[a-z0-9-]+(?:\\.[a-z0-9-]+)+");
    private static final Pattern UPI = Pattern.compile(
        "(?i)(?<![a-z0-9._-])[a-z0-9][a-z0-9._-]{1,40}@[a-z][a-z0-9]{1,20}(?![a-z0-9@-]|\\.[a-z0-9])");
    private static final Pattern URL = Pattern.compile(
        "(?i)(?<![a-z0-9@.-])(?:https?://)?(?:www\\.)?[a-z0-9][a-z0-9-]*(?:\\.[a-z0-9-]+)*\\.(?:com|in|net|org|xyz|top|click|live|icu|buzz|shop|site|online|vip|cc|help|info|app|link|me|ly|gl|page|biz|co|io|gov|edu|tk|ml|ga|cf|gq|ws|work|support|store|today|fun|rest|cfd|sbs|lol|pw)\\b(?:/[^\\s,;)\"']*)?");
    private static final Pattern PHONE = Pattern.compile(
        "(?<![\\d])(?:\\+?91[\\s-]?)?[6-9]\\d{4}[\\s-]?\\d{5}(?![\\d])");

    public static Identifiers identifiers(String text) {
        if (text == null) text = "";
        String work = EMAIL_PRUNE(text);

        List<String> upis = new ArrayList<>();
        Matcher m = UPI.matcher(work);
        while (m.find()) {
            String v = m.group().toLowerCase();
            String handle = v.substring(v.indexOf('@') + 1);
            if (!EMAIL_DOMAINS.contains(handle) && !upis.contains(v)) upis.add(v);
        }
        String noUpi = UPI.matcher(work).replaceAll(" ");

        List<String> urls = new ArrayList<>();
        Set<String> domains = new LinkedHashSet<>();
        Matcher u = URL.matcher(noUpi);
        while (u.find()) {
            String raw = u.group().replaceAll("[.!?]+$", "");
            String host = hostOf(raw);
            if (host.isEmpty() || host.matches("^\\d+(\\.\\d+)?$")) continue;
            urls.add(raw);
            domains.add(host);
        }

        List<String> phones = new ArrayList<>();
        Matcher p = PHONE.matcher(text);
        while (p.find()) {
            String digits = p.group().replaceAll("\\D", "");
            if (digits.length() > 10) digits = digits.substring(digits.length() - 10);
            if (!phones.contains(digits)) phones.add(digits);
        }
        return new Identifiers(urls, new ArrayList<>(domains), upis, phones);
    }

    private static String EMAIL_PRUNE(String s) {
        return EMAIL.matcher(s).replaceAll(" ");
    }

    public static String hostOf(String urlLike) {
        String s = urlLike.toLowerCase().replaceFirst("^https?://", "");
        int cut = s.length();
        for (char c : new char[] {'/', '?', '#', ':'}) {
            int i = s.indexOf(c);
            if (i >= 0 && i < cut) cut = i;
        }
        s = s.substring(0, cut);
        return s.startsWith("www.") ? s.substring(4) : s;
    }

    // ---- payment facts ----------------------------------------------------------------------

    private static final Pattern AMOUNT = Pattern.compile(
        "(?i)(?:₹|rs\\.?|inr)\\s?([\\d,]+(?:\\.\\d{1,2})?)|([\\d,]+(?:\\.\\d{1,2})?)\\s?(?:rupees|rs\\b|/-)");
    private static final Pattern UTR_LABELLED = Pattern.compile(
        "(?i)(?:utr|upi ref(?:erence)?(?: no\\.?| number)?|ref(?:erence)?(?: no\\.?| number)?|txn(?: id)?|transaction (?:id|no\\.?))\\D{0,8}(\\d{12})");
    private static final Pattern UTR_BARE = Pattern.compile("(?<!\\d)(\\d{12})(?!\\d)");
    private static final Pattern CLOCK = Pattern.compile(
        "(?i)(?<!\\d)(\\d{1,2}):(\\d{2})(?::\\d{2})?\\s?(am|pm)?");

    /** Prefers the amount sitting next to a payment verb; otherwise the largest amount. */
    public static Optional<BigDecimal> amount(String text) {
        if (text == null) return Optional.empty();
        BigDecimal best = null;
        BigDecimal nearVerb = null;
        Matcher m = AMOUNT.matcher(text);
        while (m.find()) {
            String g = m.group(1) != null ? m.group(1) : m.group(2);
            BigDecimal v;
            try {
                v = new BigDecimal(g.replace(",", ""));
            } catch (NumberFormatException e) {
                continue;
            }
            if (v.signum() <= 0) continue;
            int from = Math.max(0, m.start() - 40);
            String ctx = text.substring(from, Math.min(text.length(), m.end() + 20)).toLowerCase();
            if (ctx.matches("(?s).*(paid|debited|sent|transferred|deducted|payment of|transaction of|lost).*")) {
                if (nearVerb == null || v.compareTo(nearVerb) > 0) nearVerb = v;
            }
            if (best == null || v.compareTo(best) > 0) best = v;
        }
        return Optional.ofNullable(nearVerb != null ? nearVerb : best);
    }

    public static Optional<String> utr(String text) {
        if (text == null) return Optional.empty();
        Matcher m = UTR_LABELLED.matcher(text);
        if (m.find()) return Optional.of(m.group(1));
        Matcher b = UTR_BARE.matcher(text);
        if (b.find()) return Optional.of(b.group(1));
        return Optional.empty();
    }

    /**
     * Finds an HH:MM clock time in the text and anchors it to {@code now} (IST). A time more than
     * five minutes in the future is taken to be from yesterday.
     */
    public static Optional<ZonedDateTime> paidAt(String text, ZonedDateTime now) {
        if (text == null) return Optional.empty();
        Matcher m = CLOCK.matcher(text);
        ZonedDateTime found = null;
        while (m.find()) {
            int h = Integer.parseInt(m.group(1));
            int min = Integer.parseInt(m.group(2));
            String ap = m.group(3);
            if (min > 59) continue;
            if (ap != null) {
                if (h < 1 || h > 12) continue;
                h = h % 12 + (ap.equalsIgnoreCase("pm") ? 12 : 0);
            } else if (h > 23) {
                continue;
            }
            ZonedDateTime cand = ZonedDateTime.of(LocalDate.from(now.withZoneSameInstant(IST)), java.time.LocalTime.of(h, min), IST);
            if (cand.isAfter(now.plusMinutes(5))) cand = cand.minusDays(1);
            found = cand; // keep the last one: payment confirmations usually quote the latest time
        }
        return Optional.ofNullable(found);
    }

    private static final List<String> BANKS = List.of(
        "SBI", "State Bank of India", "HDFC", "ICICI", "Axis", "Kotak", "PNB", "Punjab National Bank",
        "Bank of Baroda", "Canara", "Union Bank", "IDFC", "Yes Bank", "IndusInd", "Paytm Payments Bank",
        "IDBI", "Bank of India", "Federal Bank", "AU Small Finance");

    public static Optional<String> bank(String text) {
        if (text == null) return Optional.empty();
        String lower = text.toLowerCase();
        for (String b : BANKS) {
            if (lower.matches("(?s).*\\b" + Pattern.quote(b.toLowerCase()) + "\\b.*")) return Optional.of(b);
        }
        return Optional.empty();
    }
}
