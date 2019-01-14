package be.vbgn.gradle.pluginupdates.dsl;

import javax.annotation.Nonnull;

/**
 * Specification of which updates to ignore for a {@link be.vbgn.gradle.pluginupdates.dependency.Dependency} or {@link org.gradle.api.artifacts.ModuleIdentifier}
 * <p>
 * When no restricting methods are called, it defaults to ignoring the module or dependency as a whole.
 */
public interface IgnoreSpec {

    /**
     * Ignore all major updates for a module or dependency
     * <p>
     * This will avoid suggesting updates that change the first part of the version number
     * <p>
     * Example: <code>1.2.3 -> 2.0.1</code> will be ignored, but <code>1.2.3 -> 1.3.0</code> will not be ignored
     *
     * @return for chaining with {@link #because(String)}
     * @see be.vbgn.gradle.pluginupdates.version.Version
     */
    @Nonnull
    IgnoreSpec majorUpdates();

    /**
     * Ignore all minor updates for a module or dependency
     * <p>
     * This will avoid suggesting updates that change the first or second part of the version number
     * Example: <code>1.2.3 -> 2.3.0</code> will be ignored, but <code>1.2.3 -> 1.2.4</code> will not be ignored
     * <p>
     * Also ignores {@link #majorUpdates()}
     *
     * @return for chaining with {@link #because(String)}
     * @see be.vbgn.gradle.pluginupdates.version.Version
     */
    @Nonnull
    IgnoreSpec minorUpdates();

    /**
     * Ignore all micro updates for a module or dependency
     * <p>
     * This will avoid suggesting updates that change the first, second or third part of the version number.
     * A version number has 3 or 4 parts, see {@link be.vbgn.gradle.pluginupdates.version.Version} for details.
     * <p>
     * Example <code>1.2.3 -> 1.2.4</code> will be ignored, but <code>1.2.3 -> 1.2.3.1</code> or <code>1.2.3.2 -> 1.2.3.3</code> will not be ignored
     * <p>
     * Also ignores {@link #minorUpdates()} and {@link #majorUpdates()}
     *
     * @return for chaining with {@link #because(String)}
     * @see be.vbgn.gradle.pluginupdates.version.Version
     */
    @Nonnull
    IgnoreSpec microUpdates();

    /**
     * Allows specifying a reason why this policy rule is applied.
     * <p>
     * The value passed to this function is not used anywhere, but is only used for documentation purposes
     */
    void because(@Nonnull String reason);
}
