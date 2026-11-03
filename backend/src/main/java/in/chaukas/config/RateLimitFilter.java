package in.chaukas.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sliding-window limiter for the endpoints that can spend Gemini quota: 20 requests per minute per client.
 * In-memory, so it protects a single instance. Put a shared limiter in front for multi-instance deployments.
 */
public class RateLimitFilter extends OncePerRequestFilter {
    private static final int LIMIT = 20;
    private static final long WINDOW_MS = 60_000;
    private final ConcurrentHashMap<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String p = req.getRequestURI();
        return !"POST".equals(req.getMethod()) || !(p.equals("/api/checks") || p.equals("/api/incidents"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
        throws ServletException, IOException {
        String ip = clientIp(req);
        long now = System.currentTimeMillis();
        Deque<Long> q = hits.computeIfAbsent(ip, k -> new ArrayDeque<>());
        boolean allowed;
        synchronized (q) {
            while (!q.isEmpty() && now - q.peekFirst() > WINDOW_MS) q.pollFirst();
            allowed = q.size() < LIMIT;
            if (allowed) q.addLast(now);
        }
        if (hits.size() > 5000) hits.entrySet().removeIf(e -> e.getValue().isEmpty());
        if (!allowed) {
            res.setStatus(429);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"Too many requests. Please wait a minute and try again.\"}");
            return;
        }
        chain.doFilter(req, res);
    }

    private static String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return req.getRemoteAddr();
    }
}
