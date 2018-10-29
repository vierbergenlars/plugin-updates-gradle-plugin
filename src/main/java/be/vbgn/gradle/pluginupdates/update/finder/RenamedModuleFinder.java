package be.vbgn.gradle.pluginupdates.update.finder;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.version.Version;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.gradle.api.Transformer;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class RenamedModuleFinder implements UpdateFinder {

    @Nonnull
    private UpdateFinder updateFinder;
    @Nonnull
    private Transformer<Dependency, Dependency> renames;
    private static Logger LOGGER = Logging.getLogger(RenamedModuleFinder.class);

    public RenamedModuleFinder(@Nonnull UpdateFinder updateFinder,
            @Nonnull Transformer<Dependency, Dependency> renames) {
        this.updateFinder = updateFinder;
        this.renames = renames;
    }

    @Override
    @Nonnull
    public Stream<Dependency> findUpdates(@Nonnull Dependency dependency) {
        DependencyImpl dependencyWrapper = new DependencyImpl(dependency, false);
        Dependency transformed = renames.transform(dependencyWrapper);
        boolean withVersionCalled =
                (transformed instanceof DependencyImpl) ? ((DependencyImpl) transformed).withVersionCalled : true;
        // Unwrap transformed version from wrapper
        transformed = transformed instanceof DependencyImpl ? ((DependencyImpl) transformed).child : transformed;
        boolean coordinatesChanged = !dependency.getGroup().equals(transformed.getGroup()) || !dependency.getName()
                .equals(transformed.getName());
        if (coordinatesChanged && !withVersionCalled) {
            // If the coordinates have changed, but its version has not explicitly been changed,
            // set the version so a lookup for any version will be performed.
            transformed = transformed.withVersion("+");
            LOGGER.debug("Transformation of {} did not change version, using any version as dependency.", dependency);
        }
        if (!dependency.equals(transformed)) {
            LOGGER.debug("Transformation of {} changed dependency to {}", dependency, transformed);
        }

        return updateFinder.findUpdates(transformed);
    }

    private static class DependencyImpl implements Dependency {

        private Dependency child;
        private boolean withVersionCalled;


        private DependencyImpl(Dependency child, boolean withVersionCalled) {
            this.child = child;
            this.withVersionCalled = withVersionCalled;
        }

        @Nonnull
        @Override
        public String getGroup() {
            return child.getGroup();
        }

        @Nonnull
        @Override
        public String getName() {
            return child.getName();
        }

        @Nonnull
        @Override
        public Version getVersion() {
            return child.getVersion();
        }

        @Nonnull
        @Override
        public String getClassifier() {
            return child.getClassifier();
        }

        @Nonnull
        @Override
        public String getType() {
            return child.getType();
        }

        @Nonnull
        @Override
        public Dependency withGroup(@Nonnull String group) {
            return new DependencyImpl(child.withGroup(group), withVersionCalled);
        }

        @Nonnull
        @Override
        public Dependency withName(@Nonnull String name) {
            return new DependencyImpl(child.withName(name), withVersionCalled);
        }

        @Nonnull
        @Override
        public Dependency withVersion(@Nonnull Version version) {
            return new DependencyImpl(child.withVersion(version), true);
        }

        @Nonnull
        @Override
        public Dependency withClassifier(@Nullable String classifier) {
            return new DependencyImpl(child.withClassifier(classifier), withVersionCalled);
        }

        @Nonnull
        @Override
        public Dependency withType(@Nullable String type) {
            return new DependencyImpl(child.withType(type), withVersionCalled);
        }
    }
}
