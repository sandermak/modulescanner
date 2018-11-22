package org.adoptopenjdk.modulescanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.spi.ToolProvider;
import java.util.stream.Collectors;

/**
 * This class is responsible for running jdeps on the JAR file it is inspecting
 * and returning the result of that inspection
 */
public class JdepsInspector {

    private static final Logger LOGGER = LogManager.getLogger("JdepsInspector");

    private Path jarFile;

    public JdepsInspector(Path jarFile) {
        this.jarFile = jarFile;
    }

    /**
     * Find the jdeps tool via the ToolsProvider, run it and then return a result
     *
     * @return The result of the jdeps inspection (could be a FAIL)
     */
    public JdepsInspectResult inspect() {
        Optional<ToolProvider> jdepsTool = ToolProvider.findFirst("jdeps");

        return jdepsTool.map(this::analyzeJdkInternals).orElse(JdepsInspectResult.FAIL);
    }

    // Analyse the JDK internal API usage using jdeps
    private JdepsInspectResult analyzeJdkInternals(ToolProvider jdeps) {

        var outbytes = new ByteArrayOutputStream();
        var errbytes = new ByteArrayOutputStream();

        try {
            int retVal = jdeps.run(new PrintStream(outbytes, true, Charset.defaultCharset()), new PrintStream(errbytes, true, Charset.defaultCharset()), "--jdk-internals", jarFile.toString());

            String jdepsOutput = outbytes.toString(Charset.defaultCharset());
            String errorOutput = errbytes.toString(Charset.defaultCharset());

            if (retVal != 0 || (errorOutput != null && errorOutput.length() > 0)) {
                return JdepsInspectResult.FAIL;
            }

            return new JdepsInspectResult(false, getViolations(jdepsOutput));
        } catch (RuntimeException re) {
            LOGGER.error("Could not process " + jarFile);
            LOGGER.trace(re);
            return JdepsInspectResult.FAIL;
        }
    }

    // Return a list of the jdeps violations
    private List<String> getViolations(String jdepsOutput) {
        String jdepsSeparator = "---------------------";
        int index = jdepsOutput.lastIndexOf(jdepsSeparator);
        if (index > 0) {
            String violations = jdepsOutput.substring(jdepsOutput.lastIndexOf(jdepsSeparator) + jdepsSeparator.length() + 1);
            LOGGER.info(violations);
            // break on any line break, filter blank lines, and collect as list
            return Arrays.stream(violations.split("\\R")).filter(line -> !line.trim().isEmpty()).collect(Collectors.toList());
        }

        return List.of();
    }

    /**
     * The result of a jdeps inspection
     */
    public static class JdepsInspectResult {

        /** A status indicating that the scan failed */
        public static final JdepsInspectResult FAIL = new JdepsInspectResult(true, List.of());
        public static final JdepsInspectResult SKIPPED = new JdepsInspectResult(false, List.of());

        /** Whether the jdeps tool errored or not */
        public final boolean toolerror;

        /** A List of the jdeps violations for this scan */
        public final List<String> violations;

        /**
         * Constructor
         *
         * @param toolerror - Set whether or not jdeps threw an error
         * @param violations - a List of the violations
         */
        public JdepsInspectResult(boolean toolerror, List<String> violations) {
            this.toolerror = toolerror;
            this.violations = violations;
        }

        @Override
        public String toString() {
            return "JdepsInspectResult{" +
                    "toolerror=" + toolerror +
                    ", violations=" + violations +
                    '}';
        }
    }
}
