package be.vbgn.gradle.pluginupdates.update.finder;

import static org.junit.Assert.assertEquals;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.junit.Test;

public class FilterOlderVersionsUpdateFinderTest {

    @Test
    public void findUpdatesOlderVersion() {
        Dependency original = new DefaultDependency("be.vbgn.gradle", "test1", "0.4");
        UpdateFinder updateFinder = new FilterOlderVersionsUpdateFinder(new UpdateFinder() {
            @Nonnull
            @Override
            public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
                return Stream.of(dependency.withVersion("1.0.0"), dependency.withVersion("0.3"));
            }
        });

        assertEquals(Collections.singleton(original.withVersion("1.0.0")), updateFinder.findUpdates(original).collect(Collectors.toSet()));
    }

    @Test
    public void findUpdatesOlderVersionWithGAChange() {
        Dependency original = new DefaultDependency("be.vbgn.gradle", "test1", "0.4");
        UpdateFinder updateFinder = new FilterOlderVersionsUpdateFinder(new UpdateFinder() {
            @Nonnull
            @Override
            public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
                return Stream.of(dependency.withName("test2").withVersion("1.0.0"), dependency.withName("test2").withVersion("0.3"));
            }
        });
        Set<Dependency> newDependencies = updateFinder.findUpdates(original).collect(Collectors.toSet());
        assertEquals(new HashSet<>(Arrays.asList(
                original.withName("test2").withVersion("1.0.0"),
                original.withName("test2").withVersion("0.3")
        )), newDependencies);

    }

}
