package be.vbgn.gradle.pluginupdates.update.checker;

import static org.junit.Assert.assertEquals;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.update.Update;
import be.vbgn.gradle.pluginupdates.update.finder.UpdateFinder;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

public class DefaultUpdateCheckerTest {

    @Test
    public void getUpdates() throws Exception {
        Dependency original = new DefaultDependency("org.gradle", "gradle-hello-world-plugin", "0.1");
        UpdateFinder updateFinder = new UpdateFinder() {
            @Nonnull
            @Override
            public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
                assertEquals(original, dependency);
                return Stream
                        .of(original.withVersion("0.2"), original.withVersion("1.2"), original.withVersion("0.1.1"));
            }
        };

        UpdateChecker updateChecker = new DefaultUpdateChecker(updateFinder);

        Project project = ProjectBuilder.builder()
                .build();

        ArtifactRepository repository = project.getBuildscript().getRepositories().gradlePluginPortal();
        project.getBuildscript().getRepositories().add(repository);

        Configuration configuration = project.getBuildscript()
                .getConfigurations()
                .detachedConfiguration(project.getDependencies().create(original.toDependencyNotation()));

        List<Update> updates = updateChecker.getUpdates(configuration).collect(Collectors.toList());

        assertEquals(1, updates.size());

        Update update = updates.get(0);

        assertEquals(original, update.getOriginal());
        assertEquals(3, update.getUpdates().size());

        assertEquals(original.withVersion("0.1.1"), update.getUpdates().get(0));
        assertEquals(original.withVersion("0.2"), update.getUpdates().get(1));
        assertEquals(original.withVersion("1.2"), update.getUpdates().get(2));
    }

    @Test
    public void getUpdatesEmptyConfiguration() {
        UpdateFinder updateFinder = new UpdateFinder() {
            @Nonnull
            @Override
            public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
                throw new RuntimeException("UpdateFinder#findUpdates() should not have been called.");
            }
        };

        UpdateChecker updateChecker = new DefaultUpdateChecker(updateFinder);

        Project project = ProjectBuilder.builder()
                .build();

        Configuration configuration = project.getBuildscript()
                .getConfigurations()
                .detachedConfiguration();

        List<Update> updates = updateChecker.getUpdates(configuration).collect(Collectors.toList());

        assertEquals(0, updates.size());

    }

}
