package org.adoptopenjdk.modulescanner;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.adoptopenjdk.modulescanner.MavenRepoWalker.MavenArtifact;
import org.adoptopenjdk.modulescanner.ModuleInspector.ModuleInspectResult;
import org.adoptopenjdk.modulescanner.JdepsInspector.JdepsInspectResult;

class SeparatedValuesPrinter {

	private final PrintWriter out;
	private final String delimiter;
	private final AtomicInteger lineCounter;

	SeparatedValuesPrinter(PrintWriter out, String delimiter) {
		this.out = out;
		this.delimiter = delimiter;
		this.lineCounter = new AtomicInteger();
	}

	int getLineCount() {
		return lineCounter.get();
	}

	// Create CSV header as string
	// Note: Keep in sync with #generateLine
	private String generateHeaderLine() {
		var columns = List.of(
				"groupId",
				"artifactId",
				"version",
				"moduleName",
				"moduleVersion",
				"moduleMode",
				"moduleDependencies",
				"jdepsToolError",
				"jdepsViolations");
		return String.join(delimiter, columns);
	}

	// Create single CSV line from artifact and module inspection result
	// Note: Keep in sync with #generateHeaderLine
	private String generateLine(MavenArtifact artifact, ModuleInspectResult mir, JdepsInspectResult jir) {
		var columns = List.of(
				valueOrDashIfBlank(artifact.groupId),
				valueOrDashIfBlank(artifact.artifactId),
				valueOrDashIfBlank(artifact.version),
				valueOrDashIfBlank(mir.moduleName),
				valueOrDashIfBlank(mir.moduleVersion),
				mir.isAutomaticModule ? "automatic" : mir.isExplicitModule ? "explicit" : "?",
				valueOrDashIfBlank(String.join(" + ", mir.dependencies)),
				jir.toolerror + "",
				valueOrDashIfBlank(String.join(" + ", jir.violations))
				);
		return String.join(delimiter, columns);
	}

	// Convert null or blank values to "-".
	private static String valueOrDashIfBlank(String value) {
		return (value == null || value.trim().isEmpty() ) ? "-" : value;
	}

	void printHeaderLine() {
		out.println(generateHeaderLine());
	}

	void printAndCountLine(MavenArtifact artifact, ModuleInspectResult mir, JdepsInspectResult jir) {
		out.println(generateLine(artifact, mir, jir));
		lineCounter.incrementAndGet();
	}
}
