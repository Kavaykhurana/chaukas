package in.chaukas.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** End-to-end through the real filter chain on H2, with Gemini disabled: rules + intel must still answer. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"chaukas.seed-demo=true", "chaukas.ai.enabled=false"})
class ApiFlowTest {
    @Autowired MockMvc mvc;
    private final ObjectMapper om = new ObjectMapper();

    private JsonNode json(String body) throws Exception {
        return om.readTree(body);
    }

    private String login(String email) throws Exception {
        String res = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"chaukas-demo\"}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json(res).path("token").asText();
    }

    @Test
    void checkWorksWithoutLoginAndFlagsKycScam() throws Exception {
        String body = mvc.perform(post("/api/checks").contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"Dear customer your SBI account will be blocked today. Complete KYC now: http://sbi-kyc-update.xyz/verify\",\"lang\":\"en\"}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode v = json(body).path("verdict");
        assertEquals("HIGH", v.path("risk").asText());
        assertEquals("FAKE_KYC", v.path("scamType").asText());
        assertTrue(v.path("signals").size() >= 2);
        assertEquals(false, v.path("aiUsed").asBoolean());
    }

    @Test
    void emptyCheckIsRejected() throws Exception {
        mvc.perform(post("/api/checks").contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"  \"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void incidentsNeedLogin() throws Exception {
        mvc.perform(get("/api/incidents")).andExpect(status().isUnauthorized());
    }

    @Test
    void goldenHourFlowExtractsFactsAndBuildsKit() throws Exception {
        String token = login("child@demo.chaukas.in");
        String narrative = "I paid Rs 4,999 to ramesh.collect@okaxis via SBI. UTR 412345678901. "
            + "They said it was a KYC update and my account would be blocked today.";
        String res = mvc.perform(multipart("/api/incidents").param("narrative", narrative).param("lang", "en")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode inc = json(res);
        assertEquals("412345678901", inc.path("facts").path("utr").asText());
        assertEquals(4999, inc.path("facts").path("amount").asInt());
        assertEquals("ramesh.collect@okaxis", inc.path("facts").path("payeeUpi").asText());
        assertTrue(inc.path("kit").path("callScript").asText().length() > 50);

        long id = inc.path("id").asLong();
        String reported = mvc.perform(post("/api/incidents/" + id + "/report").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"shareToCommunity\":true}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals("REPORTED", json(reported).path("status").asText());

        String lookup = mvc.perform(get("/api/intel/lookup").param("kind", "UPI").param("value", "ramesh.collect@okaxis"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertTrue(json(lookup).path("known").asBoolean());
    }

    @Test
    void highRiskCheckByWardAlertsGuardian() throws Exception {
        String parent = login("parent@demo.chaukas.in");
        String child = login("child@demo.chaukas.in");

        mvc.perform(post("/api/checks").header("Authorization", "Bearer " + parent).contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"This is CBI. Digital arrest warrant issued. Do not tell anyone, stay on the video call and transfer money.\"}"))
            .andExpect(status().isOk());

        String alerts = mvc.perform(get("/api/guardians/alerts").header("Authorization", "Bearer " + child))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertTrue(json(alerts).size() >= 1);
    }
}
