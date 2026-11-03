# Chaukas: 2:30 demo video script

Target length **2:30** (limit is 2 to 3 minutes). Record at 1080p, browser at 100% zoom, mic close, one take per scene.

**Before recording:** start the stack (`docker compose up --build`), open `http://localhost:3000`, set the language to English, keep the **demo login** ready (parent@demo.chaukas.in / chaukas-demo). Have `docs/sample-payment-chat.txt` open in another tab to copy from.

| Time | Screen | Say (voice-over) |
|---|---|---|
| 0:00 to 0:15 | Title card, then the app | "Every day Indians lose money to UPI and KYC scams, and the first hour decides whether the money can be frozen. Chaukas helps in both moments: before you pay, and right after." |
| 0:15 to 0:50 | **Check** tab. Click *Use sample message*, press *Check it* | "Mode one. I paste a message that just arrived. In under five seconds: high risk, fake KYC. Every red flag is highlighted in the message itself: 'blocked today', 'KYC', the lookalike link. Rules run first, so this works even if the AI is down." |
| 0:50 to 1:10 | Scroll to *Seen before*. Then paste `sbi.kyc.helpdesk@oksbi` and check | "Reports from earlier victims feed a community store. This UPI ID was reported three times, one confirmed by a victim, so it escalates to high risk instantly." |
| 1:10 to 1:25 | Switch language to हिन्दी, run the check again | "Same verdict in Hindi. Punjabi too. The people most targeted often don't read English." |
| 1:25 to 2:05 | **Golden hour** tab (signed in). Paste the sample payment text, press *Prepare my reports* | "Mode two: 'I've been scammed'. I paste what happened. Chaukas pulls out the amount, UTR and payee, starts the sixty-minute clock, and drafts everything: a 1930 call script, the cybercrime.gov.in complaint, the bank freeze email, and the scammer report. Nothing is filed for me. I copy and send each one." |
| 2:05 to 2:20 | **Family** tab, sign in as child in a second window | "Family guardian: when my mother checks something that looks like a digital arrest, I get an alert. Scam type only, never her message." |
| 2:20 to 2:30 | Architecture slide | "Rules first, AI second, reasons always. Java 21, Spring Boot, Spring AI with Gemini, PostgreSQL. Chaukas never says a message is safe, only that it found no known red flags. Thank you." |

## Sample text for the golden-hour scene

```
I paid Rs 4,999 to ramesh.collect@okaxis from my SBI account at 2:10 pm today. UTR 412345678901.
The caller said my KYC had expired and my account would be blocked today.
```

## Recording checklist

- [ ] AI key set (`GEMINI_API_KEY`) if you want the "AI second opinion" line visible; otherwise say "rules and community reports"
- [ ] Browser notifications and bookmarks bar hidden
- [ ] No real personal data on screen (use the demo accounts)
- [ ] Final length between 2:00 and 3:00
- [ ] Upload as unlisted YouTube or Drive link with "anyone with the link" access
