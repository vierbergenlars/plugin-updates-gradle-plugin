package be.vbgn.gradle.pluginupdates.dsl.internal;

import be.vbgn.gradle.pluginupdates.dsl.UpdatePolicy;
import be.vbgn.gradle.pluginupdates.update.finder.UpdateFinder;
import be.vbgn.gradle.pluginupdates.update.finder.VersionProvider;
import javax.annotation.Nonnull;

public interface UpdateBuilder extends UpdatePolicy {
    @Nonnull
    VersionProvider buildVersionProvider(@Nonnull VersionProvider backingProvider);

    @Nonnull
    UpdateFinder buildUpdateFinder(@Nonnull UpdateFinder backingFinder);
}
