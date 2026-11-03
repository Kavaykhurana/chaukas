package in.chaukas.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runs the dependency-free smoke suite under Maven too, so `mvn test` covers the detection core. */
class CoreSmokeTest {
    @Test
    void allCoreChecksPass() {
        List<String> failed = CoreSmoke.runAll();
        assertTrue(failed.isEmpty(), "CoreSmoke failures: " + failed);
    }
}
