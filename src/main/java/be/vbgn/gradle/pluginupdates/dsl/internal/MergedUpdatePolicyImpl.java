package be.vbgn.gradle.pluginupdates.dsl.internal;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.dsl.IgnoreSpec;
import be.vbgn.gradle.pluginupdates.dsl.RenameSpec;
import be.vbgn.gradle.pluginupdates.update.finder.UpdateFinder;
import be.vbgn.gradle.pluginupdates.update.finder.VersionProvider;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import org.gradle.api.artifacts.ModuleIdentifier;

public class MergedUpdatePolicyImpl implements UpdateBuilder {

    private List<UpdateBuilder> builders;

    public MergedUpdatePolicyImpl(List<UpdateBuilder> builders) {
        this.builders = new LinkedList<>();
        this.builders.add(new UpdatePolicyImpl());
        this.builders.addAll(builders);
    }

    @Nonnull
    @Override
    public VersionProvider buildVersionProvider(@Nonnull VersionProvider backingProvider) {
        for (UpdateBuilder builder : builders) {
            backingProvider = builder.buildVersionProvider(backingProvider);
        }
        return backingProvider;
    }

    @Nonnull
    @Override
    public UpdateFinder buildUpdateFinder(@Nonnull UpdateFinder backingFinder) {
        for (UpdateBuilder builder : builders) {
            backingFinder = builder.buildUpdateFinder(backingFinder);
        }
        return backingFinder;
    }

    @Nonnull
    @Override
    public IgnoreSpec ignore(@Nonnull ModuleIdentifier module) {
        return builders.get(0).ignore(module);
    }

    @Nonnull
    @Override
    public IgnoreSpec ignore(@Nonnull Dependency dependency) {
        return builders.get(0).ignore(dependency);
    }

    @Nonnull
    @Override
    public RenameSpec rename(@Nonnull ModuleIdentifier module) {
        return builders.get(0).rename(module);
    }
}
