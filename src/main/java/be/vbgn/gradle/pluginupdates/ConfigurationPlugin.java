package be.vbgn.gradle.pluginupdates;

import be.vbgn.gradle.pluginupdates.dsl.internal.UpdateBuilder;
import be.vbgn.gradle.pluginupdates.dsl.internal.UpdateCheckerBuilderConfiguration;
import be.vbgn.gradle.pluginupdates.dsl.internal.UpdateCheckerConfigurationImpl;
import groovy.lang.GroovyObject;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.gradle.api.Plugin;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.plugins.PluginAware;

public class ConfigurationPlugin implements Plugin<PluginAware>, Serializable {
    public static final String PLUGIN_ID = PluginUpdatesPlugin.PLUGIN_ID.concat(".config");
    private UpdateCheckerConfigurationImpl configuration;

    private <T> T createExtension(Object object, String name, Class<T> clazz) {
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

    @Override
    public void apply(@Nonnull PluginAware target) {
        configuration = createExtension(target, "pluginUpdates", UpdateCheckerConfigurationImpl.class);
    }

    public UpdateCheckerBuilderConfiguration getConfiguration() {
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

