package be.vbgn.gradle.pluginupdates;

import org.codehaus.groovy.runtime.MethodClosure;
import org.gradle.BuildResult;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
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
        if (gradle.getStartParameter().isOffline()) {
            gradle.getRootProject().getLogger()
                    .info("Gradle is running in offline mode, plugins will not be checked for updates.");
            return;
        }
        UpdateChecker.checkUpdates(gradle.getRootProject())
                .filter(Update::isOutdated)
                .forEach(update -> {
                    gradle.getRootProject().getLogger()
                            .warn("Plugin is outdated in project " + update.getProjectName() + ": " + update
                                    .getModuleGroup() + ":" + update.getModuleName() + " [" + update.getOldVersion()
                                    + " -> " + update.getNewVersion() + "]");
                });
    }

}
