package be.vbgn.gradle.pluginupdates.dependency;

import javax.annotation.Nullable;

public interface FailedDependency extends Dependency {

    @Nullable
    Throwable getProblem();

}
