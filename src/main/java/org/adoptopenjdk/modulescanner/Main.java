package org.adoptopenjdk.modulescanner;

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

    /**
     * Main method - entry point for invoking modulescanner
     *
     * @param args Commandline arguments
     */
    public static void main(String[] args) {
        var directoryToScan = Paths.get(args.length > 0 ? args[0] : DEFAULT_DIRECTORY_TO_SCAN);
        var cutoffDate = args.length > 1 ? args[1] : CUTOFF_DATE;

        new MavenRepoWalker(directoryToScan, cutoffDate).getArtifactsToInspect()
                .forEach(artifact -> {
                    JarFile jarFile = toJarFile(artifact.path);
                    if (jarFile != null) {
                        ModuleInspector.ModuleInspectResult moduleInspectorResult = new ModuleInspector(jarFile).inspect();
                        JdepsInspector.JdepsInspectResult jdepsInspectorResult = new JdepsInspector(artifact.path).inspect();
                        LOGGER.info(artifact + "\n -> " + moduleInspectorResult + "\n -> " + jdepsInspectorResult);
                    }
                });
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
