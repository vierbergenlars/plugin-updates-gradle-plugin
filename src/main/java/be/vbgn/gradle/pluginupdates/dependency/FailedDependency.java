package be.vbgn.gradle.pluginupdates.dependency;

import javax.annotation.Nullable;

/**
 * A {@link Dependency} whose lookup has failed
 */
public interface FailedDependency extends Dependency {

    /**
     * @return The exception that caused the lookup to fail. Will return {@code null} if the cause is not available.
     */
    @Nullable
    Throwable getProblem();

}
