package in.chaukas.web;

import in.chaukas.core.Extractor;
import in.chaukas.service.IncidentService;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {
    private final IncidentService incidents;

    public IncidentController(IncidentService incidents) {
        this.incidents = incidents;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Dto.IncidentView create(Authentication a,
                            @RequestParam(required = false) String narrative,
                            @RequestParam(required = false) String lang,
                            @RequestPart(required = false) List<MultipartFile> images) throws IOException {
        List<byte[]> bytes = new ArrayList<>();
        List<String> mimes = new ArrayList<>();
        if (images != null) {
            for (MultipartFile f : images) {
                if (f.isEmpty() || f.getContentType() == null || !f.getContentType().startsWith("image/")) continue;
                if (bytes.size() >= 4) break;
                bytes.add(f.getBytes());
                mimes.add(f.getContentType());
            }
        }
        return incidents.create((Long) a.getPrincipal(), narrative, bytes, mimes, lang, now());
    }

    @GetMapping
    List<Dto.IncidentView> list(Authentication a) {
        return incidents.list((Long) a.getPrincipal(), now());
    }

    @GetMapping("/{id}")
    Dto.IncidentView get(Authentication a, @PathVariable Long id) {
        return incidents.get((Long) a.getPrincipal(), id, now());
    }

    /** Called when the victim says they have filed the complaint themselves. Nothing is filed by Chaukas. */
    @PostMapping("/{id}/report")
    Dto.IncidentView report(Authentication a, @PathVariable Long id, @RequestBody(required = false) Dto.ReportRequest r) {
        boolean share = r == null || r.shareToCommunity() == null || r.shareToCommunity();
        return incidents.markReported((Long) a.getPrincipal(), id, share, now());
    }

    private static ZonedDateTime now() {
        return ZonedDateTime.now(Extractor.IST);
    }
}
