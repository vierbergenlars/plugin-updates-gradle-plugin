package be.vbgn.gradle.pluginupdates;

import be.vbgn.gradle.pluginupdates.update.formatter.DefaultUpdateFormatter;
import be.vbgn.gradle.pluginupdates.update.formatter.PluginUpdateFormatter;
import be.vbgn.gradle.pluginupdates.update.formatter.UpdateFormatter;
import be.vbgn.gradle.pluginupdates.update.task.CheckUpdateTask;
import javax.annotation.Nonnull;
import org.codehaus.groovy.runtime.MethodClosure;
import org.gradle.BuildResult;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.PluginAware;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.util.GradleVersion;

/**
 * Plugin that prints plugins that can be updated at the end of your build.
 */
public class PluginUpdatesPlugin implements Plugin<PluginAware> {

    /**
     * Gradle plugin ID of this plugin
     */
    public static final String PLUGIN_ID = "be.vbgn.plugin-updates";
    private static final Logger LOGGER = Logging.getLogger(PluginUpdatesPlugin.class);
    private final MethodClosure buildFinishedCallback = new MethodClosure(this, "onBuildFinished");

    /**
     * {@inheritDoc}
     * <p>
     * Applying this plugin will apply the {@link ConfigurationPlugin} as well to enable configuration of this plugin.
     *
     * @throws IllegalArgumentException When the plugin is applied to anything that is not a {@link Project}, {@link Gradle} or {@link Settings}
     * @see #apply(Project)
     * @see #apply(Settings)
     * @see #apply(Gradle)
     */
    @Override
    public void apply(@Nonnull PluginAware thing) {
        thing.getPluginManager().apply(ConfigurationPlugin.class);
        LOGGER.debug("Plugin applied to {}", thing.getClass());
        if (thing instanceof Project) {
            apply((Project) thing);
        } else if (thing instanceof Gradle) {
            apply((Gradle) thing);
        } else if (thing instanceof Settings) {
            apply((Settings) thing);
        } else {
            throw new IllegalArgumentException("This plugin can only be applied to Project, Gradle or Settings");
        }
    }

    /**
     * Checks if gradle is run in online or offline mode.
     * <p>
     * It also prints a message when gradle is run in offline mode
     */
    private boolean isOnline(@Nonnull Gradle gradle) {
        if (gradle.getStartParameter().isOffline()) {
            LOGGER.info("Gradle is running in offline mode, plugins will not be checked for updates");
            return false;
        }
        return true;
    }

    /**
     * Checks for plugin updates for the subproject in this <code>build.gradle</code> file.
     */
    public void apply(@Nonnull Project project) {
        Gradle gradle = project.getGradle();
        if (isOnline(gradle)) {
            LOGGER.debug("Register buildFinished callback for single project");
            configurePlugin(gradle);
            project.getGradle().buildFinished(buildFinishedCallback.curry(project));
        }
    }

    /**
     * Checks for plugin updates for the root project and all subprojects in this <code>settings.gradle</code> file.
     */
    public void apply(@Nonnull Settings settings) {
        Gradle gradle = settings.getGradle();
        if (isOnline(gradle)) {
            LOGGER.debug("Register buildFinished callback");
            configurePlugin(gradle);
            gradle.buildFinished(buildFinishedCallback);
        }
    }

    /**
     * Checks for plugin updates across all projects.
     * <p>
     * This plugin is applied in the <code>~/.gradle/init.gradle</code> file, or to a file inside the <code>~/.gradle/init.d/*.gradle</code>
     */
    public void apply(@Nonnull Gradle gradle) {
        if (GradleVersion.current().compareTo(GradleVersion.version("3.2.1")) < 0) {
            // Do not break init.gradle for versions older than what we compile against
            LOGGER.warn(
                    "Plugin updates plugin has been disabled because your gradle version is too old. Try updating to at least version 3.2.1");
            return;
        }
        if (isOnline(gradle)) {
            LOGGER.debug("Register buildFinished callback");
            configurePlugin(gradle);
            gradle.buildFinished(buildFinishedCallback);
        }
    }

    /**
     * Hook that runs when a build has finished.
     * <p>
     * This hooks is run for the cases in {@link #apply(Gradle)} and {@link #apply(Settings)}, where all projects are checked for updates
     */
    private void onBuildFinished(@Nonnull BuildResult buildResult) {
        Gradle gradle = buildResult.getGradle();
        Project rootProject = null;
        try {
            rootProject = gradle.getRootProject();
        } catch (IllegalStateException e) {
            LOGGER.debug("Could not get root project, skipping updates check. {}", e.getMessage());
        }
        if (rootProject != null) {
            rootProject.getAllprojects()
                    .stream()
                    .filter(project -> {
                        if (project.getPlugins().hasPlugin(PLUGIN_ID)) {
                            LOGGER.debug("Project {} has the plugin applied. Skipping for global updates check.",
                                    project);
                            return false;
                        }
                        return true;
                    })
                    .forEach(this::runBuildscriptUpdateCheck);
        }
    }

    /**
     * Hook that runs when a build has finished.
     * <p>
     * This hooks is run for the case in {@link #apply(Project)}, where only the project on which the plugin is applied is checked for updates
     *
     * @param project The project on which the update check is run
     */
    private void onBuildFinished(@Nonnull Project project, @Nonnull BuildResult buildResult) {
        runBuildscriptUpdateCheck(project);
    }

    /**
     * Configures shared objects used by all update checks
     *
     * @param gradle The gradle invocation to register the shared objects for
     */
    private void configurePlugin(@Nonnull Gradle gradle) {
        gradle.allprojects(project -> {
            TaskProvider<CheckUpdateTask> updateTask = project.getTasks()
                    .register("checkGradlePluginUpdates", CheckUpdateTask.class, task -> {
                        task.getConfigurations().add(project.getBuildscript().getConfigurations().named("classpath"));
                        task.setDescription("Checks for updates for your Gradle plugins");
                    });
            project.getTasks().configureEach(task -> {
                if (!(task instanceof CheckUpdateTask)) {
                    task.finalizedBy(updateTask);
                }
            });
        });
    }


    /**
     * Runs an update check and prints out outdated plugin versions
     *
     * @param project The project to run an update check for
     */
    private void runBuildscriptUpdateCheck(@Nonnull Project project) {
        try {
            UpdateFormatter updateFormatter = new PluginUpdateFormatter(new DefaultUpdateFormatter());

            project.getTasks().withType(CheckUpdateTask.class).forEach(checkUpdateTask -> {
                checkUpdateTask.getUpdates().get().forEach(update -> {
                    if (update.isOutdated()) {
                        LOGGER.warn("Plugin is outdated in {}: {}", project, updateFormatter.format(update));
                    }
                });
            });
        } catch (Throwable e) {
            LOGGER.error("Plugin update check failed.", e);
        }

    }

}
