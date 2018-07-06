package org.adoptopenjdk.modulescanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * This class is responsible for walking a Maven repository
 */
public class MavenRepoWalker {

    private static final Logger LOGGER = LogManager.getLogger("MavenRepoWalker");

    private Path root;
    private String cutoffTimestamp;

    private static final int DEFAULT_MAX_DEPTH = 100;
    private static final String VERSION_TAG = "<latest>";
    private static final String GROUP_ID_TAG = "<groupId>";
    private static final String ARTIFACT_ID_TAG = "<artifactId>";
    private static final String LAST_UPDATED_TAG = "<lastUpdated>";
    private static final Pattern EXTRACT_VERSION_PATTERN = Pattern.compile(VERSION_TAG + "(.*)</latest>");
    private static final Pattern EXTRACT_GROUP_ID_PATTERN = Pattern.compile(GROUP_ID_TAG + "(.*)</groupId>");
    private static final Pattern EXTRACT_ARTIFACT_ID_PATTERN = Pattern.compile(ARTIFACT_ID_TAG + "(.*)</artifactId>");
    private static final Pattern LAST_UPDATED_PATTERN = Pattern.compile(LAST_UPDATED_TAG + "(.*)</lastUpdated>");

    /**
     * Constructor
     *
     * @param root - the root of the Maven repository to start walking from
     * @param cutoffTimestamp - the cutoff, e.g. Don't visit anything older
     */
    public MavenRepoWalker(Path root, String cutoffTimestamp) {
        this.root = root;
        this.cutoffTimestamp = cutoffTimestamp;
    }

    /**
     * Get the MavenArtifacts to inspect with various tools
     *
     * @return a stream of MavenArtifacts
     */
    public Stream<MavenArtifact> getArtifactsToInspect() {
        try {
            return Files.find(root, DEFAULT_MAX_DEPTH, (path, attrs) -> path.endsWith("maven-metadata.xml"))
                        .flatMap(this::getLatestMavenArtifact);
        } catch (IOException ioe) {
            LOGGER.error("Exception thrown during a find", ioe);
            return Stream.empty();
        }
    }

    // Get the latest version of a given artifact
    private Stream<MavenArtifact> getLatestMavenArtifact(Path path) {
        try {
            List<String> lines = Files.readAllLines(path);
            String groupId = findAndExtract(lines, GROUP_ID_TAG, EXTRACT_GROUP_ID_PATTERN);
            String artifactId = findAndExtract(lines, ARTIFACT_ID_TAG, EXTRACT_ARTIFACT_ID_PATTERN);
            String latestVersion = findAndExtract(lines, VERSION_TAG, EXTRACT_VERSION_PATTERN);
            String timestamp = findAndExtract(lines, LAST_UPDATED_TAG, LAST_UPDATED_PATTERN);
            String relativeLocation = latestVersion + File.separator + artifactId + "-" + latestVersion + ".jar";

            MavenArtifact artifact = new MavenArtifact(groupId, artifactId, latestVersion, path.getParent().resolve(relativeLocation));

            return timestamp.compareTo(cutoffTimestamp) > 0 ? Stream.of(artifact) : Stream.empty();
        } catch (Exception ioe) {
            LOGGER.error("Could not convert " + path + " into a MavenArtifact");
            return Stream.empty();
        }
    }

    // Find the tagged information via a Pattern
    private String findAndExtract(List<String> lines, String tag, Pattern pattern) {
        return lines
                .stream()
                .filter(l -> l.contains(tag))
                .map(l -> {
                    Matcher m = pattern.matcher(l);
                    m.find();
                    return m.group(1);
                }).findFirst().orElseThrow();
    }

    /**
     * A representation of a Maven Artifact
     */
    public static class MavenArtifact {

        /** Maven Group Id */
        public final String groupId;
        /** Maven Artifact Id */
        public final String artifactId;
        /** Maven Artifact version */
        public final String version;
        /** Path to the Maven Artifact */
        public final Path path;

        /**
         * Constructor
         *
         * @param groupId - The Maven Group Id
         * @param artifactId - The Maven Artifact Id
         * @param version - The version of the Maven Artifact
         * @param path - the Path to the Maven Artifact
         */
        public MavenArtifact(String groupId, String artifactId, String version, Path path) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.path = path;
        }

        @Override
        public String toString() {
            return "MavenArtifact{" +
                    "groupId='" + groupId + '\'' +
                    ", artifactId='" + artifactId + '\'' +
                    ", version='" + version + '\'' +
                    ", path=" + path +
                    '}';
        }
    }
}
