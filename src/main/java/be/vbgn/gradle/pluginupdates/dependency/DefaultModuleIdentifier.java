package be.vbgn.gradle.pluginupdates.dependency;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.gradle.api.artifacts.ModuleIdentifier;

public class DefaultModuleIdentifier implements ModuleIdentifier, Serializable {

    @Nonnull
    private String group;

    @Nonnull
    private String name;

    public DefaultModuleIdentifier(@Nonnull String group, @Nonnull String name) {
        this.group = Objects.requireNonNull(group);
        this.name = Objects.requireNonNull(name);
    }

    @Nonnull
    @Override
    public String getGroup() {
        return group;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }
}
