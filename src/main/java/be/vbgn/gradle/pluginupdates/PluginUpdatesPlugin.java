package be.vbgn.gradle.pluginupdates;

import be.vbgn.gradle.pluginupdates.dsl.internal.UpdateBuilder;
import be.vbgn.gradle.pluginupdates.dsl.internal.UpdateCheckerBuilderConfiguration;
import be.vbgn.gradle.pluginupdates.internal.ConfigurationCollector;
import be.vbgn.gradle.pluginupdates.internal.StreamUtil;
import be.vbgn.gradle.pluginupdates.update.Update;
import be.vbgn.gradle.pluginupdates.update.checker.DefaultUpdateChecker;
import be.vbgn.gradle.pluginupdates.update.finder.DefaultUpdateFinder;
import be.vbgn.gradle.pluginupdates.update.finder.DefaultVersionProvider;
import be.vbgn.gradle.pluginupdates.update.finder.UpdateFinder;
import be.vbgn.gradle.pluginupdates.update.finder.VersionProvider;
import be.vbgn.gradle.pluginupdates.update.finder.internal.InvalidResolvesCache;
import be.vbgn.gradle.pluginupdates.update.formatter.DefaultUpdateFormatter;
import be.vbgn.gradle.pluginupdates.update.formatter.PluginUpdateFormatter;
import be.vbgn.gradle.pluginupdates.update.formatter.UpdateFormatter;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import org.codehaus.groovy.runtime.MethodClosure;
import org.gradle.BuildResult;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.PluginAware;
import org.gradle.cache.CacheRepository;
import org.gradle.util.GradleVersion;

public class PluginUpdatesPlugin implements Plugin<PluginAware> {

    public static String PLUGIN_ID = "be.vbgn.plugin-updates";
    private static Logger LOGGER = Logging.getLogger(PluginUpdatesPlugin.class);
    private ConfigurationCollector configurationCollector;

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

    private boolean isOnline(@Nonnull Gradle gradle) {
        if (gradle.getStartParameter().isOffline()) {
            LOGGER.info("Gradle is running in offline mode, plugins will not be checked for updates");
            return false;
        }
        return true;
    }

    public void apply(@Nonnull Project project) {
        Gradle gradle = project.getGradle();
        if (isOnline(gradle)) {
            LOGGER.debug("Register buildFinished callback for single project");
            configurationCollector = new ConfigurationCollector(gradle);
            project.getGradle().buildFinished(new MethodClosure(this, "onBuildFinished").curry(project));
        }
    }

    public void apply(@Nonnull Settings settings) {
        Gradle gradle = settings.getGradle();
        if (isOnline(gradle)) {
            LOGGER.debug("Register buildFinished callback");
            configurationCollector = new ConfigurationCollector(gradle);
            gradle.buildFinished(new MethodClosure(this, "onBuildFinished"));
        }
    }

    public void apply(@Nonnull Gradle gradle) {
        if (GradleVersion.current().compareTo(GradleVersion.version("3.2.1")) < 0) {
            // Do not break init.gradle for versions older than what we compile against
            LOGGER.warn(
                    "Plugin updates plugin has been disabled because your gradle version is too old. Try updating to at least version 3.2.1");
            return;
        }
        if (isOnline(gradle)) {
            LOGGER.debug("Register buildFinished callback");
            configurationCollector = new ConfigurationCollector(gradle);
            gradle.buildFinished(new MethodClosure(this, "onBuildFinished"));
        }
    }

    private void onBuildFinished(@Nonnull BuildResult buildResult) {
        Gradle gradle = buildResult.getGradle();
        StreamUtil.parallelIfNoDebug(gradle.getRootProject().getAllprojects()
                .stream())
                .filter(project -> {
                    if (project.getPlugins().hasPlugin(PLUGIN_ID)) {
                        LOGGER.debug("Project {} has the plugin applied. Skipping for global updates check.", project);
                        return false;
                    }
                    return true;
                })
                .forEach(this::runBuildscriptUpdateCheck);
    }

    private void onBuildFinished(@Nonnull Project project, @Nonnull BuildResult buildResult) {
        runBuildscriptUpdateCheck(project);
    }

    private void runBuildscriptUpdateCheck(@Nonnull Project project) {
        try {
            UpdateCheckerBuilderConfiguration checkerConfiguration = configurationCollector.forProject(project);
            UpdateFormatter updateFormatter = new PluginUpdateFormatter(new DefaultUpdateFormatter());

            UpdateBuilder updateBuilder = checkerConfiguration.getUpdateBuilder();

            CacheRepository cacheRepository = ((GradleInternal) project.getGradle()).getServices()
                    .get(CacheRepository.class);
            InvalidResolvesCache invalidResolvesCache = null;
            try {
                invalidResolvesCache = new InvalidResolvesCache(cacheRepository);
            } catch (NoClassDefFoundError e) {
                LOGGER.warn(
                        "Some required gradle classes are missing. Invalid resolves cache is disabled, which will slow down plugin update checks.");
                LOGGER.debug("Full exception for above warning", e);
            }

            VersionProvider versionProvider = updateBuilder.buildVersionProvider(new DefaultVersionProvider());
            DefaultUpdateFinder defaultUpdateFinder = new DefaultUpdateFinder(project.getBuildscript(),
                    versionProvider);
            if (invalidResolvesCache != null) {
                defaultUpdateFinder.setInvalidResolvesCache(invalidResolvesCache);
            }
            UpdateFinder updateFinder = updateBuilder.buildUpdateFinder(defaultUpdateFinder);

            DefaultUpdateChecker updateChecker = new DefaultUpdateChecker(updateFinder);

            List<Update> updates = new LinkedList<>();

            updateChecker.getUpdates(project.getBuildscript().getConfigurations().getAt("classpath"))
                    .filter(Update::isOutdated)
                    .forEach(updates::add);

            if(invalidResolvesCache != null) {
                invalidResolvesCache.close();
            }
            updates.forEach(update -> {
                LOGGER.warn("Plugin is outdated in " + project.toString() + ": " + updateFormatter
                        .format(update));
            });
        } catch (Throwable e) {
            LOGGER.error("Plugin update check failed.", e);
        }

    }

}
