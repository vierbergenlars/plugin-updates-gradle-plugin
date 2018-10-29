package be.vbgn.gradle.pluginupdates.dependency;

import javax.annotation.Nonnull;

public interface FailedDependency extends Dependency {

    @Nonnull
    Throwable getProblem();

}
