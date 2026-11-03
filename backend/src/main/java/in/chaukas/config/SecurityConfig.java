package in.chaukas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtService jwt) throws Exception {
        http
            .csrf(c -> c.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/register", "/api/auth/login", "/api/health").permitAll()
                .requestMatchers("/actuator/health/**").permitAll()
                // Mode 1 (Check) is deliberately usable without an account: people under pressure won't sign up first.
                .requestMatchers(HttpMethod.POST, "/api/checks").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/intel/lookup").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .addFilterBefore(new RateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new JwtAuthFilter(jwt), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /** Auth is JWT-only; this stops Spring from generating a throwaway default user. */
    @Bean
    UserDetailsService noUserDetails() {
        return username -> {
            throw new UsernameNotFoundException(username);
        };
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(AppProps props) {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(Arrays.stream(props.cors().allowedOrigins().split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList());
        c.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        c.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/api/**", c);
        return src;
    }

    /** Checks run the intel lookup and the Gemini call in parallel on virtual threads. */
    @Bean(destroyMethod = "shutdown")
    ExecutorService virtualExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
