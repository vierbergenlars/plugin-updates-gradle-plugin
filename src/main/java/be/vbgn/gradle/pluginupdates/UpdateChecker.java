package be.vbgn.gradle.pluginupdates;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.update.Update;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.artifacts.LenientConfiguration;
import org.gradle.api.artifacts.ResolvedConfiguration;
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
                .filter(Configuration::isVisible)
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

        List<Dependency> upToDateDependencies = upToDateConfiguration
                .getAllModuleDependencies()
                .stream()
                .flatMap(DefaultDependency::fromGradle)
                .collect(Collectors.toList());
        ;

        LOGGER.debug("Resolved: {}", upToDateDependencies);

        Set<UnresolvedDependency> unresolvedDependencies = upToDateConfiguration.getUnresolvedModuleDependencies();
        LOGGER.debug("Unresolved: {}", unresolvedDependencies);
        unresolvedDependencies.forEach(unresolvedDependency -> {
            LOGGER.warn("Could not check up-to-date of {}", unresolvedDependency);
        });

        return oldConfiguration.getFirstLevelModuleDependencies().stream()
                .flatMap(DefaultDependency::fromGradle)
                .peek(dependency -> LOGGER
                        .debug("Dependency of {}: {}", configuration, dependency))
                .map(oldResolvedDependency -> new Update() {
                    @Override
                    public Dependency getOriginal() {
                        return oldResolvedDependency;
                    }

                    @Override
                    public List<Dependency> getUpdates() {
                        return Collections.singletonList(findDependency(upToDateDependencies, oldResolvedDependency));
                    }
                });
    }

    private static Dependency checkUpdatedDependencySingle(Project project, Configuration configuration,
            Dependency dependency, Transformer<Dependency, Dependency> dependencyTransformer) {
        Configuration upToDateConfiguration = configuration.copy();
        upToDateConfiguration.setTransitive(false);
        upToDateConfiguration.getDependencies().clear();
        upToDateConfiguration.getArtifacts().clear();

        org.gradle.api.artifacts.Dependency upToDateDependency = project.getDependencies()
                .create(dependencyTransformer.transform(dependency).toDependencyNotation());
        upToDateConfiguration.getDependencies().add(upToDateDependency);

        LenientConfiguration lenientUpToDateConfiguration = upToDateConfiguration.getResolvedConfiguration()
                .getLenientConfiguration();
        return null;

    }

    private static Dependency findDependency(
            Collection<Dependency> dependencies,
            Dependency dependency) {
        return dependencies.stream()
                .filter(dependency1 -> dependency.getGroup().equals(dependency1.getGroup()))
                .filter(dependency1 -> dependency.getName().equals(dependency1.getName()))
                .filter(dependency1 -> dependency.getClassifier().equals(dependency1.getClassifier()))
                .filter(dependency1 -> dependency.getType().equals(dependency1.getType()))
                .peek(dependency1 -> LOGGER.debug("Found {} as latest release of {}", dependency1, dependency))
                .findAny()
                .orElseGet(() -> {
                    LOGGER.info("Could not find any version for {}", dependency);
                    return null;
                });
    }

    private static Configuration createLatestConfiguration(Project project, Configuration configuration) {
        Configuration newConfiguration = configuration.copy();
        newConfiguration.setTransitive(false);
        newConfiguration.getDependencies().clear();

        Set<org.gradle.api.artifacts.Dependency> newDependencies = configuration.getDependencies().stream()
                .filter(dependency -> dependency instanceof ExternalDependency)
                .map(dependency -> (ExternalDependency) dependency)
                .flatMap(DefaultDependency::fromGradle)
                .map(dependency -> dependency.withVersion("+"))
                .peek(dependency -> LOGGER.debug("New dependency for {}: {}", newConfiguration, dependency))
                .map(dependency -> project.getDependencies().create(dependency.toDependencyNotation()))
                .collect(Collectors.toSet());

        newConfiguration.getDependencies().addAll(newDependencies);
        LOGGER.debug("Created {} with dependencies {}", newConfiguration, newDependencies);
        return newConfiguration;
    }

}
