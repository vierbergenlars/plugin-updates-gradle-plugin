package be.vbgn.gradle.pluginupdates.dsl;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import java.util.Map;
import javax.annotation.Nonnull;
import org.gradle.api.artifacts.ModuleIdentifier;

/**
 * An update policy specifies which dependencies are ignored from updates, and which dependencies have been renamed to another module name:w
 * <p>
 * The update policy has a fluent DSL to:
 * <ul>
 * <li>specify modules/dependencies that are ignored with {@link #ignore(String)} and {@link #ignore(Map)}
 * <li>specify modules that have been renamed with {@link #rename(String)} and {@link #rename(Map)}
 * </ul>
 */
public interface UpdatePolicy {

    /**
     * Ignores all versions of a module
     * <p>
     * <b>Typically, this method is not used directly, but a dependency notation is used with {@link #ignore(String)} or {@link #ignore(Map)}</b>
     * <p>
     * By default, an {@link IgnoreSpec} ignores the whole module, unless the ignore specification is reduced by calling a method on {@link IgnoreSpec}
     * <p>
     * Examples:
     * <pre>
     * ignore "gradle.plugin.com.github.eerohele:saxon-gradle" majorUpdates() // Ignore major updates for a plugin
     * ignore "eu.xenit.gradle:alfresco-sdk" minorUpdates() // Ignore minor updates for a plugin
     * ignore "org.springframework.boot:spring-boot-gradle-plugin" microUpdates() // Ignore micro updates for a plugin
     *
     * // Ignore all updates for a plugin
     * ignore "be.vbgn.gradle:plugin-updates-plugin"
     * </pre>
     *
     * @param module The module identifier to ignore updates for.
     */
    @Nonnull
    IgnoreSpec ignore(@Nonnull ModuleIdentifier module);

    /**
     * Ignores a specific version or version range of a dependency
     *
     * <p>
     * <b>Typically, this method is not used directly, but a dependency notation is used with {@link #ignore(String)} or {@link #ignore(Map)}</b>
     * <p>
     * A specific version, or a version range is ignored by the {@link IgnoreSpec}, unless the ignore specification is reduced by calling a method on {@link IgnoreSpec}
     * <p>
     * Example:
     * <pre>
     * // Ignore a certain version from updates for a plugin
     * ignore "eu.xenit.gradle:alfresco-sdk:0.1.3"
     * </pre>
     *
     * @param dependency The dependency to ignore when updating
     */
    @Nonnull
    IgnoreSpec ignore(@Nonnull Dependency dependency);

    /**
     * Ignores a module (all versions) or a dependency (a specific version/version range)
     *
     * @param dependencyNotation Module specified as <code>&lt;group&gt;:&lt;name&gt;</code> or dependency specified as <code>&lt;group&gt;:&lt;name&gt;:&lt;version&gt;</code>
     * @throws BadNotationException When the dependency notation can not be parsed to a {@link Dependency} or {@link ModuleIdentifier}
     * @see #ignore(Dependency)
     * @see #ignore(ModuleIdentifier)
     */
    @Nonnull
    default IgnoreSpec ignore(@Nonnull String dependencyNotation) {
        return Util.createDependencyOrModule(dependencyNotation, this::ignore, this::ignore);
    }

    /**
     * Ignores a module (all versions) or a dependency (a specific version/version range)
     *
     * @param dependencyNotation Module specified as a map with keys <code>group</code> and <code>name</code>, or a dependency specified as a map with keys <code>group</code>, <code>name</code> and <code>version</code>
     * @throws BadNotationException When the dependency notation can not be parsed to a {@link Dependency} or {@link ModuleIdentifier}
     * @see #ignore(Dependency)
     * @see #ignore(ModuleIdentifier)
     */
    @Nonnull
    default IgnoreSpec ignore(@Nonnull Map<String, String> dependencyNotation) {
        return Util.createDependencyOrModule(dependencyNotation, this::ignore, this::ignore);
    }

    /**
     * Marks a module as renamed to an other module
     * <p>
     * <b>Typically, this method is not used directly, but a dependency notation is used with {@link #rename(String)} or {@link #rename(Map)}</b>
     * <p>
     * The module will not be checked for updates, but will be marked as superseded by an other module
     * <p>
     * Example:
     * <pre>
     *  // Change the group and name of a plugin. Any version of the eu.xenit.gradle:alfresco-docker-plugin will be
     *  // suggested as a replacement of any version of eu.xenit.gradle:xenit-gradle-plugins
     *  rename "eu.xenit.gradle:xenit-gradle-plugins" to "eu.xenit.gradle:alfresco-docker-plugin"
     *
     *  // Change the group and name of a plugin. Any version >= 0.1.4 of eu.xenit.gradle:alfresco-sdk will be
     *  // suggested as a replacement of any version of eu.xenit.gradle.plugins:ampde_gradle_plugin
     *  rename "eu.xenit.gradle.plugins:ampde_gradle_plugin" to "eu.xenit.gradle:alfresco-sdk:0.1.4+"
     * </pre>
     *
     * @param module The module identifier that will be marked as superseded
     */
    @Nonnull
    RenameSpec rename(@Nonnull ModuleIdentifier module);

    /**
     * Marks a module as renamed
     *
     * @param moduleNotation Module specified as <code>&lt;group&gt;:&lt;name&gt;</code>
     * @throws BadNotationException When the dependency notation can not be parsed to a {@link ModuleIdentifier}
     * @see #rename(ModuleIdentifier)
     */
    @Nonnull
    default RenameSpec rename(@Nonnull String moduleNotation) {
        return rename(Util.createModuleIdentifier(moduleNotation));

    }

    /**
     * Marks a module as renamed
     *
     * @param moduleNotation Module specified as a map with keys <code>group</code> and <code>name</code>,
     * @throws BadNotationException When the dependency notation can not be parsed to a {@link ModuleIdentifier}
     * @see #rename(ModuleIdentifier)
     */
    @Nonnull
    default RenameSpec rename(@Nonnull Map<String, String> moduleNotation) {
        return rename(Util.createModuleIdentifier(moduleNotation));
    }

}
