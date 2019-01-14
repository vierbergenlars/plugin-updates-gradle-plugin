package be.vbgn.gradle.pluginupdates.update;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.dependency.FailedDependency;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Represents updates that are available for a certain {@link Dependency}
 */
public interface Update {

    /**
     * The original dependency for which updates have been looked up
     */
    @Nonnull
    Dependency getOriginal();

    @Nonnull
    List<Dependency> getUpdates();

    default boolean isOutdated() {
        if (getUpdates().isEmpty()) {
            return false;
        }
        Dependency original = getOriginal();
        return getUpdates().stream()
                .filter(dependency -> !(dependency instanceof FailedDependency))
                .anyMatch(update -> !update.equals(original));
    }
}
