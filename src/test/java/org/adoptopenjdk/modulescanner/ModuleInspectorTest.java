package org.adoptopenjdk.modulescanner;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.util.jar.JarFile;

class ModuleInspectorTest {

    @Test
    void testJacksonAutomaticModule() throws Exception {
        JarFile jackson = new JarFile("./src/test/resources/jackson-core-2.9.6.jar");
        ModuleInspector.ModuleInspectResult jacksonResult = new ModuleInspector(jackson).inspect();

        assertNotNull(jackson);
        assertTrue(jacksonResult.isAutomaticModule);
        assertFalse(jacksonResult.isExplicitModule);
        assertEquals("com.fasterxml.jackson.core", jacksonResult.modulename);
    }

    @Test
    void testSlf4jExplicitModule() throws Exception {
        JarFile slf4j = new JarFile("./src/test/resources/slf4j-api-1.8.0-beta2.jar");
        ModuleInspector.ModuleInspectResult slf4jResult = new ModuleInspector(slf4j).inspect();

        assertNotNull(slf4j);
        assertFalse(slf4jResult.isAutomaticModule);
        assertTrue(slf4jResult.isExplicitModule);
        assertTrue(slf4jResult.dependencies.contains("java.base"));
        assertEquals("org.slf4j", slf4jResult.modulename);
    }

    @Test
    void testCommonsLangNonmodularJar() throws Exception {
        JarFile commonslang = new JarFile("./src/test/resources/commons-lang-2.6.jar");
        ModuleInspector.ModuleInspectResult commonslangResult = new ModuleInspector(commonslang).inspect();

        assertFalse(commonslangResult.isAutomaticModule);
        assertFalse(commonslangResult.isExplicitModule);
        assertNull(commonslangResult.modulename);
        assertNull(commonslangResult.moduleversion);
    }

    @Test
    void testJUnitPlatformCommonsMultiReleaseJar() throws Exception {
        JarFile junit = new JarFile("./src/test/resources/junit-platform-commons-1.2.0.jar");
        ModuleInspector.ModuleInspectResult junitResult = new ModuleInspector(junit).inspect();

        assertNotNull(junit);
        assertTrue(junitResult.isAutomaticModule);
        assertFalse(junitResult.isExplicitModule);
        assertEquals("org.junit.platform.commons", junitResult.modulename);
    }

    /**
     * {@code jar --list --file mrjar.jar}
     *
     * <pre><code>
     * META-INF/
     * META-INF/MANIFEST.MF
     * com/
     * com/acme/
     * com/acme/JdkSpecific.class
     * com/acme/Shared.class
     * META-INF/versions/
     * META-INF/versions/9/
     * META-INF/versions/9/com/
     * META-INF/versions/9/com/acme/
     * META-INF/versions/9/com/acme/JdkSpecific.class
     * META-INF/versions/9/module-info.class
     * </code></pre>
     *
     * <p>{@code jar --describe-module --file mrjar.jar}
     *
     * <pre><code>
     * releases: 9
     *
     * No root module descriptor, specify --release
     * </code></pre>
     *
     * <p>{@code jar --describe-module --file mrjar.jar --release 9}
     *
     * <pre><code>
     * releases: 9
     *
     * com.acme jar:file:///[...]/mrjar.jar/!META-INF/versions/9/module-info.class
     * exports com.acme
     * requires java.base mandated
     * </code></pre>
     */
    @Test
    void testMultiReleaseJarWithCompiledModuleDescriptorInVersionsDirectory() throws Exception {
        JarFile mrjar = new JarFile("./src/test/resources/mrjar.jar");
        ModuleInspector.ModuleInspectResult mrjarResult = new ModuleInspector(mrjar).inspect();

        assertNotNull(mrjar);
        assertFalse(mrjarResult.isAutomaticModule);
        assertTrue(mrjarResult.isExplicitModule);
        assertEquals("com.acme", mrjarResult.modulename);
    }
}
