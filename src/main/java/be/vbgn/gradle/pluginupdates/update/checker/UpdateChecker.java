package be.vbgn.gradle.pluginupdates.update.checker;

import be.vbgn.gradle.pluginupdates.update.Update;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.gradle.api.artifacts.Configuration;

public interface UpdateChecker {

    @Nonnull
    Stream<Update> getUpdates(@Nonnull Configuration configuration);

}
