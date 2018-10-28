package be.vbgn.gradle.pluginupdates.update;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import java.util.List;
import javax.annotation.Nonnull;

public interface Update {

    @Nonnull
    Dependency getOriginal();

    @Nonnull
    List<Dependency> getUpdates();

    default boolean isOutdated() {
        if (getUpdates().isEmpty()) {
            return false;
        }
        Dependency original = getOriginal();
        return getUpdates().stream().anyMatch(update -> !update.equals(original));
    }
}
