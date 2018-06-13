package net.branchandbound.modulescanner;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {

        var dir = Paths.get(args.length > 0 ? args[0] : "../gs-maven-mirror");
        var cutoff = args.length > 1 ? args[1] : "20170101000000";
        Stream<Path> jarPathsToInspect = new MavenRepoWalker(dir, cutoff).getJarPathsToInspect();
        jarPathsToInspect
                .map(Main::toJarFile)
                .map(JarInspector::new)
                .forEach(j -> System.out.println(j.getModuleName()));

    }

    private static JarFile toJarFile(Path p) {
        try {
            return new JarFile(p.toFile());
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
