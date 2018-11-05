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
    private UnaryOperator<Dependency> uncheckedTransformer = null;

    public ModuleRenameSpec(@Nonnull ModuleIdentifier subject) {
        this.subject = subject;
    }

    private boolean matchesModuleIdentifier(@Nonnull Dependency dependency) {
        return dependency.getGroup().equals(subject.getGroup()) && dependency.getName().equals(subject.getName());
    }

    @Override
    public void to(@Nonnull ModuleIdentifier module) {
        if (uncheckedTransformer != null) {
            throw new IllegalStateException("The rename target can only be specified once.");
        }
        uncheckedTransformer = dependency -> dependency.withGroup(module.getGroup()).withName(module.getName());
    }

    @Override
    public void to(@Nonnull Dependency dependency) {
        if (uncheckedTransformer != null) {
            throw new IllegalStateException("The rename target can only be specified once.");
        }
        uncheckedTransformer = dependency1 -> dependency1.withGroup(dependency.getGroup())
                .withName(dependency.getName()).withVersion(dependency.getVersion());
    }

    @Nonnull
    public UnaryOperator<Dependency> getTransformer() {
        if (uncheckedTransformer == null) {
            throw new IllegalStateException(
                    "You must call to() to set the rename target before fetching the transformer.");
        }
        return dependency -> {
            if (!matchesModuleIdentifier(dependency)) {
                return dependency;
            }
            return uncheckedTransformer.apply(dependency);
        };
    }
}
