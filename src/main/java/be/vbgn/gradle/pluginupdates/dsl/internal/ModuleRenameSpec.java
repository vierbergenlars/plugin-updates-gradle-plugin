package be.vbgn.gradle.pluginupdates.dsl.internal;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.dsl.RenameSpec;
import java.io.Serializable;
import java.util.function.UnaryOperator;
import javax.annotation.Nonnull;
import org.gradle.api.artifacts.ModuleIdentifier;

public class ModuleRenameSpec implements RenameSpec, Serializable {

    /**
     * Module that is the subject of this rename rule
     */
    @Nonnull
    private ModuleIdentifier subject;

    /**
     * Module that the subject will be renamed to
     */
    @Nonnull
    private ModuleIdentifier moduleTarget = null;

    private UnaryOperator<Dependency> uncheckedTransformer = UnaryOperator.identity();

    public ModuleRenameSpec(@Nonnull ModuleIdentifier subject) {
        this.subject = subject;
    }

    private boolean matchesModuleIdentifier(@Nonnull Dependency dependency) {
        return dependency.getGroup().equals(subject.getGroup()) && dependency.getName().equals(subject.getName());
    }

    @Override
    public void to(@Nonnull ModuleIdentifier module) {
        uncheckedTransformer = dependency -> dependency.withGroup(module.getGroup()).withName(module.getName());
    }

    @Override
    public void to(@Nonnull Dependency dependency) {
        uncheckedTransformer = dependency1 -> dependency1.withGroup(dependency.getGroup())
                .withName(dependency.getName()).withVersion(dependency.getVersion());
    }

    @Nonnull
    public UnaryOperator<Dependency> getTransformer() {
        return dependency -> {
            if (!matchesModuleIdentifier(dependency)) {
                return dependency;
            }
            return uncheckedTransformer.apply(dependency);
        };
    }
}
