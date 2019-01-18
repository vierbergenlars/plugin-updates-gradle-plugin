package be.vbgn.gradle.pluginupdates.update.finder;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

/**
 * Update finders are responsible for finding newer versions for a {@link Dependency}
 */
public interface UpdateFinder {

    /**
     * @param dependency The dependency to find newer versions for
     * @return a stream that contains newer versions for the provided dependency, in increasing order.
     */
    @Nonnull
    Stream<Dependency> findUpdates(@Nonnull Dependency dependency);

}
