package net.branchandbound.modulescanner;

import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.*;

public class JdepsInspectorTest {

    @Test
    public void testNonViolatingJar() {
        JdepsInspector inspector = new JdepsInspector(Paths.get("./src/test/resources/slf4j-api-1.8.0-beta2.jar"));
        JdepsInspector.JdepsInspectResult result = inspector.inspect();

        assertFalse(result.toolerror);
        assertEquals(0, result.violations.size());
    }


    @Test
    public void testNettyViolatingJar() {
        JdepsInspector inspector = new JdepsInspector(Paths.get("./src/test/resources/netty-handler-4.1.13.Final.jar"));
        JdepsInspector.JdepsInspectResult result = inspector.inspect();

        System.out.println(result);
        assertFalse(result.toolerror);
        assertEquals(1, result.violations.size());
        assertTrue(result.violations.get(0).contains("sun.security.x509.X500Name"));
    }

    @Test
    public void testNonReadableJar() {
        JdepsInspector inspector = new JdepsInspector(Paths.get("./src/test/resources/non-readable.jar"));
        JdepsInspector.JdepsInspectResult result = inspector.inspect();

        assertTrue(result.toolerror);
    }

    @Test
    public void testAkkaViolatingJar() {
        JdepsInspector inspector = new JdepsInspector(Paths.get("./src/test/resources/akka-actor_2.11-2.4.9.jar"));
        JdepsInspector.JdepsInspectResult result = inspector.inspect();

        assertFalse(result.toolerror);
        assertEquals(1, result.violations.size());
        assertTrue(result.violations.get(0).contains("sun.misc.Unsafe"));
    }


}