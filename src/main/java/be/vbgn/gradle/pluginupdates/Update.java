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

    public String getOldVersion() {
        return oldDependency.getVersion();
    }

    public String getNewVersion() {
        if (newDependency == null) {
            return null;
        }
        return newDependency.getVersion();
    }

    public boolean isOutdated() {
        if (getNewVersion() == null) {
            return false;
        }
        VersionNumber oldVersion = VersionNumber.parse(getOldVersion());
        VersionNumber newVersion = VersionNumber.parse(getNewVersion());
        return newVersion.compareTo(oldVersion) > 0;
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
