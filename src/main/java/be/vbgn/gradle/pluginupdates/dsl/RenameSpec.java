package be.vbgn.gradle.pluginupdates.dsl;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import java.util.Map;
import javax.annotation.Nonnull;
import org.gradle.api.artifacts.ModuleIdentifier;

public interface RenameSpec {

    void to(@Nonnull ModuleIdentifier module);

    void to(@Nonnull Dependency dependency);

    default void to(@Nonnull String moduleNotation) {
        try {
            to(Util.createDependency(moduleNotation));
        } catch (BadNotationException e) {
            to(Util.createModuleIdentifier(moduleNotation));
        }
    }

    default void to(@Nonnull Map<String, String> moduleNotation) {
        try {
            to(Util.createDependency(moduleNotation));
        } catch (BadNotationException e) {
            to(Util.createModuleIdentifier(moduleNotation));
        }
    }
}
