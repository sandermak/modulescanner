package org.adoptopenjdk.modulescanner;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.util.List;
import org.adoptopenjdk.modulescanner.MavenRepoWalker.MavenArtifact;
import org.adoptopenjdk.modulescanner.ModuleInspector.ModuleInspectResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

/**
 * The main entry point into the modulescanner project
 *
 * Is currently designed to be run from the CL
 */
public class Main {

    private static final Logger LOGGER = LogManager.getLogger("Main");

    private static String DEFAULT_DIRECTORY_TO_SCAN = "../gs-maven-mirror";
    private static String CUTOFF_DATE = "20170101000000";
    private static String DEFAULT_OUTPUT_FILE_NAME = "modulescanner.csv";
    private static String DEFAULT_OUTPUT_DELIMITER = ", ";

    /**
     * Main method - entry point for invoking modulescanner
     *
     * @param args Commandline arguments
     */
    public static void main(String[] args) {
        var directoryToScan = Paths.get(args.length > 0 ? args[0] : DEFAULT_DIRECTORY_TO_SCAN);
        var cutoffDate = args.length > 1 ? args[1] : CUTOFF_DATE;
        var outputFileName = args.length > 2 ? args[2] : DEFAULT_OUTPUT_FILE_NAME;

        // walk(directoryToScan, cutoffDate, new PrintWriter(System.out));

        try (var fw = new FileWriter(outputFileName); var bw = new BufferedWriter(fw); var out = new PrintWriter(bw)) {
            walk(directoryToScan, cutoffDate, out);
        } catch (IOException ioe) {
            LOGGER.error("Walking directory: " + directoryToScan + " failed", ioe);
            throw new UncheckedIOException("", ioe);
        }
    }

    private static void walk(Path directoryToScan, String cutoffDate, PrintWriter out) {
        out.println(toHead());
        new MavenRepoWalker(directoryToScan, cutoffDate).getArtifactsToInspect()
                .forEach(artifact -> {
                    JarFile jarFile = toJarFile(artifact.path);
                    if (jarFile != null) {
                        var moduleInspectorResult = new ModuleInspector(jarFile).inspect();
                        var jdepsInspectorResult = new JdepsInspector(artifact.path).inspect();
                        LOGGER.info(artifact + "\n -> " + moduleInspectorResult + "\n -> " + jdepsInspectorResult);
                        out.println(toLine(artifact, moduleInspectorResult));
                    }
                });
        out.flush();
    }

    // Convert a given Path to a Jar file for processing
    private static JarFile toJarFile(Path path) {
        try {
            return new JarFile(path.toFile());
        } catch (IOException ioe) {
            LOGGER.warn("Unable to convert Path: " + path.toAbsolutePath() + " to a JAR file", ioe);
            return null;
        }
    }

    // Create CSV header as string
    private static String toHead() {
        var columns = List.of(
                "groupId",
                "artifactId",
                "version",
                "moduleName",
                "moduleVersion",
                "moduleMode",
                "moduleDependencies");
        return String.join(DEFAULT_OUTPUT_DELIMITER, columns);
    }

    // Create single CSV line from artifact and module inspection result
    private static String toLine(MavenArtifact artifact, ModuleInspectResult mir) {
        var columns = List.of(
                valueOrDashIfBlank(artifact.groupId),
                valueOrDashIfBlank(artifact.artifactId),
                valueOrDashIfBlank(artifact.version),
                valueOrDashIfBlank(mir.moduleName),
                valueOrDashIfBlank(mir.moduleVersion),
                mir.isAutomaticModule ? "automatic" : mir.isExplicitModule ? "explicit" : "-",
                valueOrDashIfBlank(String.join(" + ", mir.dependencies)));
        return String.join(DEFAULT_OUTPUT_DELIMITER, columns);
    }

    // Convert null or blank values to "-".
    private static String valueOrDashIfBlank(String value) {
        return (value == null || value.trim().isEmpty() ) ? "-" : value;
    }
}
