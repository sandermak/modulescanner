package net.branchandbound.modulescanner;

import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class JarInspector {

    private JarFile jarFile;

    public JarInspector(JarFile jarFile) {
        this.jarFile = jarFile;
    }

    public JarInspectResult inspect() {
        Optional<ModuleDescriptor> descriptor = getModuleDescriptor();
        String automaticModuleName = getAutomaticModuleName();
        boolean isAutomaticModule = automaticModuleName != null;
        boolean isExplicitModule = descriptor.isPresent();
        String modulename =  isAutomaticModule ? automaticModuleName : descriptor.map(ModuleDescriptor::name).orElse(null);
        String moduleversion = descriptor.flatMap(ModuleDescriptor::version).map(v -> v.toString()).orElse(null);
        List<String> dependencies = descriptor.map(ModuleDescriptor::requires).map(s -> s.stream().map(ModuleDescriptor.Requires::name).collect(Collectors.toList())).orElse(List.of());
        return new JarInspectResult(isAutomaticModule, isExplicitModule, modulename, moduleversion, dependencies);
    }

    private Optional<ModuleDescriptor> getModuleDescriptor() {
        return jarFile.stream()
                .filter(entry -> entry.getName().contains("module-info"))
                .findFirst()
                .map(entry -> {
                    try {
                        return ModuleDescriptor.read(jarFile.getInputStream(entry));
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });
    }

    private String getAutomaticModuleName() {
        try {
            return jarFile.getManifest().getMainAttributes().getValue("Automatic-Module-Name");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    public static class JarInspectResult {
        public final boolean isAutomaticModule;
        public final boolean isExplicitModule;
        public final String modulename;
        public final String moduleversion;
        public final List<String> dependencies;

        public JarInspectResult(boolean isAutomaticModule, boolean isExplicitModule, String modulename, String moduleversion, List<String> dependencies) {
            this.isAutomaticModule = isAutomaticModule;
            this.isExplicitModule = isExplicitModule;
            this.modulename = modulename;
            this.moduleversion = moduleversion;
            this.dependencies = dependencies;
        }

        @Override
        public String toString() {
            return "JarInspectResult{" +
                    "isAutomaticModule=" + isAutomaticModule +
                    ", isExplicitModule=" + isExplicitModule +
                    ", modulename='" + modulename + '\'' +
                    ", moduleversion='" + moduleversion + '\'' +
                    ", dependencies=" + dependencies +
                    '}';
        }
    }
}
