package be.vbgn.gradle.pluginupdates.update.finder;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public interface UpdateFinder {

    @Nonnull
    Stream<Dependency> findUpdates(@Nonnull Dependency dependency);

}
