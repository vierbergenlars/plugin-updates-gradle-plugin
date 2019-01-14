package be.vbgn.gradle.pluginupdates.dependency;

import be.vbgn.gradle.pluginupdates.version.Version;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.gradle.api.artifacts.DependencyArtifact;
import org.gradle.api.artifacts.ModuleIdentifier;

public interface Dependency extends ModuleIdentifier {
    String DEFAULT_TYPE = DependencyArtifact.DEFAULT_TYPE;

    @Override
    @Nonnull
    String getGroup();

    @Override
    @Nonnull
    String getName();

    @Nonnull
    Version getVersion();

    @Nonnull
    String getClassifier();

    @Nonnull
    String getType();

    @Nonnull
    Dependency withGroup(@Nonnull String group);

    @Nonnull
    Dependency withName(@Nonnull String name);

    @Nonnull
    default Dependency withVersion(String version) {
        return new DefaultDependency(getGroup(), getName(), Version.parse(version), getClassifier(), getType());
    }

    @Nonnull
    Dependency withVersion(@Nonnull Version version);

    @Nonnull
    Dependency withClassifier(@Nullable String classifier);

    @Nonnull
    Dependency withType(@Nullable String type);


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
