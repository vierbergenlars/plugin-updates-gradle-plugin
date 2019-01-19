package be.vbgn.gradle.pluginupdates.update.finder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.DefaultFailedDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.dependency.FailedDependency;
import be.vbgn.gradle.pluginupdates.update.resolver.internal.InvalidResolvesCache;
import be.vbgn.gradle.pluginupdates.update.resolver.internal.InvalidResolvesCacheImpl;
import be.vbgn.gradle.pluginupdates.version.Version;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;
import org.mockito.Mockito;

public class DefaultUpdateFinderTest {

    @Test
    public void findUpdates() {
        Project project = ProjectBuilder.builder()
                .build();

        ArtifactRepository repository = project.getBuildscript().getRepositories().gradlePluginPortal();
        project.getBuildscript().getRepositories().add(repository);

        UpdateFinder updateChecker = new DefaultUpdateFinder(project.getBuildscript(), new DefaultVersionProvider());

        Dependency original = new DefaultDependency("org.gradle", "gradle-hello-world-plugin", "0.1");
        List<Dependency> updates = updateChecker.findUpdates(original)
                .filter(dependency -> !(dependency instanceof FailedDependency))
                .distinct()
                .collect(Collectors.toList());

        assertEquals(1, updates.size());
        assertEquals(original.withVersion("0.2"), updates.get(0));
    }

    @Test
    public void findUpdatesNonExisting() {
        Project project = ProjectBuilder.builder()
                .build();

        ArtifactRepository repository = project.getBuildscript().getRepositories().gradlePluginPortal();
        project.getBuildscript().getRepositories().add(repository);

        UpdateFinder updateChecker = new DefaultUpdateFinder(project.getBuildscript(), new DefaultVersionProvider());

        Dependency original = new DefaultDependency("be.vbgn.gradle.test", "test1", "0.1");
        List<Dependency> updates = updateChecker.findUpdates(original)
                .collect(Collectors.toList());
        assertTrue(updates.stream().allMatch(dependency -> dependency instanceof FailedDependency));

        updates.sort(Comparator.comparing(Dependency::getVersion));

        assertEquals(2, updates.size());
        assertEquals(Version.parse("0.+"), updates.get(0).getVersion());
        assertEquals(Version.parse("+"), updates.get(1).getVersion());
    }

    @Test
    public void findUpdatesNonExistingWithEmptyCache() {
        Project project = ProjectBuilder.builder()
                .build();

        ArtifactRepository repository = project.getBuildscript().getRepositories().gradlePluginPortal();
        project.getBuildscript().getRepositories().add(repository);

        DefaultUpdateFinder updateChecker = new DefaultUpdateFinder(project.getBuildscript(),
                new DefaultVersionProvider());

        InvalidResolvesCache invalidResolvesCacheMock = Mockito.mock(InvalidResolvesCacheImpl.class);
        updateChecker.setInvalidResolvesCache(invalidResolvesCacheMock);
        // Say that nothing is cached
        when(invalidResolvesCacheMock.get(any(Dependency.class))).thenReturn(Optional.empty());
        Dependency original = new DefaultDependency("org.gradle", "gradle-hello-world-plugin", "0.1");

        List<Dependency> updates = updateChecker.findUpdates(original)
                .collect(Collectors.toList());
        assertTrue(updates.stream().noneMatch(dependency -> dependency instanceof FailedDependency));

        // The nonexisting version is attemted to be fetched from cache
        verify(invalidResolvesCacheMock).get(original.withVersion("+"));
        verify(invalidResolvesCacheMock).get(original.withVersion("0.+"));
        verify(invalidResolvesCacheMock).get(original.withVersion("0.1.+"));
        // Check that the nonexisting version was put in the cache
        verify(invalidResolvesCacheMock).put(original.withVersion("0.1.+"));
        verifyNoMoreInteractions(invalidResolvesCacheMock);

    }

    @Test
    public void findUpdatesNonExistingWithPrimedCache() {
        Project project = ProjectBuilder.builder()
                .build();

        ArtifactRepository repository = project.getBuildscript().getRepositories().gradlePluginPortal();
        project.getBuildscript().getRepositories().add(repository);

        DefaultUpdateFinder updateChecker = new DefaultUpdateFinder(project.getBuildscript(),
                new DefaultVersionProvider());

        InvalidResolvesCache invalidResolvesCacheMock = Mockito.mock(InvalidResolvesCacheImpl.class);
        updateChecker.setInvalidResolvesCache(invalidResolvesCacheMock);
        Dependency original = new DefaultDependency("org.gradle", "gradle-hello-world-plugin", "0.1");
        // Say that nothing is cached
        when(invalidResolvesCacheMock.get(any(Dependency.class))).thenReturn(Optional.empty());
        Throwable marker = new RuntimeException("Marker");
        // Except for this one version that is an allowed failure too
        when(invalidResolvesCacheMock.get(original.withVersion("0.1.+"))).thenReturn(Optional.of(
                new DefaultFailedDependency("org.gradle", "gradle-hello-world-plugin", "0.1.+", marker)));

        List<Dependency> updates = updateChecker.findUpdates(original)
                .collect(Collectors.toList());
        assertTrue(updates.stream().noneMatch(dependency -> dependency instanceof FailedDependency));

        // The allowed failure version is fetched from cache
        verify(invalidResolvesCacheMock).get(original.withVersion("+"));
        verify(invalidResolvesCacheMock).get(original.withVersion("0.+"));
        verify(invalidResolvesCacheMock).get(original.withVersion("0.1.+"));
        // And that's it, since it was loaded from cache it was not put back in
        verifyNoMoreInteractions(invalidResolvesCacheMock);

    }
}
