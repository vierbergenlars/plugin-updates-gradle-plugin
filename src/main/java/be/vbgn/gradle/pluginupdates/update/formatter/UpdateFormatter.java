package be.vbgn.gradle.pluginupdates.update.formatter;

import be.vbgn.gradle.pluginupdates.update.Update;
import javax.annotation.Nonnull;

/**
 * Formats an {@link Update} to a human readable format
 */
public interface UpdateFormatter {

    /**
     * @param update The update to format
     * @return human readable representation of the update
     */
    @Nonnull
    String format(@Nonnull Update update);
}
