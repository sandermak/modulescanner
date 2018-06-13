package net.branchandbound.modulescanner;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class MavenRepoWalkerTest {

    @Test
    public void getJarPathsToInspect() {
        MavenRepoWalker repoWalker = new MavenRepoWalker(Paths.get("./src/test/resources/test-maven-repo"), "20170101000000");
        List<Path> jars = repoWalker.getJarPathsToInspect().collect(Collectors.toList());

        assertTrue(jars.stream().anyMatch(path -> path.endsWith("slf4j-api-1.8.0-beta2.jar")));
        assertTrue(jars.stream().anyMatch(path -> path.endsWith("jackson-core-2.9.6.jar")));
        assertEquals(2, jars.size());
        jars.stream().forEach(p -> assertTrue(p.toFile().exists()));
    }

    @Test
    public void testCutoff() {
        MavenRepoWalker repoWalker = new MavenRepoWalker(Paths.get("./src/test/resources/test-maven-repo"), "20180501000000");

        assertEquals(1, repoWalker.getJarPathsToInspect().count());
    }

}