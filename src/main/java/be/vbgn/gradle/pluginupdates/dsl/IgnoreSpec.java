package be.vbgn.gradle.pluginupdates.dsl;

public interface IgnoreSpec {

    IgnoreSpec majorUpdates();

    IgnoreSpec minorUpdates();

    IgnoreSpec microUpdates();

    void because(String reason);
}
