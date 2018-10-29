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
                .flatMap(failureAllowedVersion -> {
                    Dependency toLookup = dependency.withVersion(failureAllowedVersion.version);
                    Stream<Dependency> resolvedDependencies = resolveDependency(toLookup);

                    if (!failureAllowedVersion.failureAllowed) {
                        return resolvedDependencies;
                    }
                    // If failure of a version is allowed, filter out all failed dependencies as if they were never asked for
                    // Failures may occur because we look up wildcards one level deeper than we have a version number.
                    return resolvedDependencies
                            .peek(dependency1 -> {
                                if (dependency1 instanceof FailedDependency) {
                                    LOGGER.debug("Suppressed failure to resolve {}: {}", dependency1,
                                            ((FailedDependency) dependency1).getProblem());
                                }
                            })
                            .filter(dependency1 -> !(dependency1 instanceof FailedDependency));
                })
                .peek(dependency1 -> {
                    if (dependency1 instanceof FailedDependency) {
                        LOGGER.warn("Could not resolve {}", dependency1);
                        LOGGER.debug("Resolve exception", ((FailedDependency) dependency1).getProblem());
                    }
                });
    }

    @Nonnull
    private Stream<FailureAllowedVersion> generateVersionConstraints(@Nonnull Version version) {
        LOGGER.debug("Generating update constraints for version {}", version);
        NumberWildcard wildcard = NumberWildcard.wildcard();
        Set<FailureAllowedVersion> versions = new HashSet<>();
        versions.add(new FailureAllowedVersion(version.withMajor(wildcard), version.getMajor().isEmpty()));
        if (!version.getMajor().isEmpty()) {
            versions.add(new FailureAllowedVersion(version.withMinor(wildcard), version.getMinor().isEmpty()));
        }
        if (!version.getMinor().isEmpty()) {
            versions.add(new FailureAllowedVersion(version.withMicro(wildcard), version.getMicro().isEmpty()));
        }
        if (!version.getMicro().isEmpty()) {
            versions.add(new FailureAllowedVersion(version.withPatch(wildcard), version.getMicro().isEmpty()));
        }
        return versions.stream()
                .distinct()
                .peek(version1 -> LOGGER.debug("Update constraint: {}, failure allowed: {}", version1.version,
                        version1.failureAllowed));
    }

    @Nonnull
    private Stream<Dependency> resolveDependency(@Nonnull Dependency dependency) {
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

    private static class FailureAllowedVersion {

        private Version version;
        private boolean failureAllowed;

        private FailureAllowedVersion(Version version, boolean failureAllowed) {
            this.version = version;
            this.failureAllowed = failureAllowed;
        }
    }
}
