package org.adoptopenjdk.modulescanner;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;
import org.adoptopenjdk.modulescanner.MavenRepoWalker.MavenArtifact;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static String DEFAULT_OUTPUT_DELIMITER = ",";

    /**
     * Main method - entry point for invoking modulescanner
     *
     * @param args Commandline arguments
     */
    public static void main(String... args) {
        var directoryToScan = Paths.get(args.length > 0 ? args[0] : DEFAULT_DIRECTORY_TO_SCAN);
        var cutoffDate = args.length > 1 ? args[1] : CUTOFF_DATE;
        var output = Paths.get(args.length > 2 ? args[2] : DEFAULT_OUTPUT_FILE_NAME);

        LOGGER.info("Scanning for modules...");
        LOGGER.info("  directoryToScan = " + directoryToScan);
        LOGGER.info("       cutoffDate = " + cutoffDate);
        LOGGER.info("           output = " + output);

        try (var out = new PrintWriter(new BufferedWriter(new FileWriter(output.toFile())))) {
            walk(directoryToScan, cutoffDate, out);
            LOGGER.info("Wrote " + Files.size(output) + " bytes to: " + output);
        } catch (IOException ioe) {
            LOGGER.error("Creating output file " + output + " failed", ioe);
        }
    }

    // Walk repository and emit CSV file as output
    private static void walk(Path directoryToScan, String cutoffDate, PrintWriter out) {
        var printer = new SeparatedValuesPrinter(out, DEFAULT_OUTPUT_DELIMITER);
        printer.printHeaderLine();

        new MavenRepoWalker(directoryToScan, cutoffDate)
                .getArtifactsToInspect()
                .forEach(artifact -> handleArtifact(artifact, printer));

        out.flush();
        LOGGER.info("Printed " + printer.getLineCount() + " lines");
    }

    // Inspect artifact and emit CSV line
    private static void handleArtifact(MavenArtifact artifact, SeparatedValuesPrinter printer) {
        JarFile jarFile = toJarFile(artifact.path);
        if (jarFile != null) {
            var moduleInspectorResult = new ModuleInspector(jarFile).inspect();
            var jdepsInspectorResult = moduleInspectorResult.isExplicitModule ?
                    JdepsInspector.JdepsInspectResult.SKIPPED : new JdepsInspector(artifact.path).inspect();
            LOGGER.info(artifact + "\n -> " + moduleInspectorResult + "\n -> " + jdepsInspectorResult);
            printer.printAndCountLine(artifact, moduleInspectorResult, jdepsInspectorResult);
        }
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

}
