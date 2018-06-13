package net.branchandbound.modulescanner;

import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.util.Optional;
import java.util.jar.JarFile;

public class JarInspector {

    private JarFile jarFile;

    public JarInspector(JarFile jarFile) {
        this.jarFile = jarFile;
    }

    public boolean isAutomaticModule() {
            return getAutomaticModuleName() != null;
    }

    public boolean isExplicitModule() {
        return getModuleDescriptor().isPresent();
    }

    public String getModuleName() {
        return isAutomaticModule() ? getAutomaticModuleName() : getModuleDescriptor().map(ModuleDescriptor::name).orElse(null);
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
}
