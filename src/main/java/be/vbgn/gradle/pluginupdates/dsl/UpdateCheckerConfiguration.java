package be.vbgn.gradle.pluginupdates.dsl;

import groovy.lang.Closure;
import javax.annotation.Nonnull;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

public interface UpdateCheckerConfiguration {

    @Nonnull
    UpdatePolicy getPolicy();

    default void policy(@Nonnull Action<? super UpdatePolicy> policy) {
        policy.execute(getPolicy());
    }

    default void policy(@Nonnull Closure policy) {
        ConfigureUtil.configure(policy, getPolicy());
    }

}
