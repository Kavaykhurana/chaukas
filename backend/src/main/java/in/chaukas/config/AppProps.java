package in.chaukas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chaukas")
public record AppProps(Ai ai, Jwt jwt, Cors cors, int goldenHourMinutes, boolean seedDemo) {
    public record Ai(boolean enabled, int timeoutSeconds) {}
    public record Jwt(String secret, int ttlHours) {}
    public record Cors(String allowedOrigins) {}
}
