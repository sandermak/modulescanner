package org.adoptopenjdk.modulescanner;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class MavenRepoWalkerTest {

    @Test
    void getJarPathsToInspect() {
        MavenRepoWalker repoWalker = new MavenRepoWalker(Paths.get("./src/test/resources/test-maven-repo"), "20170101000000");
        List<MavenRepoWalker.MavenArtifact> jars = repoWalker.getArtifactsToInspect().collect(Collectors.toList());

        assertTrue(jars.stream().anyMatch(artifact -> artifact.path.endsWith("slf4j-api-1.8.0-beta2.jar")));
        assertTrue(jars.stream().anyMatch(artifact -> artifact.path.endsWith("jackson-core-2.9.6.jar")));
        assertEquals(2, jars.size());
        jars.forEach(artifact -> assertTrue(artifact.path.toFile().exists()));
    }

    @Test
    void testCutoff() {
        MavenRepoWalker repoWalker = new MavenRepoWalker(Paths.get("./src/test/resources/test-maven-repo"), "20180501000000");

        assertEquals(1, repoWalker.getArtifactsToInspect().count());
    }

}