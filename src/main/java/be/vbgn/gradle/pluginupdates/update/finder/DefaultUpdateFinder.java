package be.vbgn.gradle.pluginupdates.update.finder;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.dependency.FailedDependency;
import be.vbgn.gradle.pluginupdates.update.resolver.DefaultDependencyResolver;
import be.vbgn.gradle.pluginupdates.update.resolver.DependencyResolver;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class DefaultUpdateFinder implements UpdateFinder {

    private static final Logger LOGGER = Logging.getLogger(DefaultUpdateFinder.class);

    @Nonnull
    private VersionProvider versionProvider;

    @Nonnull
    private DependencyResolver dependencyResolver;

    DefaultUpdateFinder(@Nonnull ScriptHandler scriptHandler, @Nonnull VersionProvider versionProvider) {
        this(new DefaultDependencyResolver(scriptHandler.getDependencies(), scriptHandler.getConfigurations()),
                versionProvider);
    }

    public DefaultUpdateFinder(@Nonnull DependencyResolver dependencyResolver,
            @Nonnull VersionProvider versionProvider) {
        this.dependencyResolver = dependencyResolver;
        this.versionProvider = versionProvider;
    }


    @Override
    @Nonnull
    public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
        return this.versionProvider.getUpdateVersions(dependency)
                .flatMap(failureAllowedVersion -> {
                    Dependency toLookup = dependency.withVersion(failureAllowedVersion.getVersion());
                    Stream<Dependency> resolvedDependencies = dependencyResolver.resolve(toLookup);

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
}
