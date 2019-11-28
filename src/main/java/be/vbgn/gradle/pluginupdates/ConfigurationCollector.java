package be.vbgn.gradle.pluginupdates;

import be.vbgn.gradle.pluginupdates.dsl.internal.UpdateCheckerBuilderConfiguration;
import be.vbgn.gradle.pluginupdates.dsl.internal.UpdateCheckerConfigurationImpl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.PluginAware;

class ConfigurationCollector {
    private static final Logger LOGGER = Logging.getLogger(ConfigurationCollector.class);
    private Gradle gradle;
    private UpdateCheckerBuilderConfiguration globalConfiguration = null;


    public ConfigurationCollector(Gradle gradle) {
        this.gradle = gradle;
    }

    private UpdateCheckerBuilderConfiguration global() {
        if(globalConfiguration == null) {
            synchronized (this) {
                // Check again in synchronized block, so we do not try to create the global configuration multiple times
                // if multiple threads are checking the configuration concurrently
                if (globalConfiguration != null) {
                    return globalConfiguration;
                }
                UpdateCheckerBuilderConfiguration rootConfiguration = findUpdateCheckerConfiguration(gradle);
                UpdateCheckerBuilderConfiguration settingsConfiguration;
                try {
                    settingsConfiguration = findUpdateCheckerConfiguration(((GradleInternal) gradle).getSettings());
                } catch (ClassCastException | NoSuchMethodError e) {
                    LOGGER.error(
                            "Gradle object {} does not implement GradleInternal or does not have a getSettings() method. Plugin update configuration in settings.gradle can not be fetched and will be ignored. {}",
                            gradle, e);

                    settingsConfiguration = new UpdateCheckerConfigurationImpl();
                }
                globalConfiguration = UpdateCheckerConfigurationImpl.merge(rootConfiguration, settingsConfiguration);
            }
        }
        return globalConfiguration;

    }

    @Nonnull
    public UpdateCheckerBuilderConfiguration forProject(@Nonnull Project project) {
        if(project.getGradle() != gradle) {
            throw new IllegalArgumentException("Project must be for the same Gradle instance as the ConfigurationCollector has been constructed for.");
        }

        UpdateCheckerBuilderConfiguration projectConfiguration = findUpdateCheckerConfiguration(project);

        return UpdateCheckerConfigurationImpl.merge(global(), projectConfiguration);
    }

    @Nonnull
    private UpdateCheckerBuilderConfiguration findUpdateCheckerConfiguration(@Nonnull PluginAware pluginAware) {
        ConfigurationPlugin configurationPlugin = passthroughConfigurationPlugin(
                pluginAware.getPlugins().findPlugin(ConfigurationPlugin.PLUGIN_ID));
        if (configurationPlugin != null) {
            LOGGER.debug("Found update checker configuration plugin {}", configurationPlugin);
            return configurationPlugin.getConfiguration();
        }
        return new UpdateCheckerConfigurationImpl();
    }

    @Nullable
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
