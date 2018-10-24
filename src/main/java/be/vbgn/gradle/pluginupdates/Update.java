package be.vbgn.gradle.pluginupdates;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.util.VersionNumber;

public class Update {

    private Project project;
    private Configuration configuration;
    private Dependency oldDependency;
    private Dependency newDependency;

    Update(Project project, Configuration configuration, Dependency oldDependency,
            Dependency newDependency) {
        this.project = project;
        this.configuration = configuration;
        this.oldDependency = oldDependency;
        this.newDependency = newDependency;
    }

    public Dependency getOldDependency() {
        return oldDependency;
    }

    public Dependency getNewDependency() {
        return newDependency;
    }

    public String getProjectName() {
        return project.getName();
    }

    public String getConfigurationName() {
        return configuration.getName();
    }

    public String getModuleName() {
        return oldDependency.getName();
    }

    public String getModuleGroup() {
        return oldDependency.getGroup();
    }

    public String getModuleClassifier() {
        return oldDependency.getClassifier();
    }

    public String getOldVersion() {
        return oldDependency.getVersion();
    }

    public String getNewVersion() {
        if (getNewDependency() == null) {
            return null;
        }
        return getNewDependency().getVersion();
    }

    public boolean isOutdated() {
        if (getNewVersion() == null) {
            return false;
        }
        VersionNumber oldVersion = VersionNumber.parse(getOldVersion());
        VersionNumber newVersion = VersionNumber.parse(getNewVersion());
        return newVersion.compareTo(oldVersion) > 0;
    }

    public String getMessage() {

        String message = "Plugin is outdated in project " + getProjectName() + ": ";
        message += getPartialMessage(getOldDependency().getGroup(), getNewDependency().getGroup()) + ":";
        message += getPartialMessage(getOldDependency().getName(), getNewDependency().getName()) + ":";
        message += getPartialMessage(getOldDependency().getVersion(), getNewDependency().getVersion());

        if (!getOldDependency().getClassifier().isEmpty() || !getNewDependency().getClassifier().isEmpty()) {
            message += ":" + getPartialMessage(getOldDependency().getClassifier(), getNewDependency().getClassifier());
        }

        if (!getOldDependency().getType().equals(Dependency.DEFAULT_TYPE) || !getNewDependency().getType()
                .equals(Dependency.DEFAULT_TYPE)) {
            message += "@" + getPartialMessage(getOldDependency().getType(), getNewDependency().getType());
        }

        return message;
    }

    private String getPartialMessage(String part1, String part2) {
        if (part1.equals(part2)) {
            return part1;
        }
        return "[" + part1 + " -> " + part2 + "]";
    }

    @Override
    public String toString() {
        return "Update{" +
                "project=" + project +
                ", configuration=" + configuration +
                ", oldDependency=" + oldDependency +
                ", newDependency=" + newDependency +
                '}';
    }
}
