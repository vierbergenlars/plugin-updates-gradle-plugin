package be.vbgn.gradle.pluginupdates;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.util.VersionNumber;

public class Update {

    private Project project;
    private Configuration configuration;
    private ResolvedDependency oldDependency;
    private ResolvedDependency newDependency;

    Update(Project project, Configuration configuration, ResolvedDependency oldDependency,
            ResolvedDependency newDependency) {
        this.project = project;
        this.configuration = configuration;
        this.oldDependency = oldDependency;
        this.newDependency = newDependency;
    }

    public ResolvedDependency getOldDependency() {
        return oldDependency;
    }

    public ResolvedDependency getNewDependency() {
        return newDependency;
    }

    public String getProjectName() {
        return project.getName();
    }

    public String getConfigurationName() {
        return configuration.getName();
    }

    public String getModuleName() {
        return oldDependency.getModuleName();
    }

    public String getModuleGroup() {
        return oldDependency.getModuleGroup();
    }

    public String getOldVersion() {
        return oldDependency.getModuleVersion();
    }

    public String getNewVersion() {
        if (newDependency == null) {
            return null;
        }
        return newDependency.getModuleVersion();
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
