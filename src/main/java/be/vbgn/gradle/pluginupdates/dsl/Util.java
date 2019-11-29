package be.vbgn.gradle.pluginupdates.dsl;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.DefaultModuleIdentifier;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.gradle.api.artifacts.ModuleIdentifier;

/**
 * Utilities to create {@link Dependency} and {@link ModuleIdentifier} from alternative notations
 */
final class Util {

    private Util() {
    }

    /**
     * @param moduleNotation Module identifier specified as <code>&lt;group&gt;:&lt;name&gt;</code>
     * @return Module identifier created from the notation
     * @throws BadNotationException When the module identifier does not conform to the required format
     */
    @Nonnull
    static ModuleIdentifier createModuleIdentifier(@Nonnull String moduleNotation) {
        String[] parts = moduleNotation.split(":");

        if (parts.length != 2) {
            throw new BadNotationException("Module notation must contain 2 parts separated by colon.");
        }

        Map<String, String> notation = new HashMap<>(3);
        notation.put("group", parts[0]);
        notation.put("name", parts[1]);

        return createModuleIdentifier(notation);
    }

    /**
     * @param moduleNotation Module identifier specified as a map with keys <code>group</code> and <code>name</code>
     * @return Module identifier created from the notation
     * @throws BadNotationException When the module identifier does not conform to the required format
     */
    @Nonnull
    static ModuleIdentifier createModuleIdentifier(@Nonnull Map<String, String> moduleNotation) {
        if (!moduleNotation.containsKey("group")) {
            throw new BadNotationException("Module notation must contain a 'group' specifier.");
        }
        if (!moduleNotation.containsKey("name")) {
            throw new BadNotationException("Module notation must contain a 'name' specifier.");
        }
        if (moduleNotation.size() != 2) {
            throw new BadNotationException("Module notation can only contain 'group' and 'name' specifiers.");
        }
        return new DefaultModuleIdentifier(moduleNotation.get("group"), moduleNotation.get("name"));

    }

    /**
     * @param dependencyNotation Dependency specified as <code>&lt;group&gt;:&lt;name&gt;:&lt;version&gt;</code>
     * @return Dependency created from the notation
     * @throws BadNotationException When the dependency notation does not conform to the required format
     */
    @Nonnull
    static Dependency createDependency(@Nonnull String dependencyNotation) {
        String[] parts = dependencyNotation.split(":");

        if (parts.length != 3) {
            throw new BadNotationException("Dependency notation must contain 3 parts separated by colon.");
        }

        Map<String, String> notation = new HashMap<>(3);
        notation.put("group", parts[0]);
        notation.put("name", parts[1]);
        notation.put("version", parts[2]);

        return createDependency(notation);
    }

    /**
     * @param dependencyNotation Dependency specified as a map with keys <code>group</code>, <code>name</code> and <code>version</code>
     * @return Dependency created from the notation
     * @throws BadNotationException When the dependency notation does not conform to the required format
     */
    @Nonnull
    static Dependency createDependency(@Nonnull Map<String, String> dependencyNotation) {
        if (!dependencyNotation.containsKey("group")) {
            throw new BadNotationException("Dependency notation must contain a 'group' specifier.");
        }
        if (!dependencyNotation.containsKey("name")) {
            throw new BadNotationException("Dependency notation must contain a 'name' specifier.");
        }
        if (!dependencyNotation.containsKey("version")) {
            throw new BadNotationException("Dependency notation must contain a 'version' specifier.");
        }
        if (dependencyNotation.size() != 3) {
            throw new BadNotationException(
                    "Dependency notation can only contain 'group', 'name' and 'version' specifiers.");
        }

        return new DefaultDependency(dependencyNotation.get("group"), dependencyNotation.get("name"),
                dependencyNotation.get("version"));
    }

    /**
     * Calls a callback function with a newly created {@link Dependency} or a {@link ModuleIdentifier}, depending on the notation.
     *
     * @param notation     A dependency or a module identifier notation in string format.
     * @param moduleFn     Callback function that is called when a module identifier notation is used
     * @param dependencyFn Callback function that is called when a dependency notation is used
     * @param <T>          The type that will be created to wrap the dependency or module
     * @return An arbitrary object that will have handled the module identifier or the dependency
     * @throws BadNotationException When the dependency notation does not conform to any of {@link #createDependency(String)} and {@link #createModuleIdentifier(String)}
     * @see #createDependency(String)
     * @see #createModuleIdentifier(String)
     */
    static <T> T createDependencyOrModule(@Nonnull String notation, @Nonnull Function<ModuleIdentifier, T> moduleFn,
            @Nonnull Function<Dependency, T> dependencyFn) {
        try {
            return dependencyFn.apply(Util.createDependency(notation));
        } catch (BadNotationException e1) {
            try {
                return moduleFn.apply(Util.createModuleIdentifier(notation));
            } catch (BadNotationException e2) {
                throw new BadNotationException(
                        "Module identifier or dependency notation must contain between 2 and 3 colon separated parts.");
            }
        }
    }

    /**
     * Calls a callback function with a newly created {@link Dependency} or a {@link ModuleIdentifier}, depending on the notation.
     *
     * @param notation     A dependency or a module identifier notation in map format.
     * @param moduleFn     Callback function that is called when a module identifier notation is used
     * @param dependencyFn Callback function that is called when a dependency notation is used
     * @param <T>          The type that will be created to wrap the dependency or module
     * @return An arbitrary object that will have handled the module identifier or the dependency
     * @throws BadNotationException When the dependency notation does not conform to any of {@link #createDependency(Map)} and {@link #createModuleIdentifier(Map)}
     * @see #createDependency(Map)
     * @see #createModuleIdentifier(Map)
     */
    static <T> T createDependencyOrModule(@Nonnull Map<String, String> notation,
            @Nonnull Function<ModuleIdentifier, T> moduleFn,
            @Nonnull Function<Dependency, T> dependencyFn) {
        try {
            return dependencyFn.apply(Util.createDependency(notation));
        } catch (BadNotationException e1) {
            try {
                return moduleFn.apply(Util.createModuleIdentifier(notation));
            } catch (BadNotationException e2) {
                throw new BadNotationException(
                        "Module identifier or dependency notation must contain a 'group' and 'name' specifier and optionally a 'version' specifier.");
            }
        }
    }
}
