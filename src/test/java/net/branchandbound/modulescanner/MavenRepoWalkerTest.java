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


    private MavenRepoWalker repoWalker;

    @Before
    public void setup() {
        this.repoWalker = new MavenRepoWalker(Paths.get("./src/test/resources/test-maven-repo"));
    }

    @Test
    public void getJarPathsToInspect() {
        List<Path> jars = repoWalker.getJarPathsToInspect().collect(Collectors.toList());

        assertTrue(jars.stream().anyMatch(path -> path.endsWith("slf4j-api-1.8.0-beta2.jar")));
        assertTrue(jars.stream().anyMatch(path -> path.endsWith("jackson-core-2.9.6.jar")));
        assertEquals(2, jars.size());
    }
}