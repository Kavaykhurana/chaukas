# CodeVerse 2026: Prototype Submission checklist

Round window (from the Unstop page screenshot): **15 Nov 2026, 12:07 AM IST to 25 Nov 2026, 12:08 AM IST**.
Required items: working prototype / deployed link, GitHub repository, 2 to 3 minute demo video, updated presentation.

| # | Required item | What to submit | How to produce it | Done |
|---|---|---|---|---|
| 1 | Deployed application link | Vercel URL for the frontend (API on Render) | `docs/DEPLOY.md` steps 1 to 4 | [ ] |
| 2 | GitHub repository | Public repo with this code | `git init`, push, confirm README renders | [ ] |
| 3 | Demo video (2 to 3 min) | Unlisted YouTube / Drive link | Follow `docs/DEMO_SCRIPT.md` | [ ] |
| 4 | Updated presentation | `docs/Chaukas_Prototype_Deck.pptx` (or its PDF) | Included; add team names and live links on the last slide | [ ] |

## Before you press Submit

- [ ] Open the deployed link in a private window and run the sample message end to end
- [ ] `GET <api>/api/health` returns `{"status":"ok", ...}`
- [ ] `CORS_ORIGINS` on Render equals the Vercel URL exactly (no trailing slash)
- [ ] Free Render instances sleep: open the API URL once a few minutes before judging
- [ ] Repo has no `.env`, no API keys (`git log -p | grep -i api_key` returns nothing)
- [ ] Demo logins work: parent@demo.chaukas.in / child@demo.chaukas.in, password `chaukas-demo`
