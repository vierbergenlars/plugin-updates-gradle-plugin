package be.vbgn.gradle.pluginupdates;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.artifacts.LenientConfiguration;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.artifacts.UnresolvedDependency;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class UpdateChecker {

    public static Logger LOGGER = Logging.getLogger(UpdateChecker.class);

    public static Stream<Update> checkBuildscriptUpdates(Project project) {
        return checkUpdates(project, project.getBuildscript().getConfigurations().getAt("classpath"));
    }

    public static Stream<Update> checkUpdates(Project project) {
        return checkUpdates(project, project.getConfigurations());
    }

    public static Stream<Update> checkUpdates(Project project, ConfigurationContainer configurationContainer) {
        LOGGER.debug("Checking updates for {}, {}", project, configurationContainer);
        return configurationContainer.stream()
                .filter(configuration -> configuration.isVisible())
                .flatMap(configuration -> checkUpdates(project, configuration))
                .peek(update -> LOGGER.info("Found update {}", update));
    }

    public static Stream<Update> checkUpdates(Project project, Configuration configuration) {
        LOGGER.debug("Checking updates for {}, {}", project, configuration);
        if (configuration.isEmpty()) {
            LOGGER.debug("{} is empty, no update checking needed.", configuration);
            return Stream.empty();
        }
        ResolvedConfiguration oldConfiguration = configuration.getResolvedConfiguration();
        LenientConfiguration upToDateConfiguration = createLatestConfiguration(project, configuration)
                .getResolvedConfiguration().getLenientConfiguration();

        Set<ResolvedDependency> upToDateDependencies = upToDateConfiguration.getAllModuleDependencies();
        LOGGER.debug("Resolved: {}", upToDateDependencies);

        Set<UnresolvedDependency> unresolvedDependencies = upToDateConfiguration.getUnresolvedModuleDependencies();
        LOGGER.debug("Unresolved: {}", unresolvedDependencies);
        unresolvedDependencies.forEach(unresolvedDependency -> {
            LOGGER.warn("Could not check up-to-date of {}", unresolvedDependency);
        });

        return oldConfiguration.getFirstLevelModuleDependencies().stream()
                .peek(dependency -> LOGGER
                        .debug("Dependency of {}: {}", configuration, dependency))
                .map(oldResolvedDependency -> new Update(project, configuration, oldResolvedDependency,
                        findDependency(upToDateDependencies, oldResolvedDependency)));
    }

    private static ResolvedDependency findDependency(Set<ResolvedDependency> dependencies,
            ResolvedDependency dependency) {
        return dependencies.stream()
                .filter(dependency1 -> dependency.getModuleGroup().equals(dependency1.getModuleGroup()))
                .filter(dependency1 -> dependency.getModuleName().equals(dependency1.getModuleName()))
                .peek(dependency1 -> LOGGER.debug("Found {} as latest release of {}", dependency1, dependency))
                .findAny()
                .orElseGet(() -> {
                    LOGGER.info("Could not find any version for {}", dependency);
                    return null;
                });
    }

    private static Configuration createLatestConfiguration(Project project, Configuration configuration) {
        Set<Dependency> dependencies = configuration.getDependencies();

        Stream<Dependency> newDependenciesStream = dependencies.stream()
                .filter(dependency -> dependency instanceof ExternalDependency)
                .flatMap(dependency -> createLatestDependency(project, (ExternalDependency) dependency));

        Configuration newConfiguration = configuration.copy();
        newConfiguration.setTransitive(false);
        newConfiguration.getDependencies().clear();

        Set<Dependency> newDependencies = newDependenciesStream.collect(Collectors.toSet());
        newConfiguration.getDependencies().addAll(newDependencies);
        LOGGER.debug("Created {} with dependencies {}", newConfiguration, newDependencies);
        return newConfiguration;
    }

    private static Stream<Dependency> createLatestDependency(Project project, ExternalDependency dependency) {
        String dependencyNotation =
                dependency.getGroup() + ":" + dependency.getName() + ":+";
        if (dependency.getArtifacts().isEmpty()) {
            LOGGER.debug("Creating new dependency {}", dependencyNotation);
            return Stream.of(project.getDependencies().create(dependencyNotation));
        }
        return dependency.getArtifacts().stream()
                .map(artifact -> dependencyNotation + "@" + artifact.getType())
                .peek(dependency1 -> LOGGER.debug("Creating new dependency {}", dependency1))
                .map(dependency1 -> project.getDependencies().create(dependency1))
                ;
        /*LOGGER.debug("Creating new dependency from {} with notation {}", dependency, dependencyNotation);
        ExternalDependency newDependency = (ExternalDependency) project.getDependencies().create(dependencyNotation);
        newDependency.getArtifacts().addAll(dependency.getArtifacts());
        return newDependency;*/
    }

}
