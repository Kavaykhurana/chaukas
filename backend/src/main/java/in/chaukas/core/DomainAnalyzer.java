package in.chaukas.core;

import in.chaukas.core.Models.Signal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Looks only at the link itself: shorteners, throwaway endings, IP hosts and brand look-alikes. */
public final class DomainAnalyzer {
    private DomainAnalyzer() {}

    private static final Set<String> SHORTENERS = Set.of(
        "bit.ly", "tinyurl.com", "t.co", "cutt.ly", "rb.gy", "is.gd", "goo.gl", "shorturl.at", "tiny.cc", "ow.ly", "bitly.com");

    private static final Set<String> SUSPICIOUS_TLDS = Set.of(
        "xyz", "top", "click", "live", "icu", "buzz", "shop", "site", "online", "vip", "cc", "tk", "ml", "ga", "cf", "gq",
        "ws", "work", "support", "cfd", "sbs", "lol", "pw", "rest", "fun");

    /** brand token -> registrable domains that really belong to it. */
    private static final Map<String, Set<String>> BRANDS = Map.ofEntries(
        Map.entry("sbi", Set.of("sbi.co.in", "onlinesbi.sbi", "sbi.bank.in", "onlinesbi.com")),
        Map.entry("hdfc", Set.of("hdfcbank.com", "hdfc.com", "hdfcbank.net")),
        Map.entry("icici", Set.of("icicibank.com", "icicidirect.com", "icicilombard.com")),
        Map.entry("axis", Set.of("axisbank.com", "axis.bank.in")),
        Map.entry("kotak", Set.of("kotak.com", "kotakbank.com")),
        Map.entry("pnb", Set.of("pnbindia.in", "netpnb.com")),
        Map.entry("paytm", Set.of("paytm.com", "paytmbank.com")),
        Map.entry("phonepe", Set.of("phonepe.com")),
        Map.entry("gpay", Set.of("pay.google.com")),
        Map.entry("irctc", Set.of("irctc.co.in")),
        Map.entry("amazon", Set.of("amazon.in", "amazon.com", "amazon.co.in")),
        Map.entry("flipkart", Set.of("flipkart.com")),
        Map.entry("indiapost", Set.of("indiapost.gov.in")),
        Map.entry("fedex", Set.of("fedex.com")),
        Map.entry("dhl", Set.of("dhl.com")),
        Map.entry("uidai", Set.of("uidai.gov.in")),
        Map.entry("incometax", Set.of("incometax.gov.in", "incometaxindia.gov.in")),
        Map.entry("echallan", Set.of("echallan.parivahan.gov.in", "parivahan.gov.in")),
        Map.entry("mparivahan", Set.of("parivahan.gov.in")),
        Map.entry("pmkisan", Set.of("pmkisan.gov.in")));

    private static final Set<String> SECOND_LEVELS = Set.of("co", "com", "gov", "nic", "org", "ac", "net", "edu", "bank");
    private static final Pattern IPV4 = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}$");

    /** "login.sbi-kyc.xyz" -> "sbi-kyc.xyz"; "onlinesbi.co.in" -> "onlinesbi.co.in". */
    public static String registrable(String host) {
        String[] p = host.split("\\.");
        if (p.length <= 2) return host;
        boolean cc = p[p.length - 1].length() == 2 && SECOND_LEVELS.contains(p[p.length - 2]);
        int keep = cc ? 3 : 2;
        StringBuilder sb = new StringBuilder();
        for (int i = p.length - keep; i < p.length; i++) {
            if (sb.length() > 0) sb.append('.');
            sb.append(p[i]);
        }
        return sb.toString();
    }

    public static List<Signal> analyze(List<String> urls) {
        List<Signal> out = new ArrayList<>();
        for (String url : urls) {
            String host = Extractor.hostOf(url);
            String reg = registrable(host);
            String tld = host.substring(host.lastIndexOf('.') + 1);

            if (IPV4.matcher(host).matches()) {
                out.add(new Signal("ip_host", host, "", 25));
                continue;
            }
            if (SHORTENERS.contains(reg)) {
                out.add(new Signal("shortener", host, "", 15));
                continue;
            }
            boolean official = BRANDS.values().stream().anyMatch(s -> s.contains(reg))
                || reg.endsWith(".gov.in") || reg.endsWith(".nic.in");
            if (!official) {
                for (String brand : BRANDS.keySet()) {
                    if (reg.replace("-", "").contains(brand) || host.replace("-", "").contains(brand)) {
                        out.add(new Signal("lookalike", host, "", 32));
                        break;
                    }
                }
                if (out.stream().noneMatch(s -> s.phrase().equals(host))
                    && host.matches(".*(kyc|verify|login|secure|refund|reward|claim|helpdesk|customer-?care|e-?challan|kisan).*")) {
                    out.add(new Signal("lookalike", host, "", 24));
                }
                if (out.stream().noneMatch(s -> s.phrase().equals(host)) && SUSPICIOUS_TLDS.contains(tld)) {
                    out.add(new Signal("tld", host, "", 14));
                }
                if (host.chars().filter(c -> c == '-').count() >= 2 && out.stream().noneMatch(s -> s.phrase().equals(host))) {
                    out.add(new Signal("hyphens", host, "", 10));
                }
            }
        }
        return out;
    }
}
