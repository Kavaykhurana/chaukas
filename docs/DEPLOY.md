# Deploying Chaukas

## Option A: everything locally (fastest check)

```bash
cp .env.example .env        # optionally add GEMINI_API_KEY
docker compose up --build   # web http://localhost:3000, api http://localhost:8080
```

## Option B: Render (API + Postgres) and Vercel (frontend)

1. Push the repo to GitHub.
2. **Render** > New > Blueprint > pick the repo. `render.yaml` creates the Postgres database and the Docker web service from `backend/`.
3. In the `chaukas-api` service set `GEMINI_API_KEY` (Google AI Studio key) and `CORS_ORIGINS` (your Vercel URL, set after step 4, then redeploy).
4. **Vercel** > Add project > root directory `frontend`. Environment variable `VITE_API_URL` = the Render service URL (e.g. `https://chaukas-api.onrender.com`).
5. Check `https://<render-url>/api/health`, then open the Vercel URL.

## Run the backend without Docker

```bash
cd backend
mvn spring-boot:run          # H2 in-memory, demo data seeded, rules-only unless GEMINI_API_KEY is set
cd ../frontend && npm install && npm run dev   # http://localhost:5173, proxies /api to :8080
```

## Run only the detection core tests (no Maven needed)

```bash
cd backend
javac -encoding UTF-8 -d /tmp/core-out src/main/java/in/chaukas/core/*.java src/test/java/in/chaukas/core/CoreSmoke.java
java -Dfile.encoding=UTF-8 -cp /tmp/core-out in.chaukas.core.CoreSmoke
```
