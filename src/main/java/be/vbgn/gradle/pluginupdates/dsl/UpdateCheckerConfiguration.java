package be.vbgn.gradle.pluginupdates.dsl;

import groovy.lang.Closure;
import javax.annotation.Nonnull;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

/**
 * Contains all configuration for the {@link be.vbgn.gradle.pluginupdates.PluginUpdatesPlugin}
 *
 * <pre>
 *  apply plugin: be.vbgn.gradle.pluginupdates.ConfigurationPlugin
 *  pluginUpdates {
 *      policy {
 *          // Change the group and name of a plugin. Any version of the eu.xenit.gradle:alfresco-docker-plugin will be
 *          // suggested as a replacement of any version of eu.xenit.gradle:xenit-gradle-plugins
 *          rename "eu.xenit.gradle:xenit-gradle-plugins" to "eu.xenit.gradle:alfresco-docker-plugin"
 *
 *          // Change the group and name of a plugin. Any version >= 0.1.4 of eu.xenit.gradle:alfresco-sdk will be
 *          // suggested as a replacement of any version of eu.xenit.gradle.plugins:ampde_gradle_plugin
 *          rename "eu.xenit.gradle.plugins:ampde_gradle_plugin" to "eu.xenit.gradle:alfresco-sdk:0.1.4+"
 *
 *          // Ignore a certain level of updates for a plugin
 *          ignore "gradle.plugin.com.github.eerohele:saxon-gradle" majorUpdates() // Ignore major updates for a plugin
 *          ignore "eu.xenit.gradle:alfresco-sdk" minorUpdates() // Ignore minor updates for a plugin
 *          ignore "org.springframework.boot:spring-boot-gradle-plugin" microUpdates() // Ignore micro updates for a plugin
 *
 *          // Ignore a certain version from updates for a plugin
 *          ignore "eu.xenit.gradle:alfresco-sdk:0.1.3" because "It contains a critical bug"
 *
 *          // Ignore all updates for a plugin
 *          ignore "be.vbgn.gradle:plugin-updates-plugin" because "We don't ever want to update this plugin"
 *      }
 *  }
 * </pre>
 */
public interface UpdateCheckerConfiguration {

    /**
     * A policy controls which plugins are checked for updates.
     */
    @Nonnull
    UpdatePolicy getPolicy();

    /**
     * Immediately configures the update policy
     */
    default void policy(@Nonnull Action<? super UpdatePolicy> policy) {
        policy.execute(getPolicy());
    }

    /**
     * Immediately configures the update policy
     *
     * @deprecated Use {@link #policy(Action)} instead. This method can also be used automatically by gradle as target for closures
     */
    @Deprecated
    default void policy(@Nonnull Closure policy) {
        policy(ConfigureUtil.configureUsing(policy));
    }

}
