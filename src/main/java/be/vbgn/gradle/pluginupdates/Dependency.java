package be.vbgn.gradle.pluginupdates;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.gradle.api.artifacts.DependencyArtifact;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;

public class Dependency {

    private String group;
    private String name;
    private String version;
    private String classifier;
    private String type;

    public static final String DEFAULT_TYPE = DependencyArtifact.DEFAULT_TYPE;

    public Dependency(String group, String name, String version, String classifier, String type) {
        this.group = group;
        this.name = name;
        this.version = version;
        this.classifier = classifier;
        this.type = type;
    }

    private Dependency(ResolvedDependency dependency, ResolvedArtifact artifact) {
        group = dependency.getModuleGroup();
        name = dependency.getModuleName();
        version = dependency.getModuleVersion();

        if (artifact != null) {
            classifier = artifact.getClassifier();
            type = artifact.getType();
        }
    }


    private Dependency(ExternalDependency dependency, DependencyArtifact artifact) {
        group = dependency.getGroup();
        name = dependency.getName();
        version = dependency.getVersion();

        if (artifact != null) {
            classifier = artifact.getClassifier();
            type = artifact.getType();
        }
    }

    public static Stream<Dependency> fromGradle(ExternalDependency dependency) {
        if (dependency.getArtifacts().isEmpty()) {
            return Stream.of(new Dependency(dependency, null));
        }
        return dependency.getArtifacts().stream()
                .map(artifact -> new Dependency(dependency, artifact));
    }

    public static Stream<Dependency> fromGradle(ResolvedDependency dependency) {
        if (dependency.getModuleArtifacts().isEmpty()) {
            return Stream.of(new Dependency(dependency, null));
        }
        return dependency.getModuleArtifacts().stream()
                .map(artifact -> new Dependency(dependency, artifact));
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getClassifier() {
        if (classifier == null) {
            return "";
        }
        return classifier;
    }

    public String getType() {
        if (type == null) {
            return DEFAULT_TYPE;
        }
        return type;
    }

    public Dependency withGroup(String group) {
        return new Dependency(group, name, version, classifier, type);
    }

    public Dependency withName(String name) {
        return new Dependency(group, name, version, classifier, type);
    }

    public Dependency withVersion(String version) {
        return new Dependency(group, name, version, classifier, type);
    }

    public Dependency withClassifier(String classifier) {
        return new Dependency(group, name, version, classifier, type);
    }

    public Dependency withType(String type) {
        return new Dependency(group, name, version, classifier, type);
    }

    public Map<String, String> toDependencyNotation() {
        Map<String, String> dependencyNotation = new HashMap<>(5, 1F);
        dependencyNotation.put("group", getGroup());
        dependencyNotation.put("name", getName());
        dependencyNotation.put("version", getVersion());
        if (!getClassifier().isEmpty()) {
            dependencyNotation.put("classifier", getClassifier());
        }
        if (!getType().equals(DEFAULT_TYPE)) {
            dependencyNotation.put("ext", getType());
        }

        return Collections.unmodifiableMap(dependencyNotation);
    }

    public String toString() {
        String dependencyNotation = getGroup() + ":" + getName() + ":" + getVersion();
        if (!getClassifier().isEmpty()) {
            dependencyNotation += ":" + getClassifier();
        }
        if (!getType().equals(DEFAULT_TYPE)) {
            dependencyNotation += "@" + getType();
        }
        return dependencyNotation;
    }
}
