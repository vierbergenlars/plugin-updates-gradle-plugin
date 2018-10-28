package be.vbgn.gradle.pluginupdates.dependency;

import be.vbgn.gradle.pluginupdates.version.Version;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.gradle.api.artifacts.DependencyArtifact;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;

public class DefaultDependency implements Dependency {

    @Nonnull
    private String group;
    @Nonnull
    private String name;
    @Nonnull
    private Version version;
    @Nullable
    private String classifier;
    @Nullable
    private String type;

    public DefaultDependency(@Nonnull String group, @Nonnull String name, @Nonnull Version version) {
        this(group, name, version, "", DEFAULT_TYPE);
    }

    public DefaultDependency(@Nonnull String group, @Nonnull String name, @Nonnull String version) {
        this(group, name, Version.parse(version));
    }

    public DefaultDependency(@Nonnull String group, @Nonnull String name, @Nonnull String version,
            @Nullable String classifier, @Nullable String type) {
        this(group, name, Version.parse(version), classifier, type);
    }

    public DefaultDependency(@Nonnull String group, @Nonnull String name, @Nonnull Version version,
            @Nullable String classifier, @Nullable String type) {
        this.group = group;
        this.name = name;
        this.version = version;
        this.classifier = classifier;
        this.type = type;
    }

    private DefaultDependency(@Nonnull ResolvedDependency dependency, @Nullable ResolvedArtifact artifact) {
        this(dependency.getModuleGroup(), dependency.getModuleName(), dependency.getModuleVersion(),
                artifact != null ? artifact.getClassifier() : null, artifact != null ? artifact.getType() : null);
    }


    private DefaultDependency(@Nonnull ExternalDependency dependency, @Nullable DependencyArtifact artifact) {
        this(Objects.requireNonNull(dependency.getGroup()), dependency.getName(),
                Objects.requireNonNull(dependency.getVersion()),
                artifact != null ? artifact.getClassifier() : null, artifact != null ? artifact.getType() : null);
    }

    @Nonnull
    public static Stream<DefaultDependency> fromGradle(@Nonnull ExternalDependency dependency) {
        if (dependency.getArtifacts().isEmpty()) {
            return Stream.of(new DefaultDependency(dependency, null));
        }
        return dependency.getArtifacts().stream()
                .map(artifact -> new DefaultDependency(dependency, artifact));
    }

    @Nonnull
    public static Stream<DefaultDependency> fromGradle(@Nonnull ResolvedDependency dependency) {
        if (dependency.getModuleArtifacts().isEmpty()) {
            return Stream.of(new DefaultDependency(dependency, null));
        }
        return dependency.getModuleArtifacts().stream()
                .map(artifact -> new DefaultDependency(dependency, artifact));
    }

    @Override
    @Nonnull
    public String getGroup() {
        return group;
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    @Nonnull
    public Version getVersion() {
        return version;
    }

    @Override
    @Nonnull
    public String getClassifier() {
        if (classifier == null) {
            return "";
        }
        return classifier;
    }

    @Override
    @Nonnull
    public String getType() {
        if (type == null) {
            return DEFAULT_TYPE;
        }
        return type;
    }

    @Override
    @Nonnull
    public DefaultDependency withGroup(String group) {
        return new DefaultDependency(group, name, version, classifier, type);
    }

    @Override
    @Nonnull
    public DefaultDependency withName(String name) {
        return new DefaultDependency(group, name, version, classifier, type);
    }

    @Override
    @Nonnull
    public DefaultDependency withVersion(Version version) {
        return new DefaultDependency(group, name, version, classifier, type);
    }

    @Override
    @Nonnull
    public DefaultDependency withClassifier(String classifier) {
        return new DefaultDependency(group, name, version, classifier, type);
    }

    @Override
    @Nonnull
    public DefaultDependency withType(String type) {
        return new DefaultDependency(group, name, version, classifier, type);
    }

    @Override
    public String toString() {
        String dependencyNotation = getGroup() + ":" + getName() + ":" + getVersion().toString();
        if (!getClassifier().isEmpty()) {
            dependencyNotation += ":" + getClassifier();
        }
        if (!getType().equals(DEFAULT_TYPE)) {
            dependencyNotation += "@" + getType();
        }
        return dependencyNotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultDependency that = (DefaultDependency) o;
        return Objects.equals(getGroup(), that.getGroup()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getVersion(), that.getVersion()) &&
                Objects.equals(getClassifier(), that.getClassifier()) &&
                Objects.equals(getType(), that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGroup(), getName(), getVersion(), getClassifier(), getType());
    }
}
