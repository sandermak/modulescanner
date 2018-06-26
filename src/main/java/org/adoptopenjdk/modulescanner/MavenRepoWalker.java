package org.adoptopenjdk.modulescanner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MavenRepoWalker {

    private Path root;
    private String cutoffTimestamp;

    private static final String versionTag = "<latest>";
    private static final String groupIdTag = "<groupId>";
    private static final String artifactIdTag = "<artifactId>";
    private static final String lastupdatedTag = "<lastUpdated>";
    private static final Pattern extractVersionPattern = Pattern.compile(versionTag + "(.*)</latest>");
    private static final Pattern extractGroupIdPattern = Pattern.compile(groupIdTag + "(.*)</groupId>");
    private static final Pattern extractArtifactIdPattern = Pattern.compile(artifactIdTag + "(.*)</artifactId>");
    private static final Pattern lastupdatedPattern = Pattern.compile(lastupdatedTag + "(.*)</lastUpdated>");

    public MavenRepoWalker(Path root, String cutoffTimestamp) {
        this.root = root;
        this.cutoffTimestamp = cutoffTimestamp;
    }

    public Stream<MavenArtifact> getArtifactsToInspect() {
        try {
            return Files.find(root, 100, (path, attrs) -> path.endsWith("maven-metadata.xml"))
                        .flatMap(this::getLatestMavenArtifact);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return Stream.empty();
        }
    }

    private Stream<MavenArtifact> getLatestMavenArtifact(Path path) {
        try {
            List<String> lines = Files.readAllLines(path);
            String groupId = findAndExtract(lines, groupIdTag, extractGroupIdPattern);
            String artifactId = findAndExtract(lines, artifactIdTag, extractArtifactIdPattern);
            String latestVersion = findAndExtract(lines, versionTag, extractVersionPattern);
            String timestamp = findAndExtract(lines, lastupdatedTag, lastupdatedPattern);
            String relativeLocation = latestVersion + File.separator + artifactId + "-" + latestVersion + ".jar";

            MavenArtifact artifact = new MavenArtifact(groupId, artifactId, latestVersion, path.getParent().resolve(relativeLocation));

            return timestamp.compareTo(cutoffTimestamp) > 0 ? Stream.of(artifact) : Stream.empty();
        } catch (Exception ioe) {
            System.err.println("Could not process " + path);
            return Stream.empty();
        }
    }

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

    public static class MavenArtifact {
        public final String groupId;
        public final String artifactId;
        public final String version;
        public final Path path;

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
