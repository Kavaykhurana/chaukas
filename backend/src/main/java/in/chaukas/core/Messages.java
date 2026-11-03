package in.chaukas.core;

import java.util.Map;

/** All user-facing verdict copy in English, Hindi and Punjabi. */
public final class Messages {
    private Messages() {}

    private static String[] t(String en, String hi, String pa) {
        return new String[] {en, hi, pa};
    }

    private static String pick(String[] v, Lang l) {
        return v[l.ordinal()];
    }

    // ---- scam type labels -------------------------------------------------------------------
    private static final Map<String, String[]> LABELS = Map.ofEntries(
        Map.entry("FAKE_KYC", t("Fake KYC update", "फर्जी KYC अपडेट", "ਜਾਅਲੀ KYC ਅੱਪਡੇਟ")),
        Map.entry("DIGITAL_ARREST", t("Digital-arrest scam", "डिजिटल अरेस्ट ठगी", "ਡਿਜੀਟਲ ਅਰੈਸਟ ਠੱਗੀ")),
        Map.entry("COLLECT_REQUEST", t("Fake collect request", "फर्जी कलेक्ट रिक्वेस्ट", "ਜਾਅਲੀ ਕਲੈਕਟ ਰਿਕਵੈਸਟ")),
        Map.entry("INVESTMENT_FRAUD", t("Investment fraud", "निवेश धोखाधड़ी", "ਨਿਵੇਸ਼ ਧੋਖਾਧੜੀ")),
        Map.entry("TASK_JOB", t("Task / part-time job scam", "टास्क / पार्ट-टाइम जॉब ठगी", "ਟਾਸਕ / ਪਾਰਟ-ਟਾਈਮ ਨੌਕਰੀ ਠੱਗੀ")),
        Map.entry("PRIZE", t("Prize / lottery scam", "इनाम / लॉटरी ठगी", "ਇਨਾਮ / ਲਾਟਰੀ ਠੱਗੀ")),
        Map.entry("PARCEL", t("Parcel / customs scam", "पार्सल / कस्टम्स ठगी", "ਪਾਰਸਲ / ਕਸਟਮ ਠੱਗੀ")),
        Map.entry("OTP_PIN", t("OTP / PIN theft", "OTP / PIN की चोरी", "OTP / PIN ਦੀ ਚੋਰੀ")),
        Map.entry("BILL", t("Fake bill / disconnection threat", "फर्जी बिल / कनेक्शन कटने की धमकी", "ਜਾਅਲੀ ਬਿੱਲ / ਕੁਨੈਕਸ਼ਨ ਕੱਟਣ ਦੀ ਧਮਕੀ")),
        Map.entry("UPI_REFUND", t("Fake refund / wrong transfer", "फर्जी रिफंड / गलत ट्रांसफर", "ਜਾਅਲੀ ਰਿਫੰਡ / ਗਲਤ ਟ੍ਰਾਂਸਫਰ")),
        Map.entry("GENERIC", t("Suspicious message", "संदिग्ध संदेश", "ਸ਼ੱਕੀ ਸੁਨੇਹਾ")),
        Map.entry("NONE", t("No known red flags", "कोई ज्ञात खतरे के संकेत नहीं", "ਕੋਈ ਜਾਣਿਆ ਖ਼ਤਰੇ ਦਾ ਸੰਕੇਤ ਨਹੀਂ")));

