package be.vbgn.gradle.pluginupdates.update.finder;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class FilterOlderVersionsUpdateFinder implements UpdateFinder {

    private static final Logger LOGGER = Logging.getLogger(FilterOlderVersionsUpdateFinder.class);
    private UpdateFinder updateFinder;

    public FilterOlderVersionsUpdateFinder(@Nonnull UpdateFinder updateFinder) {
        this.updateFinder = updateFinder;
    }

    @Nonnull
    @Override
    public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
        return updateFinder.findUpdates(dependency)
                .filter(updatedDependency -> {
                    if(!updatedDependency.getGroup().equals(dependency.getGroup()) || !updatedDependency.getName().equals(dependency.getName())) {
                        LOGGER.debug("Not checking version because GA coordinates are different.");
                        return true;
                    }
                    if (updatedDependency.getVersion().compareTo(dependency.getVersion()) < 0) {
                        LOGGER.info(
                                "Dropped dependency update " + updatedDependency + " because version is older than "
                                        + dependency.getVersion());
                        return false;
                    }
                    return true;
                });
    }
}
