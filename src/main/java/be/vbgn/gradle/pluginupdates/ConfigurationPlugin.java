package be.vbgn.gradle.pluginupdates;

import be.vbgn.gradle.pluginupdates.dsl.internal.UpdateBuilder;
import be.vbgn.gradle.pluginupdates.dsl.internal.UpdateCheckerBuilderConfiguration;
import be.vbgn.gradle.pluginupdates.dsl.internal.UpdateCheckerConfigurationImpl;
import groovy.lang.GroovyObject;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.gradle.api.Plugin;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtraPropertiesExtension;
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
     * Creates an extension block, or an emulated extension block
     *
     * @param object The object to create the extension block on.
     *               Creates a real extension block if it implements {@link ExtensionAware},
     *               or an emulated extension block if it implements {@link GroovyObject} and has an {@link ExtraPropertiesExtension} where extra properties can be attached to
     * @param name   The name of the extension block that will be created
     * @param clazz  The type that will implement the extension block. it must have a 0 parameter constructor
     * @param <T>    Type that will implement the extension block.
     * @return An instanciated class of type {@literal clazz}
     * @throws RuntimeException When no extension block can be created
     */
    @Nonnull
    private <T> T createExtension(@Nullable Object object, @Nonnull String name, @Nonnull Class<T> clazz) {
        if(object instanceof ExtensionAware) {
            return ((ExtensionAware) object).getExtensions().create(name, clazz);
        }
        if(object instanceof GroovyObject) {
            GroovyObject groovyObject = (GroovyObject) object;
            ExtraPropertiesExtension extension = (ExtraPropertiesExtension) groovyObject.getProperty("ext");
            T newInstance = null;
            try {
                newInstance = clazz.newInstance();
            } catch (InstantiationException|IllegalAccessException e) {
                throw new RuntimeException("Can not create instance of "+clazz.getCanonicalName());
            }
            extension.set(name, newInstance);
            return newInstance;
        }
        throw new RuntimeException("Object is not a groovy object, can't add extension.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(@Nonnull PluginAware target) {
        configuration = createExtension(target, "pluginUpdates", UpdateCheckerConfigurationImpl.class);
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

