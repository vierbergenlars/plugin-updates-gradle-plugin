package be.vbgn.gradle.pluginupdates.update.resolver;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.dependency.FailedDependency;
import be.vbgn.gradle.pluginupdates.update.finder.internal.InvalidResolvesCache;
import java.util.Optional;
import java.util.stream.Stream;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class FailureCachingDependencyResolver implements DependencyResolver {
    private static final Logger LOGGER = Logging.getLogger(FailureCachingDependencyResolver.class);

    private DependencyResolver parentResolver;
    private InvalidResolvesCache invalidResolvesCache;

    public FailureCachingDependencyResolver(
            DependencyResolver parentResolver,
            InvalidResolvesCache invalidResolvesCache) {
        this.parentResolver = parentResolver;
        this.invalidResolvesCache = invalidResolvesCache;
    }

    @Override
    public Stream<Dependency> resolve(Dependency dependency) {
        Optional<FailedDependency> maybeFailedDependency = invalidResolvesCache.get(dependency);

        if(maybeFailedDependency.isPresent()) {
            LOGGER.trace("Found failed dependency in cache. Using failed dependency {}",
                    maybeFailedDependency.get());
            return Stream.of(maybeFailedDependency.get());
        }

        return parentResolver.resolve(dependency)
                .peek(dependency1 -> {
                    if(dependency1 instanceof FailedDependency) {
                        invalidResolvesCache.put(dependency);
                    }
                });
    }
}
