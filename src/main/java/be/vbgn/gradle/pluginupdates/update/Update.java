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
     * @return the original dependency for which updates have been looked up
     */
    @Nonnull
    Dependency getOriginal();

    /**
     * @return a list of updates that are available for the original dependency.
     * This list is sorted by increasing severity of the update and can contain duplicates.
     * The list may also contain one or more {@link FailedDependency}
     */
    @Nonnull
    List<Dependency> getUpdates();

    /**
     * @return whether the original dependency is an outdated version
     */
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
