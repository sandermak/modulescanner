package org.adoptopenjdk.modulescanner;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;

class JdepsInspectorTest {

    @Test
    void testNonViolatingJar() {
        JdepsInspector inspector = new JdepsInspector(Paths.get("./src/test/resources/slf4j-api-1.8.0-beta2.jar"));
        JdepsInspector.JdepsInspectResult result = inspector.inspect();

        assertFalse(result.toolerror);
        assertEquals(0, result.violations.size());
    }


    @Test
    void testNettyViolatingJar() {
        JdepsInspector inspector = new JdepsInspector(Paths.get("./src/test/resources/netty-handler-4.1.13.Final.jar"));
        JdepsInspector.JdepsInspectResult result = inspector.inspect();

        System.out.println(result);
        assertFalse(result.toolerror);
        assertLinesMatch(List.of("^sun.security.x509.X500Name.+since 1.4$"), result.violations);
    }

    @Test
    void testNonReadableJar() {
        JdepsInspector inspector = new JdepsInspector(Paths.get("./src/test/resources/non-readable.jar"));
        JdepsInspector.JdepsInspectResult result = inspector.inspect();

        assertTrue(result.toolerror);
    }

    @Test
    void testAkkaViolatingJar() {
        JdepsInspector inspector = new JdepsInspector(Paths.get("./src/test/resources/akka-actor_2.11-2.4.9.jar"));
        JdepsInspector.JdepsInspectResult result = inspector.inspect();

        assertFalse(result.toolerror);
        assertLinesMatch(List.of("^sun.misc.Unsafe.+$"), result.violations);
    }


}