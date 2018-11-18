package be.vbgn.gradle.pluginupdates;

import be.vbgn.gradle.pluginupdates.dsl.internal.UpdateBuilder;
import be.vbgn.gradle.pluginupdates.dsl.internal.UpdateCheckerConfigurationImpl;
import be.vbgn.gradle.pluginupdates.internal.cache.InvalidResolvesCache;
import be.vbgn.gradle.pluginupdates.update.Update;
import be.vbgn.gradle.pluginupdates.update.checker.DefaultUpdateChecker;
import be.vbgn.gradle.pluginupdates.update.finder.DefaultUpdateFinder;
import be.vbgn.gradle.pluginupdates.update.finder.DefaultVersionProvider;
import be.vbgn.gradle.pluginupdates.update.finder.FailedVersionsCachingFinder;
import be.vbgn.gradle.pluginupdates.update.finder.FailedVersionsCachingVersionProvider;
import be.vbgn.gradle.pluginupdates.update.finder.UpdateFinder;
import be.vbgn.gradle.pluginupdates.update.finder.VersionProvider;
import be.vbgn.gradle.pluginupdates.update.formatter.DefaultUpdateFormatter;
import be.vbgn.gradle.pluginupdates.update.formatter.PluginUpdateFormatter;
import be.vbgn.gradle.pluginupdates.update.formatter.UpdateFormatter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
            project.getGradle().buildFinished(new MethodClosure(this, "onBuildFinished").curry(project));
        }
    }

    public void apply(@Nonnull Settings settings) {
        Gradle gradle = settings.getGradle();
        if (isOnline(gradle)) {
            LOGGER.debug("Register buildFinished callback");
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
            UpdateCheckerConfigurationImpl checkerConfiguration = getUpdateCheckerConfiguration(project);
            UpdateFormatter updateFormatter = new PluginUpdateFormatter(new DefaultUpdateFormatter());

            UpdateBuilder updateBuilder = checkerConfiguration.getPolicy();

            CacheRepository cacheRepository = ((GradleInternal) project.getGradle()).getServices()
                    .get(CacheRepository.class);

            try (InvalidResolvesCache invalidResolvesCache = new InvalidResolvesCache(cacheRepository)) {

                VersionProvider versionProvider = new FailedVersionsCachingVersionProvider(
                        updateBuilder.buildVersionProvider(new DefaultVersionProvider()), invalidResolvesCache);
                UpdateFinder updateFinder = new FailedVersionsCachingFinder(updateBuilder
                        .buildUpdateFinder(new DefaultUpdateFinder(project.getBuildscript(), versionProvider)),
                        invalidResolvesCache);

                DefaultUpdateChecker updateChecker = new DefaultUpdateChecker(updateFinder);

                updateChecker.getUpdates(project.getBuildscript().getConfigurations().getAt("classpath"))
                        .filter(Update::isOutdated)
                        .forEach(update -> {
                            LOGGER.warn("Plugin is outdated in " + project.toString() + ": " + updateFormatter
                                    .format(update));
                        });
            }
        } catch (Throwable e) {
            LOGGER.error("Plugin update check failed.", e);
        }

    }

    @Nonnull
    private UpdateCheckerConfigurationImpl getUpdateCheckerConfiguration(@Nonnull Project project) {
        UpdateCheckerConfigurationImpl rootConfiguration = findUpdateCheckerConfiguration(project.getGradle());
        Gradle gradle = project.getGradle();
        UpdateCheckerConfigurationImpl settingsConfiguration;
        try {
            settingsConfiguration = findUpdateCheckerConfiguration(
                    ((GradleInternal) gradle).getSettings());
        } catch (ClassCastException | NoSuchMethodError e) {
            LOGGER.error(
                    "Gradle object {} does not implement GradleInternal or does not have a getSettings() method. Plugin update configuration in settings.gradle can not be fetched and will be ignored. {}",
                    gradle, e);

            settingsConfiguration = new UpdateCheckerConfigurationImpl();
        }
        UpdateCheckerConfigurationImpl projectConfiguration = findUpdateCheckerConfiguration(project);

        return UpdateCheckerConfigurationImpl.merge(rootConfiguration, settingsConfiguration, projectConfiguration);
    }

    @Nonnull
    private UpdateCheckerConfigurationImpl findUpdateCheckerConfiguration(@Nonnull PluginAware pluginAware) {
        ConfigurationPlugin configurationPlugin = passthroughConfigurationPlugin(
                pluginAware.getPlugins().findPlugin(ConfigurationPlugin.PLUGIN_ID));
        if (configurationPlugin != null) {
            LOGGER.debug("Found update checker configuration plugin {}", configurationPlugin);
            return configurationPlugin.configuration;
        }
        return new UpdateCheckerConfigurationImpl();
    }

    private ConfigurationPlugin passthroughConfigurationPlugin(@Nullable Plugin plugin) {
        if (plugin == null) {
            return null;
        }
        LOGGER.debug("Passing through plugin {}: original classloader {}", plugin, plugin.getClass().getClassLoader());
        if (plugin.getClass() == ConfigurationPlugin.class) {
            LOGGER.debug("No conversion needed for this class.");
            return (ConfigurationPlugin) plugin;
        }
        try {
            ByteArrayOutputStream bytearrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(bytearrayOutputStream);
            outputStream.writeObject(plugin);

            InputStream bytearrayInputStream = new ByteArrayInputStream(bytearrayOutputStream.toByteArray());
            ObjectInputStream inputStream = new ObjectInputStream(bytearrayInputStream);

            ConfigurationPlugin localPlugin = (ConfigurationPlugin) inputStream.readObject();

            LOGGER.debug("Passing through plugin {}: new plugin {}, classloader {}", plugin, localPlugin,
                    localPlugin.getClass().getClassLoader());
            return localPlugin;

        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("Can not pass through configuration plugin: {}", e);
            return null;
        }
    }
}
