package be.vbgn.gradle.pluginupdates.update.resolver.internal;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.dependency.FailedDependency;
import java.util.Optional;

/**
 * Keeps a cache of dependencies for which resolving has failed, so they are not resolved every time
 */
public interface InvalidResolvesCache {

    /**
     * Places a new dependency in the failed resolves cache.
     *
     * @param dependency The dependency that has to be added to the list of failed resolves.
     *                   The original dependency that has failed should be passed as a parameter, not the {@link FailedDependency}
     *                   that is the result of the resolution failure.
     */
    void put(Dependency dependency);

    /**
     * Tries to fetch a {@link FailedDependency} from cache for a {@link Dependency} that has failed.
     * <p>
     * Failures are only kept for a limited time, and are cleaned up after they have expired.
     *
     * @param dependency The dependency to look up a failure for
     * @return A failed dependency, or {@link Optional#empty()} when no failed dependency is found or its cache time has expired
     */
    Optional<FailedDependency> get(Dependency dependency);
}
