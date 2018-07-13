package org.adoptopenjdk.modulescanner;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void testCsvOutput() throws Exception {
        var testMavenRepo = Paths.get("src/test/resources/test-maven-repo");
        var targetOutput = Files.createTempFile("actual-", "-modulescanner.csv");

        Main.main(testMavenRepo.toString(), "20170101000000", targetOutput.toString());

        var expectedLines = Files.readAllLines(testMavenRepo.resolve("expected-modulescanner.csv"));
        var actualLines = Files.readAllLines(targetOutput);
        assertLinesMatch(expectedLines, actualLines);
    }
}
