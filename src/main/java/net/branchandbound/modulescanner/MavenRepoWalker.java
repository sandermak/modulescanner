package net.branchandbound.modulescanner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MavenRepoWalker {

    private Path root;
    private String cutoffTimestamp;

    private static final String versionTag = "<latest>";
    private static final String artifactIdTag = "<artifactId>";
    private static final String lastupdatedTag = "<lastUpdated>";
    private static final Pattern extractVersionPattern = Pattern.compile(versionTag + "(.*)</latest>");
    private static final Pattern extractArtifactIdPattern = Pattern.compile(artifactIdTag + "(.*)</artifactId>");
    private static final Pattern lastupdatedPattern = Pattern.compile(lastupdatedTag + "(.*)</lastUpdated>");

    public MavenRepoWalker(Path root, String cutoffTimestamp) {
        this.root = root;
        this.cutoffTimestamp = cutoffTimestamp;
    }

    public Stream<Path> getJarPathsToInspect() {
        try {
            return Files.find(root, 100, (path, attrs) -> path.endsWith("maven-metadata.xml"))
                        .flatMap(this::getLatestJar);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return Stream.empty();
        }
    }

    private Stream<Path> getLatestJar(Path path) {
        try {
            Path artifactsDir = path.getParent();
            Optional<String> latestArtifactName = getLatestArtifactLocation(path);

            return latestArtifactName.map(a -> artifactsDir.resolve(a)).stream();
        } catch (Exception ioe) {
            System.out.println("Could not process " + path);
            return Stream.empty();
        }
    }

    private Optional<String> getLatestArtifactLocation(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        String artifactId = findAndExtract(lines.stream(), artifactIdTag, extractArtifactIdPattern);
        String latestVersion = findAndExtract(lines.stream(), versionTag, extractVersionPattern);
        String timestamp = findAndExtract(lines.stream(), lastupdatedTag, lastupdatedPattern);

        return timestamp.compareTo(cutoffTimestamp) > 0 ? Optional.of(latestVersion + File.separator + artifactId + "-" + latestVersion + ".jar") : Optional.empty();
    }

    private String findAndExtract(Stream<String> stream, String tag, Pattern pattern) {
        return stream.filter(l -> l.contains(tag))
                .map(l -> {
                    Matcher m = pattern.matcher(l);
                    m.find();
                    return m.group(1);
                }).findFirst().orElseThrow();
    }

}
