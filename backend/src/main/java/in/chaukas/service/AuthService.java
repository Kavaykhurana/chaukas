package in.chaukas.service;

import in.chaukas.config.ApiException;
import in.chaukas.config.JwtService;
import in.chaukas.core.Lang;
import in.chaukas.domain.AppUser;
import in.chaukas.repo.UserRepository;
import in.chaukas.web.Dto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public Dto.AuthResponse register(Dto.RegisterRequest r) {
        String email = r.email().trim().toLowerCase(Locale.ROOT);
        if (users.findByEmailIgnoreCase(email).isPresent()) {
            throw ApiException.conflict("An account with this email already exists.");
        }
        AppUser u = new AppUser();
        u.setEmail(email);
        u.setName(r.name().trim());
        u.setPasswordHash(encoder.encode(r.password()));
        u.setPreferredLang(Lang.parse(r.lang()).code());
        users.save(u);
        return new Dto.AuthResponse(jwt.issue(u), Dto.UserView.of(u));
    }

    public Dto.AuthResponse login(Dto.LoginRequest r) {
        AppUser u = users.findByEmailIgnoreCase(r.email().trim())
            .filter(x -> encoder.matches(r.password(), x.getPasswordHash()))
            .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Wrong email or password."));
        return new Dto.AuthResponse(jwt.issue(u), Dto.UserView.of(u));
    }

    public AppUser require(Long id) {
        return users.findById(id).orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Session expired."));
    }

    public void setLang(Long id, String lang) {
        AppUser u = require(id);
        u.setPreferredLang(Lang.parse(lang).code());
        users.save(u);
    }
}
