package net.branchandbound.modulescanner;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

public class Main {

    public static void main(String[] args) {
        var dir = Paths.get(args.length > 0 ? args[0] : "../gs-maven-mirror");
        var cutoff = args.length > 1 ? args[1] : "20170101000000";

        new MavenRepoWalker(dir, cutoff).getArtifactsToInspect()
                .forEach(artifact -> {
                    JarFile jarFile = toJarFile(artifact.path);
                    JarInspector.JarInspectResult result1 = new JarInspector(jarFile).inspect();
                    JdepsInspector.JdepsInspectResult result2  = new JdepsInspector(artifact.path).inspect();
                    System.out.println(artifact + "\n -> " + result1 + "\n -> " + result2);
                });
    }

    private static JarFile toJarFile(Path p) {
        try {
            return new JarFile(p.toFile());
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

}