    // ---- one-line headline for a HIGH verdict, per scam type --------------------------------
    private static final Map<String, String[]> HEADLINES = Map.ofEntries(
        Map.entry("FAKE_KYC", t("Don't open the link. Your bank will never ask for KYC this way.",
            "लिंक न खोलें। आपका बैंक KYC के लिए कभी इस तरह नहीं कहता।",
            "ਲਿੰਕ ਨਾ ਖੋਲ੍ਹੋ। ਤੁਹਾਡਾ ਬੈਂਕ KYC ਲਈ ਕਦੇ ਇਸ ਤਰ੍ਹਾਂ ਨਹੀਂ ਕਹਿੰਦਾ।")),
        Map.entry("DIGITAL_ARREST", t("Hang up. No agency arrests anyone over a video call.",
            "फोन काट दें। कोई भी एजेंसी वीडियो कॉल पर गिरफ्तारी नहीं करती।",
            "ਫ਼ੋਨ ਕੱਟ ਦਿਓ। ਕੋਈ ਵੀ ਏਜੰਸੀ ਵੀਡੀਓ ਕਾਲ 'ਤੇ ਗ੍ਰਿਫ਼ਤਾਰੀ ਨਹੀਂ ਕਰਦੀ।")),
        Map.entry("COLLECT_REQUEST", t("Don't approve it. You never enter a PIN to receive money.",
            "मंजूर न करें। पैसे पाने के लिए PIN कभी नहीं डालना पड़ता।",
            "ਮਨਜ਼ੂਰ ਨਾ ਕਰੋ। ਪੈਸੇ ਲੈਣ ਲਈ PIN ਕਦੇ ਨਹੀਂ ਪਾਉਣਾ ਪੈਂਦਾ।")),
        Map.entry("INVESTMENT_FRAUD", t("Don't send money. Guaranteed returns do not exist.",
            "पैसे न भेजें। गारंटीशुदा मुनाफा जैसी कोई चीज़ नहीं होती।",
            "ਪੈਸੇ ਨਾ ਭੇਜੋ। ਪੱਕੇ ਮੁਨਾਫ਼ੇ ਵਰਗੀ ਕੋਈ ਚੀਜ਼ ਨਹੀਂ ਹੁੰਦੀ।")),
        Map.entry("TASK_JOB", t("Don't pay anything. Real jobs never ask you to deposit money first.",
            "कुछ भी भुगतान न करें। असली नौकरी में पहले पैसे जमा नहीं कराए जाते।",
            "ਕੋਈ ਭੁਗਤਾਨ ਨਾ ਕਰੋ। ਅਸਲੀ ਨੌਕਰੀ ਵਿੱਚ ਪਹਿਲਾਂ ਪੈਸੇ ਜਮ੍ਹਾਂ ਨਹੀਂ ਕਰਵਾਏ ਜਾਂਦੇ।")),
        Map.entry("PRIZE", t("Ignore it. You can't win a contest you never entered.",
            "अनदेखा करें। जिस प्रतियोगिता में भाग नहीं लिया, उसे आप जीत नहीं सकते।",
            "ਅਣਡਿੱਠਾ ਕਰੋ। ਜਿਸ ਮੁਕਾਬਲੇ ਵਿੱਚ ਹਿੱਸਾ ਨਹੀਂ ਲਿਆ, ਉਹ ਤੁਸੀਂ ਜਿੱਤ ਨਹੀਂ ਸਕਦੇ।")),
        Map.entry("PARCEL", t("Don't pay any 'customs fee'. Check the courier on its official app.",
            "'कस्टम्स फीस' न दें। कूरियर की जानकारी उसके आधिकारिक ऐप पर देखें।",
            "'ਕਸਟਮ ਫੀਸ' ਨਾ ਦਿਓ। ਕੂਰੀਅਰ ਦੀ ਜਾਣਕਾਰੀ ਉਸਦੀ ਅਧਿਕਾਰਤ ਐਪ 'ਤੇ ਵੇਖੋ।")),
        Map.entry("OTP_PIN", t("Never share an OTP, PIN or CVV. Nobody genuine will ask for it.",
            "OTP, PIN या CVV किसी को न बताएं। कोई असली व्यक्ति इसे नहीं माँगता।",
            "OTP, PIN ਜਾਂ CVV ਕਿਸੇ ਨੂੰ ਨਾ ਦੱਸੋ। ਕੋਈ ਅਸਲੀ ਵਿਅਕਤੀ ਇਹ ਨਹੀਂ ਮੰਗਦਾ।")),
        Map.entry("BILL", t("Don't pay through this message. Check the bill on the official app or office.",
            "इस संदेश से भुगतान न करें। बिल आधिकारिक ऐप या दफ्तर में जांचें।",
            "ਇਸ ਸੁਨੇਹੇ ਰਾਹੀਂ ਭੁਗਤਾਨ ਨਾ ਕਰੋ। ਬਿੱਲ ਅਧਿਕਾਰਤ ਐਪ ਜਾਂ ਦਫ਼ਤਰ ਵਿੱਚ ਚੈੱਕ ਕਰੋ।")),
        Map.entry("UPI_REFUND", t("Don't send anything back. Ask your bank, not the sender.",
            "कुछ भी वापस न भेजें। सीधे अपने बैंक से पूछें, भेजने वाले से नहीं।",
            "ਕੁਝ ਵੀ ਵਾਪਸ ਨਾ ਭੇਜੋ। ਸਿੱਧਾ ਆਪਣੇ ਬੈਂਕ ਤੋਂ ਪੁੱਛੋ, ਭੇਜਣ ਵਾਲੇ ਤੋਂ ਨਹੀਂ।")),
        Map.entry("GENERIC", t("This looks like a scam. Don't click, pay or reply.",
            "यह ठगी जैसा लगता है। क्लिक, भुगतान या जवाब न दें।",
            "ਇਹ ਠੱਗੀ ਵਰਗਾ ਲੱਗਦਾ ਹੈ। ਕਲਿੱਕ, ਭੁਗਤਾਨ ਜਾਂ ਜਵਾਬ ਨਾ ਦਿਓ।")));

