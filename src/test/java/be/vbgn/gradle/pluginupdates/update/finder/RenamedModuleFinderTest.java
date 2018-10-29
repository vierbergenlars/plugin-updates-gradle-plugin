package be.vbgn.gradle.pluginupdates.update.finder;

import static org.junit.Assert.assertEquals;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.version.Version;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.junit.Test;

public class RenamedModuleFinderTest {

    @Test
    public void findUpdatesChangeGroup() throws Exception {
        Dependency original = new DefaultDependency("be.vbgn.gradle", "test1", "0.4");
        UpdateFinder updateFinder = new RenamedModuleFinder(new UpdateFinder() {
            @Nonnull
            @Override
            public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
                assertEquals(dependency.getGroup(), "be.vbgn.gradle.test2");
                assertEquals(dependency.getName(), "test1");

                // When a group or name has changed and version has not been set explicitly
                assertEquals(dependency.getVersion(), Version.parse("+"));
                assertEquals("", dependency.getClassifier());
                assertEquals("jar", dependency.getType());
                return Stream.empty();
            }
        }, dependency -> dependency.withGroup("be.vbgn.gradle.test2"));

        updateFinder.findUpdates(original);
    }

    @Test
    public void findUpdatesChangeName() throws Exception {
        Dependency original = new DefaultDependency("be.vbgn.gradle", "test1", "0.4");
        UpdateFinder updateFinder = new RenamedModuleFinder(new UpdateFinder() {
            @Nonnull
            @Override
            public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
                assertEquals("be.vbgn.gradle", dependency.getGroup());
                assertEquals("test2", dependency.getName());

                // When a group or name has changed and version has not been set explicitly
                assertEquals(Version.parse("+"), dependency.getVersion());
                assertEquals("", dependency.getClassifier());
                assertEquals("jar", dependency.getType());
                return Stream.empty();
            }
        }, dependency -> dependency.withName("test2"));

        updateFinder.findUpdates(original);
    }

    @Test
    public void findUpdatesChangeVersion() throws Exception {
        Dependency original = new DefaultDependency("be.vbgn.gradle", "test1", "0.4");
        UpdateFinder updateFinder = new RenamedModuleFinder(new UpdateFinder() {
            @Nonnull
            @Override
            public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
                assertEquals("be.vbgn.gradle.test2", dependency.getGroup());
                assertEquals("test1", dependency.getName());
                // When a group or name has changed and version has been set explicitly
                assertEquals(dependency.getVersion(), Version.parse("0.1"));
                assertEquals("", dependency.getClassifier());
                assertEquals("jar", dependency.getType());
                return Stream.empty();
            }
        }, dependency -> dependency.withGroup("be.vbgn.gradle.test2").withVersion("0.1"));

        updateFinder.findUpdates(original);
    }

    @Test
    public void findUpdatesChangeClassifier() throws Exception {
        Dependency original = new DefaultDependency("be.vbgn.gradle", "test1", "0.4");
        UpdateFinder updateFinder = new RenamedModuleFinder(new UpdateFinder() {
            @Nonnull
            @Override
            public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
                assertEquals("be.vbgn.gradle", dependency.getGroup());
                assertEquals("test1", dependency.getName());
                assertEquals(Version.parse("0.4"), dependency.getVersion());
                assertEquals("shaded", dependency.getClassifier());
                assertEquals("jar", dependency.getType());
                return Stream.empty();
            }
        }, dependency -> dependency.withClassifier("shaded"));

        updateFinder.findUpdates(original);
    }

    @Test
    public void findUpdatesChangeType() throws Exception {
        Dependency original = new DefaultDependency("be.vbgn.gradle", "test1", "0.4");
        UpdateFinder updateFinder = new RenamedModuleFinder(new UpdateFinder() {
            @Nonnull
            @Override
            public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
                assertEquals("be.vbgn.gradle", dependency.getGroup());
                assertEquals("test1", dependency.getName());
                assertEquals(Version.parse("0.4"), dependency.getVersion());
                assertEquals("", dependency.getClassifier());
                assertEquals("pom", dependency.getType());
                return Stream.empty();
            }
        }, dependency -> dependency.withType("pom"));

        updateFinder.findUpdates(original);
    }

    @Test
    public void findUpdatesNewDependency() throws Exception {
        Dependency original = new DefaultDependency("be.vbgn.gradle", "test1", "0.4");
        UpdateFinder updateFinder = new RenamedModuleFinder(new UpdateFinder() {
            @Nonnull
            @Override
            public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
                assertEquals("be.vbgn.gradle", dependency.getGroup());
                assertEquals("test2", dependency.getName());
                assertEquals(Version.parse("0.4"), dependency.getVersion());
                assertEquals("", dependency.getClassifier());
                assertEquals("jar", dependency.getType());
                return Stream.empty();
            }
        }, dependency -> new DefaultDependency(dependency.getGroup(), "test2", dependency.getVersion(),
                dependency.getClassifier(), dependency.getType()));

        updateFinder.findUpdates(original);
    }
}
