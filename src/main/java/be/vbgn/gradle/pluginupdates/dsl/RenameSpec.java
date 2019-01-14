package be.vbgn.gradle.pluginupdates.dsl;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import java.util.Map;
import javax.annotation.Nonnull;
import org.gradle.api.artifacts.ModuleIdentifier;

/**
 * Specification of which module/dependency a module has been renamed to
 * <p>
 * You are required to call exactly one of the <code>to()</code> methods for the object to be in a valid state.
 */
public interface RenameSpec {

    /**
     * Rename to an other module (no version constraint)
     * <p>
     * <b>Typically, this method is not used directly, but a dependency notation is used with {@link #to(String)} or {@link #to(Map)}</b>
     * <p>
     * Any version of the target module is valid as a replacement for the source module, even if it is of a lower version than the source module.
     * <p>
     * Example: for the renameSpec <code>rename 'com.example.a:package' to 'com.example.b:other'</code>,
     * the outcome <code>com.example.a:package:1.2.3 -> com.example.b:other:1.0.0</code> is valid
     *
     * @param module The module identifier of the target module that the source module has been renamed to
     */
    void to(@Nonnull ModuleIdentifier module);

    /**
     * Rename to an other dependency (specific version or version constraint)
     * <p>
     * <b>Typically, this method is not used directly, but a dependency notation is used with {@link #to(String)} or {@link #to(Map)}</b>
     * <p>
     * Only versions of the target module that match the version/version constraint are valid as a replacement of the source module.
     * <p>
     * Example: for the renameSpec <code>rename 'com.example.a:package' to 'com.example.b:other:1.+'</code>,
     * the outcome <code>com.example.a:package:1.2.3 -> com.example.b:other:1.0.0</code> is valid.
     * But for the same renameSpec, the outcome <code>com.example.a:package:1.2.3 -> com.example.b:other:0.3.4</code> is invalid and will not be proposed
     *
     * @param dependency The dependency that the source module has been renamed to
     */
    void to(@Nonnull Dependency dependency);

    /**
     * Rename to a module (any version) or dependency (specific version or version constraint)
     *
     * @param moduleNotation Module specified as <code>&lt;group&gt;:&lt;name&gt;</code> or dependency specified as <code>&lt;group&gt;:&lt;name&gt;:&lt;version&gt;</code>
     * @throws BadNotationException When the dependency notation can not be parsed to a {@link Dependency} or {@link ModuleIdentifier}
     * @see #to(Dependency)
     * @see #to(ModuleIdentifier)
     */
    default void to(@Nonnull String moduleNotation) {
        Util.createDependencyOrModule(moduleNotation, m -> {
            this.to(m);
            return null;
        }, d -> {
            this.to(d);
            return null;
        });
    }

    /**
     * Rename to a module (any version) or dependency (specific version or version constraint)
     *
     * @param moduleNotation Module specified as a map with keys <code>group</code> and <code>name</code>, or a dependency specified as a map with keys <code>group</code>, <code>name</code> and <code>version</code>
     * @throws BadNotationException When the dependency notation can not be parsed to a {@link Dependency} or {@link ModuleIdentifier}
     * @see #to(Dependency)
     * @see #to(ModuleIdentifier)
     */
    default void to(@Nonnull Map<String, String> moduleNotation) {
        Util.createDependencyOrModule(moduleNotation, m -> {
            this.to(m);
            return null;
        }, d -> {
            this.to(d);
            return null;
        });
    }
}
