package in.chaukas.core;

/** Languages the verdict and the 1930 call script can be rendered in. */
public enum Lang {
    EN, HI, PA;

    public static Lang parse(String s) {
        if (s == null) return EN;
        String v = s.trim().toLowerCase();
        if (v.startsWith("hi")) return HI;
        if (v.startsWith("pa")) return PA;
        return EN;
    }

    public String code() {
        return name().toLowerCase();
    }
}
