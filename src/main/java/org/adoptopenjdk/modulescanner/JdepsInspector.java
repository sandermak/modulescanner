package org.adoptopenjdk.modulescanner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.spi.ToolProvider;

public class JdepsInspector {

    private Path jarFile;

    public JdepsInspector(Path jarFile) {
        this.jarFile = jarFile;
    }

    public JdepsInspectResult inspect() {
        Optional<ToolProvider> jdepsTool = ToolProvider.findFirst("jdeps");

        return jdepsTool.map(this::analyzeJdkInternals).orElse(JdepsInspectResult.FAIL_RESULT);
    }

    private JdepsInspectResult analyzeJdkInternals(ToolProvider jdeps) {
        var outbytes = new ByteArrayOutputStream();
        var errbytes = new ByteArrayOutputStream();

        try {
            int retVal = jdeps.run(new PrintStream(outbytes), new PrintStream(errbytes), "--jdk-internals", jarFile.toString());

            String out = outbytes.toString(Charset.defaultCharset());
            String err = errbytes.toString(Charset.defaultCharset());

            if (retVal != 0 || (err != null && err.length() > 0)) {
                return JdepsInspectResult.FAIL_RESULT;
            }

            return new JdepsInspectResult(false, getViolations(out));
        } catch (RuntimeException re) {
            System.err.println("Could not process " + jarFile);
            re.printStackTrace();
            return JdepsInspectResult.FAIL_RESULT;
        }
    }

    private List<String> getViolations(String out) {
        String jdepsSeparator = "---------------------";
        int index = out.lastIndexOf(jdepsSeparator);
        if (index > 0) {
            String violations = out.substring(out.lastIndexOf(jdepsSeparator) + jdepsSeparator.length() + 1);
            System.out.println(violations);
            return Arrays.asList(violations.split("\\r?\\n"));
        }

        return List.of();
    }

    public static class JdepsInspectResult {

        public static JdepsInspectResult FAIL_RESULT = new JdepsInspectResult(true, List.of());

        public final boolean toolerror;
        public final List<String> violations;

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
