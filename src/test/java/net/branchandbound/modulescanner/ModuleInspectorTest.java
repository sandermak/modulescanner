package net.branchandbound.modulescanner;

import org.junit.Test;

import java.util.jar.JarFile;

import static org.junit.Assert.*;

public class ModuleInspectorTest {

    @Test
    public void testJacksonAutomaticModule() throws Exception {
        JarFile jackson = new JarFile("./src/test/resources/jackson-core-2.9.6.jar");
        ModuleInspector.ModuleInspectResult jacksonResult = new ModuleInspector(jackson).inspect();

        assertNotNull(jackson);
        assertTrue(jacksonResult.isAutomaticModule);
        assertFalse(jacksonResult.isExplicitModule);
        assertEquals("com.fasterxml.jackson.core", jacksonResult.modulename);
    }

    @Test
    public void testSlf4jExplicitModule() throws Exception {
        JarFile slf4j = new JarFile("./src/test/resources/slf4j-api-1.8.0-beta2.jar");
        ModuleInspector.ModuleInspectResult slf4jResult = new ModuleInspector(slf4j).inspect();

        assertNotNull(slf4j);
        assertFalse(slf4jResult.isAutomaticModule);
        assertTrue(slf4jResult.isExplicitModule);
        assertTrue(slf4jResult.dependencies.contains("java.base"));
        assertEquals("org.slf4j", slf4jResult.modulename);
    }

    @Test
    public void testCommonsLangNonmodularJar() throws Exception {
        JarFile commonslang = new JarFile("./src/test/resources/commons-lang-2.6.jar");
        ModuleInspector.ModuleInspectResult commonslangResult = new ModuleInspector(commonslang).inspect();

        assertFalse(commonslangResult.isAutomaticModule);
        assertFalse(commonslangResult.isExplicitModule);
        assertNull(commonslangResult.modulename);
        assertNull(commonslangResult.moduleversion);
    }
     // TODO test with multi-release JARs
}