package be.vbgn.gradle.pluginupdates.dsl.internal;

import be.vbgn.gradle.pluginupdates.dsl.IgnoreSpec;
import java.io.Serializable;
import javax.annotation.Nonnull;

class AbstractIgnoreSpec implements IgnoreSpec, Serializable {

    /**
     * Ignore all updates for this module
     */
    protected boolean ignoreModule = true;
    /**
     * Ignore only major updates for this module
     */
    protected boolean ignoreMajorUpdates = false;
    /**
     * Ignore minor updates for this module
     */
    protected boolean ignoreMinorUpdates = false;
    /**
     * Ignore micro updates for this module
     */
    protected boolean ignoreMicroUpdates = false;

    @Nonnull
    @Override
    public IgnoreSpec majorUpdates() {
        ignoreModule = false;
        ignoreMajorUpdates = true;
        return this;
    }

    @Nonnull
    @Override
    public IgnoreSpec minorUpdates() {
        majorUpdates();
        ignoreMinorUpdates = true;
        return this;
    }

    @Nonnull
    @Override
    public IgnoreSpec microUpdates() {
        minorUpdates();
        ignoreMicroUpdates = true;
        return this;
    }

    @Override
    public void because(@Nonnull String reason) {
        // TODO: Reason is not used anywhere right now
    }
}
