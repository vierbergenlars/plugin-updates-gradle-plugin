package be.vbgn.gradle.pluginupdates.update.finder;

import be.vbgn.gradle.pluginupdates.StreamUtil;
import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.DefaultFailedDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.dependency.FailedDependency;
import be.vbgn.gradle.pluginupdates.update.finder.internal.InvalidResolvesCache;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    @Nonnull
    private VersionProvider versionProvider;

    @Nullable
    private InvalidResolvesCache invalidResolvesCache;

    public DefaultUpdateFinder(@Nonnull ScriptHandler scriptHandler, @Nonnull VersionProvider versionProvider) {
        this(scriptHandler.getDependencies(), scriptHandler.getConfigurations(), versionProvider);
    }

    public DefaultUpdateFinder(@Nonnull Project project, @Nonnull VersionProvider versionProvider) {
        this(project.getDependencies(), project.getConfigurations(), versionProvider);
    }

    public DefaultUpdateFinder(@Nonnull DependencyHandler dependencyHandler,
            @Nonnull ConfigurationContainer configurationContainer, @Nonnull VersionProvider versionProvider) {
        this.dependencyHandler = dependencyHandler;
        this.configurationContainer = configurationContainer;
        this.versionProvider = versionProvider;
    }


    @Override
    @Nonnull
    public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
        return StreamUtil.parallelIfNoDebug(this.versionProvider.getUpdateVersions(dependency))
                .flatMap(failureAllowedVersion -> {
                    Dependency toLookup = dependency.withVersion(failureAllowedVersion.getVersion());
                    Stream<Dependency> resolvedDependencies = resolveDependency(toLookup,
                            failureAllowedVersion.isFailureAllowed());

                    if (!failureAllowedVersion.isFailureAllowed()) {
                        return resolvedDependencies;
                    }
                    // If failure of a version is allowed, filter out all failed dependencies as if they were never asked for
                    // Failures may occur because we look up wildcards one level deeper than we have a version number.
                    return resolvedDependencies
                            .peek(dependency1 -> {
                                if (dependency1 instanceof FailedDependency) {
                                    LOGGER.debug("Suppressed failure to resolve {}", dependency1,
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

    private Stream<Dependency> resolveDependency(@Nonnull Dependency dependency, boolean failureAllowed) {
        if (failureAllowed && invalidResolvesCache != null) {
            Optional<FailedDependency> failedDependencyOptional = invalidResolvesCache.get(dependency);
            if (failedDependencyOptional.isPresent()) {
                LOGGER.trace("Found failed dependency in cache. Using failed dependency {}",
                        failedDependencyOptional.get());
                return Stream.of(failedDependencyOptional.get());
            }

            return resolveDependencyUncached(dependency)
                    .peek(dependency1 -> {
                        if (dependency1 instanceof FailedDependency) {
                            invalidResolvesCache.put(dependency);
                        }
                    });
        }

        return resolveDependencyUncached(dependency);

    }


    @Nonnull
    private Stream<Dependency> resolveDependencyUncached(@Nonnull Dependency dependency) {
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

    public void setInvalidResolvesCache(@Nonnull InvalidResolvesCache invalidResolvesCache) {
        this.invalidResolvesCache = invalidResolvesCache;
    }
}
