package be.vbgn.gradle.pluginupdates.dsl;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.DefaultModuleIdentifier;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.gradle.api.artifacts.ModuleIdentifier;

class Util {

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
