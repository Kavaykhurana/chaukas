package in.chaukas.web;

import in.chaukas.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    Dto.AuthResponse register(@Valid @RequestBody Dto.RegisterRequest r) {
        return auth.register(r);
    }

    @PostMapping("/login")
    Dto.AuthResponse login(@Valid @RequestBody Dto.LoginRequest r) {
        return auth.login(r);
    }

    @GetMapping("/me")
    Dto.UserView me(Authentication a) {
        return Dto.UserView.of(auth.require((Long) a.getPrincipal()));
    }

    @PutMapping("/lang")
    Dto.UserView lang(Authentication a, @Valid @RequestBody Dto.LangRequest r) {
        Long id = (Long) a.getPrincipal();
        auth.setLang(id, r.lang());
        return Dto.UserView.of(auth.require(id));
    }
}
