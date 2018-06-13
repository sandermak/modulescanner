package net.branchandbound.modulescanner;

import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {

        var dir = Paths.get(args[0] != null ? args[0] : "./gs-maven-mirror");
	    //new JarInspector(dir).scan();
    }
}
