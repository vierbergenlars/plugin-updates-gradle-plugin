package be.vbgn.gradle.pluginupdates.update.checker;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.update.Update;
import be.vbgn.gradle.pluginupdates.update.finder.UpdateFinder;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class DefaultUpdateChecker implements UpdateChecker {

    @Nonnull
    private UpdateFinder updateFinder;
    private static final Logger LOGGER = Logging.getLogger(DefaultUpdateChecker.class);

    public DefaultUpdateChecker(@Nonnull UpdateFinder updateFinder) {
        this.updateFinder = updateFinder;
    }

    @Override
    @Nonnull
    public Stream<Update> getUpdates(@Nonnull Configuration configuration) {
        if (configuration.isEmpty()) {
            LOGGER.info("{} is empty, no update checking needed.", configuration);
            return Stream.empty();
        }
        return configuration.getResolvedConfiguration()
                .getFirstLevelModuleDependencies()
                .stream()
                .flatMap(DefaultDependency::fromGradle)
                .peek(dependency -> LOGGER.debug("First level module of {}: {}", configuration, dependency))
                .map(dependency -> new UpdateEntry(dependency, getUpdatedDependencies(dependency)));

    }

    @Nonnull
    private Stream<Dependency> getUpdatedDependencies(@Nonnull Dependency dependency) {
        return updateFinder.findUpdates(dependency)
                .distinct()
                .sorted(Comparator.comparing(Dependency::getVersion));
    }

    private static class UpdateEntry implements Update {

        @Nonnull
        private Dependency original;
        @Nonnull
        private List<Dependency> updates;

        private UpdateEntry(@Nonnull Dependency original, @Nonnull Stream<Dependency> updates) {
            this(original, updates.collect(Collectors.toList()));
        }

        private UpdateEntry(@Nonnull Dependency original, @Nonnull List<Dependency> updates) {
            this.original = original;
            this.updates = updates;
        }

        @Nonnull
        public List<Dependency> getUpdates() {
            return updates;
        }


        @Nonnull
        public Dependency getOriginal() {
            return original;
        }
    }

}
