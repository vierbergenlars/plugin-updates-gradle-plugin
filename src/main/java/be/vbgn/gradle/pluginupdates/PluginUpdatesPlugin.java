package be.vbgn.gradle.pluginupdates;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.codehaus.groovy.runtime.MethodClosure;
import org.gradle.BuildResult;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.PluginAware;

public class PluginUpdatesPlugin implements Plugin<PluginAware> {

    @Override
    public void apply(PluginAware thing) {
        if (thing instanceof Project) {
            apply((Project) thing);
        } else if (thing instanceof Gradle) {
            apply((Gradle) thing);
        } else if (thing instanceof Settings) {
            apply((Settings) thing);
        } else {
            throw new IllegalArgumentException("This plugin can only be applied to Project, Gradle or Settings.");
        }
    }

    public void apply(Project project) {
        apply(project.getGradle());
    }

    public void apply(Settings settings) {
        apply(settings.getGradle());
    }

    public void apply(Gradle gradle) {
        gradle.buildFinished(new MethodClosure(this, "onBuildFinished"));
    }

    private void onBuildFinished(BuildResult buildResult) {
        Gradle gradle = buildResult.getGradle();
        gradle.getRootProject().getAllprojects()
                .stream()
                .flatMap(project2 -> checkUpdates(project2, project2.getBuildscript()
                        .getConfigurations()
                        .getAt("classpath"))
                )
                .filter(Update::isOutdated)
                .forEach(update -> {
                    gradle.getRootProject().getLogger()
                            .warn("Plugin is outdated in project " + update.getProjectName() + ": " + update
                                    .getModuleGroup() + ":" + update.getModuleName() + " [" + update.getOldVersion()
                                    + " -> " + update.getNewVersion() + "]");
                });
        buildResult.rethrowFailure();

    }

    private Stream<Update> checkUpdates(Project project, Configuration configuration) {
        ResolvedConfiguration oldConfiguration = configuration.getResolvedConfiguration();
        ResolvedConfiguration upToDateConfiguration = createLatestConfiguration(project, configuration)
                .getResolvedConfiguration();

        Set<ResolvedDependency> upToDateDependencies = upToDateConfiguration.getFirstLevelModuleDependencies();

        return oldConfiguration.getFirstLevelModuleDependencies().stream()
                .map(oldResolvedDependency -> new Update(project, configuration, oldResolvedDependency,
                        findDependency(upToDateDependencies, oldResolvedDependency)
                                .orElseGet(() -> oldResolvedDependency)));
    }

    private Optional<ResolvedDependency> findDependency(Set<ResolvedDependency> dependencies,
            ResolvedDependency dependency) {
        return dependencies.stream()
                .filter(dependency2 -> dependency.getModuleGroup().equals(dependency2.getModuleGroup()))
                .filter(dependency1 -> dependency.getModuleName().equals(dependency1.getModuleName()))
                .findFirst();
    }

    private Configuration createLatestConfiguration(Project project, Configuration configuration) {
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

    private Dependency createLatestDependency(Project project, Dependency dependency) {
        return project.getDependencies().create(dependency.getGroup() + ":" + dependency.getName() + ":+");
    }


}
