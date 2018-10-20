package be.vbgn.gradle.pluginupdates;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class UpdateChecker {

    public static Logger LOGGER = Logging.getLogger(UpdateChecker.class);

    public static Stream<Update> checkUpdates(Project project) {
        return project.getAllprojects()
                .parallelStream()
                .flatMap(project1 -> checkUpdates(project1,
                        project1.getBuildscript().getConfigurations().getAt("classpath")));
    }

    public static Stream<Update> checkUpdates(Project project, Configuration configuration) {
        LOGGER.debug("Checking updates for project {}, configuration {}", project, configuration);
        ResolvedConfiguration oldConfiguration = configuration.getResolvedConfiguration();
        ResolvedConfiguration upToDateConfiguration = createLatestConfiguration(project, configuration)
                .getResolvedConfiguration();

        Set<ResolvedDependency> upToDateDependencies = upToDateConfiguration.getFirstLevelModuleDependencies();

        return oldConfiguration.getFirstLevelModuleDependencies().stream()
                .map(oldResolvedDependency -> new Update(project, configuration, oldResolvedDependency,
                        findDependency(upToDateDependencies, oldResolvedDependency)
                                .orElseGet(() -> oldResolvedDependency)));
    }

    private static Optional<ResolvedDependency> findDependency(Set<ResolvedDependency> dependencies,
            ResolvedDependency dependency) {
        return dependencies.stream()
                .filter(dependency2 -> dependency.getModuleGroup().equals(dependency2.getModuleGroup()))
                .filter(dependency1 -> dependency.getModuleName().equals(dependency1.getModuleName()))
                .findFirst();
    }

    private static Configuration createLatestConfiguration(Project project, Configuration configuration) {
        DependencySet dependencies = configuration.getDependencies();

        Stream<Dependency> newDependenciesStream = dependencies.stream()
                .filter(dependency -> dependency instanceof ExternalDependency)
                .map(dependency -> createLatestDependency(project, dependency));

        Configuration newConfiguration = configuration.copy();
        newConfiguration.setTransitive(false);
        newConfiguration.getDependencies().clear();

        newConfiguration.getDependencies().addAll(newDependenciesStream.collect(Collectors.toSet()));
        return newConfiguration;
    }

    private static Dependency createLatestDependency(Project project, Dependency dependency) {
        return project.getDependencies().create(dependency.getGroup() + ":" + dependency.getName() + ":+");
    }

}
