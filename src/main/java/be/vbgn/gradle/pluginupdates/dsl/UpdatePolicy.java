package be.vbgn.gradle.pluginupdates.dsl;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import java.util.Map;
import javax.annotation.Nonnull;
import org.gradle.api.artifacts.ModuleIdentifier;

public interface UpdatePolicy {

    @Nonnull
    IgnoreSpec ignore(@Nonnull ModuleIdentifier module);

    @Nonnull
    IgnoreSpec ignore(@Nonnull Dependency dependency);

    @Nonnull
    default IgnoreSpec ignore(@Nonnull String dependencyNotation) {
        return Util.createDependencyOrModule(dependencyNotation, this::ignore, this::ignore);
    }

    @Nonnull
    default IgnoreSpec ignore(@Nonnull Map<String, String> dependencyNotation) {
        return Util.createDependencyOrModule(dependencyNotation, this::ignore, this::ignore);
    }

    @Nonnull
    RenameSpec rename(@Nonnull ModuleIdentifier module);

    @Nonnull
    default RenameSpec rename(@Nonnull String moduleNotation) {
        return rename(Util.createModuleIdentifier(moduleNotation));

    }

    @Nonnull
    default RenameSpec rename(@Nonnull Map<String, String> moduleNotation) {
        return rename(Util.createModuleIdentifier(moduleNotation));
    }

}
