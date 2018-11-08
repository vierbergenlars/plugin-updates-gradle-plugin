package be.vbgn.gradle.pluginupdates.update.finder;

import be.vbgn.gradle.pluginupdates.version.Version;
import java.util.Objects;

public class FailureAllowedVersion {

    private Version version;
    private boolean failureAllowed;

    public FailureAllowedVersion(Version version, boolean failureAllowed) {
        this.version = version;
        this.failureAllowed = failureAllowed;
    }

    public Version getVersion() {
        return version;
    }

    public boolean isFailureAllowed() {
        return failureAllowed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FailureAllowedVersion that = (FailureAllowedVersion) o;
        return isFailureAllowed() == that.isFailureAllowed() &&
                Objects.equals(getVersion(), that.getVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVersion(), isFailureAllowed());
    }
}
