package in.chaukas.web;

import in.chaukas.config.ApiException;
import in.chaukas.core.Models.Hit;
import in.chaukas.service.IntelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/intel")
public class IntelController {
    private final IntelService intel;

    public IntelController(IntelService intel) {
        this.intel = intel;
    }

    /** GET /api/intel/lookup?kind=UPI&value=helpdesk@okaxis */
    @GetMapping("/lookup")
    Dto.IntelLookupResponse lookup(@RequestParam String kind, @RequestParam String value) {
        String k = kind.toUpperCase(Locale.ROOT);
        if (!List.of("UPI", "PHONE", "DOMAIN").contains(k)) throw ApiException.badRequest("kind must be UPI, PHONE or DOMAIN");
        if (value.isBlank() || value.length() > 190) throw ApiException.badRequest("value is required");
        List<Hit> hits = intel.lookup(k, value);
        if (hits.isEmpty()) return new Dto.IntelLookupResponse(k, value, false, 0, false);
        Hit h = hits.get(0);
        return new Dto.IntelLookupResponse(k, h.value(), true, h.reports(), h.confirmed());
    }
}
