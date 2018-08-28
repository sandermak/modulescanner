package org.adoptopenjdk.modulescanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * This class is responsible for determining if a Maven Artifact has either a
 * module-info.java (explicit module support) or an AutoMatic-Module-Name:
 * entry in a META-INF file (automatic module support).
 */
public class ModuleInspector {

    private static final Logger LOGGER = LogManager.getLogger("ModuleInspector");

    private JarFile jarFile;

    /**
     * Constructor
     * @param jarFile - The Maven artifact to scan
     */
    public ModuleInspector(JarFile jarFile) {
        this.jarFile = jarFile;
    }

    /**
     * Inspect the Maven Artifact for whether it has Automatic and / or
     * explicit module support. When both Automatic-Module-Name and a
     * descriptor are present, the module is categorized as explicit.
     *
     * @return A ModuleInspectionResult
     */
    public ModuleInspectResult inspect() {
        Optional<ModuleDescriptor> descriptor = getModuleDescriptor();
        String automaticModuleName = getAutomaticModuleName();
        boolean isAutomaticModule = !(descriptor.isPresent() || automaticModuleName == null);
        boolean isExplicitModule = descriptor.isPresent();
        String moduleName =  isAutomaticModule ? automaticModuleName : descriptor.map(ModuleDescriptor::name).orElse(null);
        String moduleVersion = descriptor.flatMap(ModuleDescriptor::version).map(version -> version.toString()).orElse(null);
        List<String> dependencies = descriptor.map(ModuleDescriptor::requires)
                                              .map(requiresSet -> requiresSet.stream().map(ModuleDescriptor.Requires::name)
                                                                                      .collect(Collectors.toList()))
                                              .orElse(List.of());
        return new ModuleInspectResult(isAutomaticModule, isExplicitModule, moduleName, moduleVersion, dependencies);
    }

    // Get the module descriptor (module-info for explicit modules) if there is one
    private Optional<ModuleDescriptor> getModuleDescriptor() {
        return jarFile.stream()
                .filter(entry -> entry.getName().contains("module-info"))
                .findFirst()
                .map(entry -> {
                    try {
                        return ModuleDescriptor.read(jarFile.getInputStream(entry));
                    } catch (IOException ioe) {
                        LOGGER.error("Failed to get the module-info descriptor", ioe);
                        // TODO Not sure we want to throw a RuntimeException here
                        throw new RuntimeException(ioe);
                    }
                });
    }

    // Get Automatic-Module-Name (for automatic module support) if there is one
    private String getAutomaticModuleName() {
        try {
            return jarFile.getManifest().getMainAttributes().getValue("Automatic-Module-Name");
        } catch (IOException ioe) {
            LOGGER.error("Failed to get the Automatic-Module-Name", ioe);
            return null;
        }
    }

    /**
     * The Result of the module support inspection
     */
    public static class ModuleInspectResult {

        /** Is an an automatic module */
        public final boolean isAutomaticModule;
        /** Is an an explicit module */
        public final boolean isExplicitModule;
        /** The name of the module */
        public final String moduleName;
        /** The version of the module, this is actually a Maven artifact version as modules aren't versioned */
        public final String moduleVersion;
        /** The dependencies of this module */
        public final List<String> dependencies;

        /**
         * Constructor
         *
         * @param isAutomaticModule - Is an an automatic module
         * @param isExplicitModule - Is an an explicit module
         * @param moduleName - The name of the module
         * @param moduleVersion - The version of the module
         * @param dependencies - The dependencies of this module
         */
        public ModuleInspectResult(boolean isAutomaticModule, boolean isExplicitModule, String moduleName, String moduleVersion, List<String> dependencies) {
            this.isAutomaticModule = isAutomaticModule;
            this.isExplicitModule = isExplicitModule;
            this.moduleName = moduleName;
            this.moduleVersion = moduleVersion;
            this.dependencies = dependencies;
        }

        @Override
        public String toString() {
            return "ModuleInspectResult{" +
                    "isAutomaticModule=" + isAutomaticModule +
                    ", isExplicitModule=" + isExplicitModule +
                    ", moduleName='" + moduleName + '\'' +
                    ", moduleVersion='" + moduleVersion + '\'' +
                    ", dependencies=" + dependencies +
                    '}';
        }
    }
}
