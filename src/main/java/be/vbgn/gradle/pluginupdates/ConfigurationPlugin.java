package be.vbgn.gradle.pluginupdates;

import be.vbgn.gradle.pluginupdates.dsl.internal.UpdateBuilder;
import be.vbgn.gradle.pluginupdates.dsl.internal.UpdateCheckerBuilderConfiguration;
import be.vbgn.gradle.pluginupdates.dsl.internal.UpdateCheckerConfigurationImpl;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.gradle.api.Plugin;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.PluginAware;

/**
 * Supporting plugin that allows to specify a configuration for the {@link PluginUpdatesPlugin}
 * <p>
 * This plugin exposes {@link be.vbgn.gradle.pluginupdates.dsl.UpdateCheckerConfiguration} as <code>pluginUpdates</code>
 * <p>
 *
 * @see be.vbgn.gradle.pluginupdates.dsl.UpdateCheckerConfiguration Configuration example
 */
public class ConfigurationPlugin implements Plugin<PluginAware>, Serializable {

    /**
     * Plugin id of the configuration plugin
     */
    public static final String PLUGIN_ID = PluginUpdatesPlugin.PLUGIN_ID.concat(".config");

    /**
     * Configuration that is specified at the place where the plugin is applied
     */
    private UpdateCheckerConfigurationImpl configuration;

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(@Nonnull PluginAware target) {
        configuration = new DslObject(target).getExtensions()
                .create("pluginUpdates", UpdateCheckerConfigurationImpl.class);
    }

    /**
     * Gets the settings that are configured at the place this plugin is applied
     *
     * @return The configuration for this extension block
     */
    UpdateCheckerBuilderConfiguration getConfiguration() {
        return configuration;
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeObject(configuration.getUpdateBuilder());

    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        configuration = new UpdateCheckerConfigurationImpl((UpdateBuilder) in.readObject());

    }

    private void readObjectNoData()
            throws ObjectStreamException {
        configuration = new UpdateCheckerConfigurationImpl();
    }
}

