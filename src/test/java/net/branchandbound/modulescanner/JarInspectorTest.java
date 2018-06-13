package net.branchandbound.modulescanner;

import org.junit.Test;

import java.util.jar.JarFile;

import static org.junit.Assert.*;

public class JarInspectorTest {


    @Test
    public void jacksonIsAutomaticModule() throws Exception {

        JarFile jackson = new JarFile("./src/test/resources/jackson-core-2.9.6.jar");
        JarInspector scanner = new JarInspector(jackson);

        assertNotNull(jackson);
        assertTrue(scanner.inspect().isAutomaticModule);
    }

    @Test
    public void slf4jIsNotAutomaticModule() throws Exception {
        JarFile slf4j = new JarFile("./src/test/resources/slf4j-api-1.8.0-beta2.jar");
        JarInspector scanner = new JarInspector(slf4j);

        assertNotNull(slf4j);
        assertFalse(scanner.inspect().isAutomaticModule);
    }

    @Test
    public void slf4jIsExplicitModule() throws Exception {
        JarFile slf4j = new JarFile("./src/test/resources/slf4j-api-1.8.0-beta2.jar");
        JarInspector scanner = new JarInspector(slf4j);

        assertNotNull(slf4j);
        assertTrue(scanner.inspect().isExplicitModule);
    }

    @Test
    public void jacksonIsNotExplicitModule() throws Exception {
        JarFile jackson = new JarFile("./src/test/resources/jackson-core-2.9.6.jar");
        JarInspector scanner = new JarInspector(jackson);

        assertNotNull(jackson);
        assertFalse(scanner.inspect().isExplicitModule);
    }

    @Test
    public void jacksonModuleName() throws Exception {
        JarFile jackson = new JarFile("./src/test/resources/jackson-core-2.9.6.jar");
        JarInspector scanner = new JarInspector(jackson);

        assertNotNull(jackson);
        assertEquals("com.fasterxml.jackson.core", scanner.inspect().modulename);
    }

    @Test
    public void slf4jModuleName() throws Exception {
        JarFile slf4j = new JarFile("./src/test/resources/slf4j-api-1.8.0-beta2.jar");
        JarInspector scanner = new JarInspector(slf4j);

        assertNotNull(slf4j);
        assertEquals("org.slf4j", scanner.inspect().modulename);
     }

     // TODO test with multi-release JARs
}