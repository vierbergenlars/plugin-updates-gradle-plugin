package be.vbgn.gradle.pluginupdates.dsl.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import be.vbgn.gradle.pluginupdates.dependency.DefaultDependency;
import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.dsl.IgnoreSpec;
import be.vbgn.gradle.pluginupdates.dsl.RenameSpec;
import be.vbgn.gradle.pluginupdates.update.finder.FailureAllowedVersion;
import be.vbgn.gradle.pluginupdates.update.finder.UpdateFinder;
import be.vbgn.gradle.pluginupdates.update.finder.VersionProvider;
import be.vbgn.gradle.pluginupdates.version.Version;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.gradle.api.artifacts.ModuleIdentifier;
import org.junit.Test;

public class MergedUpdatePolicyImplTest {

    static abstract class BuilderOnly implements UpdateBuilder {
        @Nonnull
        @Override
        public IgnoreSpec ignore(@Nonnull ModuleIdentifier module) {
            throw new UnsupportedOperationException();
        }

        @Nonnull
        @Override
        public IgnoreSpec ignore(@Nonnull Dependency dependency) {
            throw new UnsupportedOperationException();
        }

        @Nonnull
        @Override
        public RenameSpec rename(@Nonnull ModuleIdentifier module) {
            throw new UnsupportedOperationException();
        }

    }

    @Test
    public void buildVersionProvider() {
        Dependency markerDependency = new DefaultDependency("bla", "bla", "1.2.3");
        FailureAllowedVersion markerVersion = new FailureAllowedVersion(Version.parse("1.1.1"), true);
        VersionProvider original = new VersionProvider() {
            @Nonnull
            @Override
            public Stream<FailureAllowedVersion> getUpdateVersions(@Nonnull Dependency dependency) {
                if(dependency == markerDependency) {
                    return Stream.of(markerVersion);
                }
                throw new UnsupportedOperationException();
            }
        };

        VersionProvider provider1 = new VersionProvider() {
            @Nonnull
            @Override
            public Stream<FailureAllowedVersion> getUpdateVersions(@Nonnull Dependency dependency) {
                throw new UnsupportedOperationException();
            }
        };

        VersionProvider provider2 = new VersionProvider() {
            @Nonnull
            @Override
            public Stream<FailureAllowedVersion> getUpdateVersions(@Nonnull Dependency dependency) {
                throw new UnsupportedOperationException();
            }
        };


        UpdateBuilder updateBuilder1 = new BuilderOnly() {
            @Nonnull
            @Override
            public VersionProvider buildVersionProvider(@Nonnull VersionProvider backingProvider) {
                assertEquals(markerVersion, backingProvider.getUpdateVersions(markerDependency).findFirst().get());
                return provider1;
            }

            @Nonnull
            @Override
            public UpdateFinder buildUpdateFinder(@Nonnull UpdateFinder backingFinder) {
                throw new UnsupportedOperationException();
            }
        };

        UpdateBuilder updateBuilder2 = new BuilderOnly() {
            @Nonnull
            @Override
            public VersionProvider buildVersionProvider(@Nonnull VersionProvider backingProvider) {
                assertSame(provider1, backingProvider);
                return provider2;
            }

            @Nonnull
            @Override
            public UpdateFinder buildUpdateFinder(@Nonnull UpdateFinder backingFinder) {
                throw new UnsupportedOperationException();
            }
        };

        UpdateBuilder merged = new MergedUpdatePolicyImpl(Arrays.asList(updateBuilder1, updateBuilder2));

        assertSame(provider2, merged.buildVersionProvider(original));
    }

    @Test
    public void buildUpdateFinder() {
        Dependency markerDependency = new DefaultDependency("bla", "bla", "1.2.3");
        UpdateFinder original = new UpdateFinder() {
            @Nonnull
            @Override
            public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
                if(dependency == markerDependency) {
                    return Stream.of(dependency);
                }
                throw new UnsupportedOperationException();
            }
        };
        UpdateFinder finder1 = new UpdateFinder() {
            @Nonnull
            @Override
            public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
                throw new UnsupportedOperationException();
            }
        };

        UpdateFinder finder2 = new UpdateFinder() {
            @Nonnull
            @Override
            public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
                throw new UnsupportedOperationException();
            }
        };

        UpdateBuilder updateBuilder1 = new BuilderOnly() {
            @Nonnull
            @Override
            public VersionProvider buildVersionProvider(@Nonnull VersionProvider backingProvider) {
                throw new UnsupportedOperationException();
            }

            @Nonnull
            @Override
            public UpdateFinder buildUpdateFinder(@Nonnull UpdateFinder backingFinder) {
                assertSame(markerDependency, backingFinder.findUpdates(markerDependency).findFirst().get());
                return finder1;
            }
        };

        UpdateBuilder updateBuilder2 = new BuilderOnly() {
            @Nonnull
            @Override
            public VersionProvider buildVersionProvider(@Nonnull VersionProvider backingProvider) {
                throw new UnsupportedOperationException();
            }

            @Nonnull
            @Override
            public UpdateFinder buildUpdateFinder(@Nonnull UpdateFinder backingFinder) {
                assertSame(finder1, backingFinder);
                return finder2;
            }
        };

        UpdateBuilder merged = new MergedUpdatePolicyImpl(Arrays.asList(updateBuilder1, updateBuilder2));

        assertSame(finder2, merged.buildUpdateFinder(original));
    }

}
