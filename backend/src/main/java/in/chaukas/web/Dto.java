package in.chaukas.web;

import in.chaukas.core.Models;
import in.chaukas.domain.AppUser;
import in.chaukas.domain.GuardianAlert;
import in.chaukas.domain.Incident;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class Dto {
    private Dto() {}

    public record RegisterRequest(@NotBlank @Size(max = 120) String name,
                                  @NotBlank @Email @Size(max = 190) String email,
                                  @NotBlank @Size(min = 8, max = 72) String password,
                                  String lang) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    public record UserView(Long id, String name, String email, String lang) {
        public static UserView of(AppUser u) {
            return new UserView(u.getId(), u.getName(), u.getEmail(), u.getPreferredLang());
        }
    }

    public record AuthResponse(String token, UserView user) {}

    public record LangRequest(@NotBlank String lang) {}

    public record CheckResponse(Long id, Models.Verdict verdict, Models.Identifiers identifiers) {}

    public record IntelLookupResponse(String kind, String value, boolean known, int reports, boolean confirmed) {}

    public record InviteView(String code) {}

    public record AcceptInviteRequest(@NotBlank String code) {}

    public record LinkView(Long id, String code, String status, String wardName, String guardianName, Instant createdAt) {}

    public record Family(List<LinkView> myGuardians, List<LinkView> myWards) {}

    public record AlertView(Long id, String wardName, String scamType, String message, boolean read, Instant createdAt) {
        public static AlertView of(GuardianAlert a) {
            return new AlertView(a.getId(), a.getWardName(), a.getScamType(), a.getMessage(), a.isRead(), a.getCreatedAt());
        }
    }

    public record IncidentFacts(BigDecimal amount, String utr, String payeeUpi, String payeePhone, String bank,
                                Instant paidAt, String scamType) {}

    /** A golden-hour incident: extracted facts, the countdown, and the ready-to-send drafts. */
    public record IncidentView(Long id, String status, IncidentFacts facts, long minutesLeft, long minutesElapsed,
                               boolean withinGoldenHour, List<String> missing, Models.Kit kit, Instant reportedAt) {
        public static IncidentView of(Incident i, IncidentFacts f, long left, long elapsed, boolean within,
                                      List<String> missing, Models.Kit kit) {
            return new IncidentView(i.getId(), i.getStatus(), f, left, elapsed, within, missing, kit, i.getReportedAt());
        }
    }

    public record ReportRequest(Boolean shareToCommunity) {}

    public record ErrorBody(String error) {}
}
