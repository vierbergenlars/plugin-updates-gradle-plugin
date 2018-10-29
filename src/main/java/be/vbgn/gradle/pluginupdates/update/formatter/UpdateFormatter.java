package be.vbgn.gradle.pluginupdates.update.formatter;

import be.vbgn.gradle.pluginupdates.update.Update;
import javax.annotation.Nonnull;

public interface UpdateFormatter {

    @Nonnull
    String format(@Nonnull Update update);
}
