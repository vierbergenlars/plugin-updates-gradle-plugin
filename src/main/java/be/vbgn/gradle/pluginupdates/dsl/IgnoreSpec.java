package be.vbgn.gradle.pluginupdates.dsl;

public interface IgnoreSpec {

    IgnoreSpec majorUpdates();

    IgnoreSpec minorUpdates();

    void because(String reason);
}
