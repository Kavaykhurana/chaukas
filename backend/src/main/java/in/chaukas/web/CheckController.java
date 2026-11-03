package in.chaukas.web;

import in.chaukas.service.CheckService;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/checks")
public class CheckController {
    private final CheckService checks;

    public CheckController(CheckService checks) {
        this.checks = checks;
    }

    /** Multipart so a pasted message and a screenshot can arrive together. Works without login. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Dto.CheckResponse check(Authentication auth,
                            @RequestParam(required = false) String text,
                            @RequestParam(required = false) String lang,
                            @RequestPart(required = false) MultipartFile image) throws IOException {
        byte[] bytes = image == null || image.isEmpty() ? null : image.getBytes();
        String mime = image == null ? null : image.getContentType();
        return checks.check(userId(auth), text, bytes, mime, lang);
    }

    /** JSON variant for scripts and tests. */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    Dto.CheckResponse checkJson(Authentication auth, @RequestBody JsonCheck body) {
        return checks.check(userId(auth), body.text(), null, null, body.lang());
    }

    record JsonCheck(String text, String lang) {}

    static Long userId(Authentication a) {
        return a != null && a.getPrincipal() instanceof Long id ? id : null;
    }
}
