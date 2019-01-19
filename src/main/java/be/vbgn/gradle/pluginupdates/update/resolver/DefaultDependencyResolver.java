package be.vbgn.gradle.pluginupdates.update.resolver;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.DefaultFailedDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.dependency.FailedDependency;
import java.util.stream.Stream;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.LenientConfiguration;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class DefaultDependencyResolver implements DependencyResolver {
    private static final Logger LOGGER = Logging.getLogger(DefaultDependencyResolver.class);
    private DependencyHandler dependencyHandler;
    private ConfigurationContainer configurationContainer;

    public DefaultDependencyResolver(DependencyHandler dependencyHandler,
            ConfigurationContainer configurationContainer) {
        this.dependencyHandler = dependencyHandler;
        this.configurationContainer = configurationContainer;
    }

    @Override
    public Stream<Dependency> resolve(Dependency dependency) {
        LOGGER.debug("Resolving dependency {}", dependency);
        org.gradle.api.artifacts.Dependency updatedDependency = dependencyHandler
                .create(dependency.toDependencyNotation());
        Configuration updatedConfiguration = configurationContainer.detachedConfiguration(updatedDependency);
        updatedConfiguration.setTransitive(false);
        updatedConfiguration.setVisible(false);

        LenientConfiguration updatedLenientConfiguration = updatedConfiguration.getResolvedConfiguration()
                .getLenientConfiguration();

        Stream<FailedDependency> failedDependencies = updatedLenientConfiguration.getUnresolvedModuleDependencies()
                .stream()
                .map(DefaultFailedDependency::fromGradle);
        Stream<Dependency> updatedDependencies = updatedLenientConfiguration.getAllModuleDependencies().stream()
                .flatMap(DefaultDependency::fromGradle);

        return Stream.concat(failedDependencies, updatedDependencies)
                .peek(dependency1 -> LOGGER.debug("Resolved dependency {} to {}", dependency, dependency1));
    }
}
