package be.vbgn.gradle.pluginupdates.dsl.internal;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.dsl.RenameSpec;
import java.io.Serializable;
import java.util.function.UnaryOperator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.gradle.api.artifacts.ModuleIdentifier;

public class ModuleRenameSpec implements RenameSpec, Serializable {

    /**
     * Module that is the subject of this rename rule
     */
    @Nonnull
    private ModuleIdentifier subject;

    @Nullable
    private ModuleIdentifier targetModule;

    @Nullable
    private Dependency targetDependency;

    public ModuleRenameSpec(@Nonnull ModuleIdentifier subject) {
        this.subject = subject;
    }

    private boolean matchesModuleIdentifier(@Nonnull ModuleIdentifier dependency) {
        return dependency.getGroup().equals(subject.getGroup()) && dependency.getName().equals(subject.getName());
    }

    private void validateEmpty() {
        if (targetModule != null || targetDependency != null) {
            throw new IllegalStateException("The rename target can only be specified once.");
        }
    }

    @Override
    public void to(@Nonnull ModuleIdentifier module) {
        validateEmpty();
        targetModule = module;
    }

    @Override
    public void to(@Nonnull Dependency dependency) {
        validateEmpty();
        targetDependency = dependency;
    }

    @Nonnull
    public UnaryOperator<Dependency> getTransformer() {
        if (targetModule != null) {
            return dependency -> {
                if (!matchesModuleIdentifier(dependency)) {
                    return dependency;
                }
                return dependency.withGroup(targetModule.getGroup()).withName(targetModule.getName());
            };
        }

        if (targetDependency != null) {
            return dependency -> {
                if (!matchesModuleIdentifier(dependency)) {
                    return dependency;
                }
                return dependency.withGroup(targetDependency.getGroup()).withName(targetDependency.getName())
                        .withVersion(targetDependency.getVersion());
            };

        }
        throw new IllegalStateException(
                "You must call to() to set the rename target before fetching the transformer.");
    }
}
