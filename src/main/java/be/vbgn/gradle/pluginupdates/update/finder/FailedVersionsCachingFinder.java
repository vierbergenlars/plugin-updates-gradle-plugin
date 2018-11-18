package be.vbgn.gradle.pluginupdates.update.finder;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.dependency.FailedDependency;
import be.vbgn.gradle.pluginupdates.internal.cache.InvalidResolvesCache;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class FailedVersionsCachingFinder implements UpdateFinder {

    @Nonnull
    private UpdateFinder updateFinder;

    @Nonnull
    private InvalidResolvesCache cache;

    public FailedVersionsCachingFinder(@Nonnull UpdateFinder updateFinder, @Nonnull InvalidResolvesCache cache) {
        this.updateFinder = updateFinder;
        this.cache = cache;
    }

    @Nonnull
    @Override
    public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
        return updateFinder.findUpdates(dependency)
                .peek(dependency1 -> {
                    if(dependency1 instanceof FailedDependency) {
                        cache.put((FailedDependency)dependency1);
                    }
                });
    }

}
