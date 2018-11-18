package be.vbgn.gradle.pluginupdates.dependency;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.gradle.api.artifacts.UnresolvedDependency;

public class DefaultFailedDependency extends DefaultDependency implements FailedDependency {

    @Nullable
    private Throwable problem;

    public DefaultFailedDependency(@Nonnull String group, @Nonnull String name, @Nonnull String version,
            @Nullable Throwable problem) {
        super(group, name, version);
        this.problem = problem;
    }

    public static FailedDependency fromGradle(UnresolvedDependency unresolvedDependency) {
        return new DefaultFailedDependency(unresolvedDependency.getSelector().getGroup(),
                unresolvedDependency.getSelector().getName(),
                Objects.requireNonNull(unresolvedDependency.getSelector().getVersion()),
                unresolvedDependency.getProblem());
    }

    @Override
    @Nullable
    public Throwable getProblem() {
        return problem;
    }

    @Override
    public String toString() {
        return super.toString() + "!failed";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        DefaultFailedDependency that = (DefaultFailedDependency) o;
        return Objects.equals(getProblem(), that.getProblem());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getProblem());
    }
}
