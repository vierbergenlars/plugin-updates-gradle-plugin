package be.vbgn.gradle.pluginupdates.update.finder;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

/**
 * A version provider generates a list of version constraints that are updates of a {@link Dependency}
 */
public interface VersionProvider {

    /**
     * @param dependency The dependency to generate version constraints for
     * @return a stream of version constraints, including a flag that specifies if resolving of a constraint is allowed to fail or not
     */
    @Nonnull
    Stream<FailureAllowedVersion> getUpdateVersions(@Nonnull Dependency dependency);
}
