package be.vbgn.gradle.pluginupdates.update.finder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.dependency.FailedDependency;
import be.vbgn.gradle.pluginupdates.version.Version;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

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
}