    private static final String[] MEDIUM_HEADLINE = t(
        "Be careful. Some parts of this message look like known scam tricks.",
        "सावधान रहें। इस संदेश के कुछ हिस्से जाने-पहचाने ठगी के तरीकों जैसे हैं।",
        "ਸਾਵਧਾਨ ਰਹੋ। ਇਸ ਸੁਨੇਹੇ ਦੇ ਕੁਝ ਹਿੱਸੇ ਜਾਣੀਆਂ-ਪਛਾਣੀਆਂ ਠੱਗੀ ਦੀਆਂ ਚਾਲਾਂ ਵਰਗੇ ਹਨ।");
    private static final String[] LOW_HEADLINE = t(
        "No known red flags. That is not the same as safe.",
        "कोई ज्ञात खतरे के संकेत नहीं। इसका मतलब पूरी तरह सुरक्षित नहीं है।",
        "ਕੋਈ ਜਾਣਿਆ ਖ਼ਤਰੇ ਦਾ ਸੰਕੇਤ ਨਹੀਂ। ਇਸਦਾ ਮਤਲਬ ਪੂਰੀ ਤਰ੍ਹਾਂ ਸੁਰੱਖਿਅਤ ਨਹੀਂ।");

    private static final String[] ADVICE_HIGH = t(
        "If you already paid or shared details, open Golden hour now. Speed decides whether money can be frozen.",
        "अगर आप पैसे भेज चुके हैं या जानकारी दे चुके हैं, तो अभी गोल्डन आवर खोलें। पैसे फ्रीज़ होंगे या नहीं, यह रफ़्तार पर निर्भर है।",
        "ਜੇ ਤੁਸੀਂ ਪੈਸੇ ਭੇਜ ਚੁੱਕੇ ਹੋ ਜਾਂ ਜਾਣਕਾਰੀ ਦੇ ਚੁੱਕੇ ਹੋ, ਤਾਂ ਹੁਣੇ ਗੋਲਡਨ ਆਵਰ ਖੋਲ੍ਹੋ। ਪੈਸੇ ਫ੍ਰੀਜ਼ ਹੋਣਗੇ ਜਾਂ ਨਹੀਂ, ਇਹ ਤੇਜ਼ੀ 'ਤੇ ਨਿਰਭਰ ਹੈ।");
    private static final String[] ADVICE_MEDIUM = t(
        "Verify through the official app or the number printed on your card or bill before you act.",
        "कुछ भी करने से पहले आधिकारिक ऐप या कार्ड/बिल पर छपे नंबर से पुष्टि करें।",
        "ਕੁਝ ਵੀ ਕਰਨ ਤੋਂ ਪਹਿਲਾਂ ਅਧਿਕਾਰਤ ਐਪ ਜਾਂ ਕਾਰਡ/ਬਿੱਲ 'ਤੇ ਛਪੇ ਨੰਬਰ ਤੋਂ ਪੁਸ਼ਟੀ ਕਰੋ।");
    private static final String[] ADVICE_LOW = t(
        "Chaukas only knows today's patterns. If anything feels off, call the sender on a number you already trust.",
        "चौकस केवल आज के पैटर्न जानता है। कुछ भी अजीब लगे तो उस नंबर पर फोन करें जिस पर आपको पहले से भरोसा है।",
        "ਚੌਕਸ ਸਿਰਫ਼ ਅੱਜ ਦੇ ਪੈਟਰਨ ਜਾਣਦਾ ਹੈ। ਕੁਝ ਵੀ ਅਜੀਬ ਲੱਗੇ ਤਾਂ ਉਸ ਨੰਬਰ 'ਤੇ ਫ਼ੋਨ ਕਰੋ ਜਿਸ 'ਤੇ ਤੁਹਾਨੂੰ ਪਹਿਲਾਂ ਤੋਂ ਭਰੋਸਾ ਹੈ।");

