package be.vbgn.gradle.pluginupdates.dsl.internal;

import be.vbgn.gradle.pluginupdates.dsl.IgnoreSpec;

class AbstractIgnoreSpec implements IgnoreSpec {

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

    @Override
    public IgnoreSpec majorUpdates() {
        ignoreModule = false;
        ignoreMajorUpdates = true;
        return this;
    }

    @Override
    public IgnoreSpec minorUpdates() {
        majorUpdates();
        ignoreMinorUpdates = true;
        return this;
    }

    @Override
    public IgnoreSpec microUpdates() {
        minorUpdates();
        ignoreMicroUpdates = true;
        return this;
    }

    @Override
    public void because(String reason) {
        // TODO: Reason is not used anywhere right now
    }
}
