package be.vbgn.gradle.pluginupdates.dependency;

import be.vbgn.gradle.pluginupdates.version.Version;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.gradle.api.artifacts.DependencyArtifact;
import org.gradle.api.artifacts.ModuleIdentifier;

/**
 * A {@link Dependency} represents a dependency on a certain artifact from an external source.
 * <p>
 * A dependency is composed of 5 components: group, name, version, classifier and type.
 */
public interface Dependency extends ModuleIdentifier {
    /**
     * The default {@link #getType()} of a dependency
     */
    String DEFAULT_TYPE = DependencyArtifact.DEFAULT_TYPE;

    /**
     * @return the group of this dependency. The group is often required to find the artifacts of a dependency in a
     * repository. For example, the group name corresponds to a directory name in a Maven like repository.
     */
    @Override
    @Nonnull
    String getGroup();

    /**
     * @return the name of this dependency. The name is almost always required to find the artifacts of a dependency in
     * a repository.
     */
    @Override
    @Nonnull
    String getName();

    /**
     * @return the version of this dependency. The version is often required to find the artifacts of a dependency in a
     * repository. For example the version name corresponds to a directory name in a Maven like repository.
     */
    @Nonnull
    Version getVersion();

    /**
     * @return the classifier of this dependency artifact. A classifier is used to distinguish different distributions of the same dependency.
     */
    @Nonnull
    String getClassifier();

    /**
     * @return the type of this dependency artifact. Often the type is the same as the extension,
     * but sometimes this is not the case.
     */
    @Nonnull
    String getType();

    /**
     * Creates a new dependency instance with a new {@link #getGroup()} identifier
     *
     * @param group The new group identifier to use
     * @return A new {@link Dependency} instance with the {@link #getGroup()} replaced with a new value
     */
    @Nonnull
    Dependency withGroup(@Nonnull String group);

    /**
     * Creates a new dependency instance with a new {@link #getName()} identifier
     *
     * @param name The new name identifier to use
     * @return A new {@link Dependency} instance with the {@link #getName()} replaced with a new value
     */
    @Nonnull
    Dependency withName(@Nonnull String name);

    /**
     * Creates a new dependency instance with a new {@link #getVersion()} identifier
     *
     * @param version The new version identifier to use
     * @return A new {@link Dependency} instance with the {@link #getVersion()} replaced with a new value
     *
     * <b>Implementation note</b>
     *
     * This default implementation uses {@link Version#parse(String)} and {@link #withVersion(Version)} to set the version
     */
    @Nonnull
    default Dependency withVersion(String version) {
        return withVersion(Version.parse(version));
    }

    /**
     * Creates a new dependency instance with a new {@link #getVersion()} identifier
     *
     * @param version The new version identifier to use
     * @return A new {@link Dependency} instance with the {@link #getVersion()} replaced with a new value
     */
    @Nonnull
    Dependency withVersion(@Nonnull Version version);

    /**
     * Creates a new dependency instance with a new {@link #getClassifier()} identifier
     *
     * @param classifier The new classifier identifier to use
     * @return A new {@link Dependency} instance with the {@link #getClassifier()} replaced with a new value
     */
    @Nonnull
    Dependency withClassifier(@Nullable String classifier);

    /**
     * Creates a new dependency instance with a new {@link #getType()} identifier
     *
     * @param type The new type identifier to use
     * @return A new {@link Dependency} instance with the {@link #getType()} replaced with a new value
     */
    @Nonnull
    Dependency withType(@Nullable String type);


    /**
     * @return the Gradle dependency notation that matches this dependency
     */
    @Nonnull
    default Map<String, String> toDependencyNotation() {
        Map<String, String> dependencyNotation = new HashMap<>(5, 1F);
        dependencyNotation.put("group", getGroup());
        dependencyNotation.put("name", getName());
        dependencyNotation.put("version", getVersion().toString());
        if (!getClassifier().isEmpty()) {
            dependencyNotation.put("classifier", getClassifier());
        }
        if (!getType().equals(DEFAULT_TYPE)) {
            dependencyNotation.put("ext", getType());
        }

        return Collections.unmodifiableMap(dependencyNotation);
    }

}
