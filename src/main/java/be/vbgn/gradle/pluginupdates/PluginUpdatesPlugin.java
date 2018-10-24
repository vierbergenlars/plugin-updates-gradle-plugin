package be.vbgn.gradle.pluginupdates;

import org.codehaus.groovy.runtime.MethodClosure;
import org.gradle.BuildResult;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.PluginAware;
import org.gradle.util.GradleVersion;

public class PluginUpdatesPlugin implements Plugin<PluginAware> {

    public static Logger LOGGER = Logging.getLogger(PluginUpdatesPlugin.class);
    @Override
    public void apply(PluginAware thing) {
        LOGGER.debug("Plugin applied to {}", thing.getClass());
        if (thing instanceof Project) {
            apply((Project) thing);
        } else if (thing instanceof Gradle) {
            if (GradleVersion.current().compareTo(GradleVersion.version("3.2.1")) < 0) {
                // Do not break init.gradle for versions older than what we compile against
                LOGGER.warn(
                        "Plugin updates plugin has been disabled because your gradle version is too old. Try updating to at least version 3.2.1");
                return;
            }
            apply((Gradle) thing);
        } else if (thing instanceof Settings) {
            apply((Settings) thing);
        } else {
            throw new IllegalArgumentException("This plugin can only be applied to Project, Gradle or Settings");
        }
    }

    public void apply(Project project) {
        apply(project.getGradle());
    }

    public void apply(Settings settings) {
        apply(settings.getGradle());
    }

    public void apply(Gradle gradle) {
        if (gradle.getStartParameter().isOffline()) {
            LOGGER.info("Gradle is running in offline mode, plugins will not be checked for updates");
            return;
        }
        LOGGER.debug("Register buildFinished callback");
        gradle.buildFinished(new MethodClosure(this, "onBuildFinished"));
    }

    private void onBuildFinished(BuildResult buildResult) {
        Gradle gradle = buildResult.getGradle();
        gradle.getRootProject().getAllprojects().parallelStream()
                .flatMap(UpdateChecker::checkBuildscriptUpdates)
                .peek(update -> LOGGER.debug("Found dependency: {}", update))
                .filter(Update::isOutdated)
                .forEach(update -> {
                    gradle.getRootProject().getLogger()
                            .warn(update.getMessage());
                });
    }

}
