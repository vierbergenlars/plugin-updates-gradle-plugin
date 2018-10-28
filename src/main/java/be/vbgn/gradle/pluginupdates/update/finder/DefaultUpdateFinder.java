package be.vbgn.gradle.pluginupdates.update.finder;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.DefaultFailedDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.dependency.FailedDependency;
import be.vbgn.gradle.pluginupdates.version.NumberWildcard;
import be.vbgn.gradle.pluginupdates.version.Version;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.LenientConfiguration;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class DefaultUpdateFinder implements UpdateFinder {

    private static Logger LOGGER = Logging.getLogger(DefaultUpdateFinder.class);

    @Nonnull
    private DependencyHandler dependencyHandler;
    @Nonnull
    private ConfigurationContainer configurationContainer;

    public DefaultUpdateFinder(@Nonnull ScriptHandler scriptHandler) {
        this(scriptHandler.getDependencies(), scriptHandler.getConfigurations());
    }

    public DefaultUpdateFinder(@Nonnull Project project) {
        this(project.getDependencies(), project.getConfigurations());
    }

    public DefaultUpdateFinder(@Nonnull DependencyHandler dependencyHandler,
            @Nonnull ConfigurationContainer configurationContainer) {
        this.dependencyHandler = dependencyHandler;
        this.configurationContainer = configurationContainer;
    }


    @Override
    @Nonnull
    public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
        Version dependencyVersion = dependency.getVersion();

        return generateVersionConstraints(dependencyVersion)
                .flatMap(version -> findUpdatedDependency(dependency.withVersion(version)));
    }

    @Nonnull
    private Stream<Version> generateVersionConstraints(@Nonnull Version version) {
        LOGGER.debug("Generating update constraints for version {}", version);
        NumberWildcard wildcard = NumberWildcard.wildcard();
        Set<Version> versions = new HashSet<>();
        versions.add(version.withMajor(wildcard));
        if (!version.getMajor().isEmpty()) {
            versions.add(version.withMinor(wildcard));
        }
        if (!version.getMinor().isEmpty()) {
            versions.add(version.withMicro(wildcard));
        }
        if (!version.getMicro().isEmpty()) {
            versions.add(version.withPatch(wildcard));

        }
        return versions.stream()
                .distinct()
                .peek(version1 -> LOGGER.debug("Update constraint: {}", version1));
    }

    @Nonnull
    private Stream<Dependency> findUpdatedDependency(@Nonnull Dependency dependency) {
        LOGGER.debug("Resolving dependency {}", dependency);
        org.gradle.api.artifacts.Dependency updatedDependency = dependencyHandler
                .create(dependency.toDependencyNotation());
        Configuration updatedConfiguration = configurationContainer.detachedConfiguration(updatedDependency);
        updatedConfiguration.setTransitive(false);
        updatedConfiguration.setVisible(false);

        LenientConfiguration updatedLenientConfiguration = updatedConfiguration.getResolvedConfiguration()
                .getLenientConfiguration();

        updatedLenientConfiguration.getUnresolvedModuleDependencies()
                .forEach(unresolvedDependency -> LOGGER
                        .warn("Could not check updates for {}: {}", dependency,
                                unresolvedDependency.getProblem().getMessage(), unresolvedDependency.getProblem()));

        Stream<FailedDependency> failedDependencies = updatedLenientConfiguration.getUnresolvedModuleDependencies()
                .stream()
                .map(DefaultFailedDependency::fromGradle)
                .peek(dependency1 -> LOGGER
                        .warn("Could not check updates for {}:  {}", dependency1,
                                dependency1.getProblem().getMessage()));
        Stream<Dependency> updatedDependencies = updatedLenientConfiguration.getAllModuleDependencies().stream()
                .flatMap(DefaultDependency::fromGradle);

        return Stream.concat(failedDependencies, updatedDependencies)
                .peek(dependency1 -> LOGGER.debug("Resolved dependency {} to {}", dependency, dependency1));
    }
}
