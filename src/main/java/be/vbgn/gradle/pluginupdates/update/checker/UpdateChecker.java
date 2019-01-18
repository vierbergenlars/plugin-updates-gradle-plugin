package be.vbgn.gradle.pluginupdates.update.checker;

import be.vbgn.gradle.pluginupdates.update.Update;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.gradle.api.artifacts.Configuration;

/**
 * Update checkers are responsible to take an {@link Configuration} and to generate a {@link Stream<Update>} containing
 * possible new versions for all entries in the {@link Configuration}
 */
public interface UpdateChecker {

    /**
     * Checks for updates for dependencies listed in a configuration
     * <p>
     * Usually, only direct dependencies from the configuration are listed for updates
     *
     * @param configuration The configuration for which dependencies are listed
     * @return A stream of updates for the dependencies in the configuration
     */
    @Nonnull
    Stream<Update> getUpdates(@Nonnull Configuration configuration);

}