    // ---- reasons per signal id -------------------------------------------------------------
    private static final Map<String, String[]> REASONS = Map.ofEntries(
        Map.entry("urgency", t("Deadline pressure so you act before you think.",
            "जल्दबाज़ी का दबाव, ताकि आप सोचे बिना कदम उठाएं।",
            "ਜਲਦਬਾਜ਼ੀ ਦਾ ਦਬਾਅ, ਤਾਂ ਜੋ ਤੁਸੀਂ ਸੋਚੇ ਬਿਨਾਂ ਕਦਮ ਚੁੱਕੋ।")),
        Map.entry("threat", t("Threatens to block, freeze or cut off something you rely on.",
            "खाता/सेवा बंद करने की धमकी, जिस पर आप निर्भर हैं।",
            "ਖਾਤਾ/ਸੇਵਾ ਬੰਦ ਕਰਨ ਦੀ ਧਮਕੀ, ਜਿਸ 'ਤੇ ਤੁਸੀਂ ਨਿਰਭਰ ਹੋ।")),
        Map.entry("kyc", t("Banks do not ask for KYC through links or messages.",
            "बैंक लिंक या संदेश के ज़रिए KYC नहीं मांगते।",
            "ਬੈਂਕ ਲਿੰਕ ਜਾਂ ਸੁਨੇਹੇ ਰਾਹੀਂ KYC ਨਹੀਂ ਮੰਗਦੇ।")),
        Map.entry("link_action", t("Asks you to tap a link to update, verify or log in.",
            "लिंक पर क्लिक करके अपडेट, वेरिफाई या लॉगिन करने को कहता है।",
            "ਲਿੰਕ 'ਤੇ ਕਲਿੱਕ ਕਰਕੇ ਅੱਪਡੇਟ, ਵੈਰੀਫਾਈ ਜਾਂ ਲੌਗਇਨ ਕਰਨ ਨੂੰ ਕਹਿੰਦਾ ਹੈ।")),
        Map.entry("authority", t("Claims to be police, CBI, customs or a court to scare you.",
            "डराने के लिए पुलिस, CBI, कस्टम्स या कोर्ट होने का दावा।",
            "ਡਰਾਉਣ ਲਈ ਪੁਲਿਸ, CBI, ਕਸਟਮ ਜਾਂ ਕੋਰਟ ਹੋਣ ਦਾ ਦਾਅਵਾ।")),
        Map.entry("collect", t("A collect request or 'enter PIN to receive' takes money out of your account.",
            "कलेक्ट रिक्वेस्ट या 'पैसे पाने के लिए PIN डालें' से आपके खाते से पैसे कटते हैं।",
            "ਕਲੈਕਟ ਰਿਕਵੈਸਟ ਜਾਂ 'ਪੈਸੇ ਲੈਣ ਲਈ PIN ਪਾਓ' ਨਾਲ ਤੁਹਾਡੇ ਖਾਤੇ ਵਿੱਚੋਂ ਪੈਸੇ ਕੱਟਦੇ ਹਨ।")),
        Map.entry("investment", t("Promises guaranteed or unrealistic returns.",
            "गारंटीशुदा या असंभव मुनाफे का वादा।",
            "ਪੱਕੇ ਜਾਂ ਅਸੰਭਵ ਮੁਨਾਫ਼ੇ ਦਾ ਵਾਅਦਾ।")),
        Map.entry("task", t("Pays for simple online tasks, then asks you to deposit money.",
            "आसान ऑनलाइन टास्क के पैसे देकर फिर जमा कराने को कहता है।",
            "ਆਸਾਨ ਆਨਲਾਈਨ ਟਾਸਕ ਦੇ ਪੈਸੇ ਦੇ ਕੇ ਫਿਰ ਜਮ੍ਹਾਂ ਕਰਵਾਉਣ ਨੂੰ ਕਹਿੰਦਾ ਹੈ।")),
        Map.entry("prize", t("Says you won something you never entered for.",
            "ऐसा इनाम जीतने का दावा जिसमें आपने भाग ही नहीं लिया।",
            "ਅਜਿਹਾ ਇਨਾਮ ਜਿੱਤਣ ਦਾ ਦਾਅਵਾ ਜਿਸ ਵਿੱਚ ਤੁਸੀਂ ਹਿੱਸਾ ਹੀ ਨਹੀਂ ਲਿਆ।")),
        Map.entry("parcel", t("Fake courier or customs problem used to demand a fee.",
            "फीस मांगने के लिए नकली कूरियर या कस्टम्स की समस्या।",
            "ਫੀਸ ਮੰਗਣ ਲਈ ਨਕਲੀ ਕੂਰੀਅਰ ਜਾਂ ਕਸਟਮ ਦੀ ਸਮੱਸਿਆ।")),
        Map.entry("credential", t("Asks for an OTP, PIN, CVV or password. Nobody genuine does.",
            "OTP, PIN, CVV या पासवर्ड माँगता है। कोई असली व्यक्ति ऐसा नहीं करता।",
            "OTP, PIN, CVV ਜਾਂ ਪਾਸਵਰਡ ਮੰਗਦਾ ਹੈ। ਕੋਈ ਅਸਲੀ ਵਿਅਕਤੀ ਅਜਿਹਾ ਨਹੀਂ ਕਰਦਾ।")),
        Map.entry("bill", t("Threatens a disconnection to push you into paying through a link.",
            "लिंक से भुगतान कराने के लिए कनेक्शन कटने की धमकी।",
            "ਲਿੰਕ ਰਾਹੀਂ ਭੁਗਤਾਨ ਕਰਵਾਉਣ ਲਈ ਕੁਨੈਕਸ਼ਨ ਕੱਟਣ ਦੀ ਧਮਕੀ।")),
        Map.entry("refund", t("A 'refund' or 'wrong transfer' story used to make you send money or share a PIN.",
            "'रिफंड' या 'गलत ट्रांसफर' की कहानी, ताकि आप पैसे भेजें या PIN बताएं।",
            "'ਰਿਫੰਡ' ਜਾਂ 'ਗਲਤ ਟ੍ਰਾਂਸਫਰ' ਦੀ ਕਹਾਣੀ, ਤਾਂ ਜੋ ਤੁਸੀਂ ਪੈਸੇ ਭੇਜੋ ਜਾਂ PIN ਦੱਸੋ।")),
        Map.entry("secrecy", t("Tells you to keep it secret or stay on the call.",
            "किसी को न बताने या कॉल पर बने रहने को कहता है।",
            "ਕਿਸੇ ਨੂੰ ਨਾ ਦੱਸਣ ਜਾਂ ਕਾਲ 'ਤੇ ਬਣੇ ਰਹਿਣ ਨੂੰ ਕਹਿੰਦਾ ਹੈ।")),
        Map.entry("shortener", t("A shortened link hides where it really goes.",
            "छोटा किया गया लिंक छिपा देता है कि वह असल में कहाँ जाता है।",
            "ਛੋਟਾ ਕੀਤਾ ਲਿੰਕ ਲੁਕਾ ਦਿੰਦਾ ਹੈ ਕਿ ਉਹ ਅਸਲ ਵਿੱਚ ਕਿੱਥੇ ਜਾਂਦਾ ਹੈ।")),
        Map.entry("tld", t("Unusual web address ending often used for throwaway scam sites.",
            "अजीब डोमेन एंडिंग, जो अक्सर चलताऊ ठगी वाली साइटों पर होती है।",
            "ਅਜੀਬ ਡੋਮੇਨ ਅੰਤ, ਜੋ ਅਕਸਰ ਠੱਗੀ ਵਾਲੀਆਂ ਸਾਈਟਾਂ 'ਤੇ ਹੁੰਦਾ ਹੈ।")),
        Map.entry("lookalike", t("Uses a bank or service name but is not its real website.",
            "बैंक/सेवा का नाम इस्तेमाल करता है पर उसकी असली वेबसाइट नहीं है।",
            "ਬੈਂਕ/ਸੇਵਾ ਦਾ ਨਾਮ ਵਰਤਦਾ ਹੈ ਪਰ ਉਸਦੀ ਅਸਲੀ ਵੈੱਬਸਾਈਟ ਨਹੀਂ ਹੈ।")),
        Map.entry("ip_host", t("A raw IP address instead of a real website name.",
            "असली वेबसाइट के नाम की जगह सीधा IP पता।",
            "ਅਸਲੀ ਵੈੱਬਸਾਈਟ ਦੇ ਨਾਮ ਦੀ ਥਾਂ ਸਿੱਧਾ IP ਪਤਾ।")),
        Map.entry("hyphens", t("Long hyphenated address, typical of look-alike sites.",
            "कई हाइफ़न वाला लंबा पता, जो नकली साइटों में आम है।",
            "ਕਈ ਹਾਈਫ਼ਨ ਵਾਲਾ ਲੰਮਾ ਪਤਾ, ਜੋ ਨਕਲੀ ਸਾਈਟਾਂ ਵਿੱਚ ਆਮ ਹੈ।")),
        Map.entry("intel", t("Matches earlier scam reports in the Chaukas community list.",
            "चौकस की सामुदायिक सूची में पहले की ठगी-रिपोर्टों से मेल खाता है।",
            "ਚੌਕਸ ਦੀ ਭਾਈਚਾਰਕ ਸੂਚੀ ਵਿੱਚ ਪਹਿਲਾਂ ਦੀਆਂ ਠੱਗੀ-ਰਿਪੋਰਟਾਂ ਨਾਲ ਮਿਲਦਾ ਹੈ।")));

    public static String label(String scamType, Lang l) {
        String[] v = LABELS.get(scamType);
        return pick(v != null ? v : LABELS.get("GENERIC"), l);
    }

    public static String headline(Models.Risk risk, String scamType, Lang l) {
        return switch (risk) {
            case HIGH -> {
                String[] v = HEADLINES.get(scamType);
                yield pick(v != null ? v : HEADLINES.get("GENERIC"), l);
            }
            case MEDIUM -> pick(MEDIUM_HEADLINE, l);
            case LOW -> pick(LOW_HEADLINE, l);
        };
    }

    public static String advice(Models.Risk risk, Lang l) {
        return pick(switch (risk) {
            case HIGH -> ADVICE_HIGH;
            case MEDIUM -> ADVICE_MEDIUM;
            case LOW -> ADVICE_LOW;
        }, l);
    }

    public static String reason(String signalId, Lang l) {
        String[] v = REASONS.get(signalId);
        return v == null ? "" : pick(v, l);
    }
}
