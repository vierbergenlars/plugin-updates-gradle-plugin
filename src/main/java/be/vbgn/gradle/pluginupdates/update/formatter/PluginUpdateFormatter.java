package be.vbgn.gradle.pluginupdates.update.formatter;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.update.Update;
import be.vbgn.gradle.pluginupdates.version.Version;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class PluginUpdateFormatter implements UpdateFormatter {

    private UpdateFormatter fallbackFormatter;

    public PluginUpdateFormatter(UpdateFormatter fallbackFormatter) {
        this.fallbackFormatter = fallbackFormatter;
    }

    @Nonnull
    @Override
    public String format(@Nonnull Update update) {
        Dependency original = update.getOriginal();

        if(!isPluginIdentifier(original)) {
            return fallbackFormatter.format(update);
        }
        if(keepsSameCoordinates(update)) {
            String versionUpdate = update.getUpdates().stream()
                    .map(Dependency::getVersion)
                    .map(Version::toString)
                    .collect(Collectors.joining(" -> "));
            return "id '"+original.getGroup()+"' version '["+versionUpdate+"]'";
        } else if(update.getUpdates().stream().allMatch(PluginUpdateFormatter::isPluginIdentifier)){
            return Stream.concat(Stream.of(update.getOriginal()), update.getUpdates().stream())
                    .map(dependency -> "id '"+dependency.getGroup()+"' version '"+dependency.getVersion().toString()+"'")
                    .collect(Collectors.joining(" -> ", "[", "]"));
        }


        return fallbackFormatter.format(update);
    }

    private static boolean keepsSameCoordinates(Update update) {
        Dependency original = update.getOriginal();

        for (Dependency dependency : update.getUpdates()) {
            if(!dependency.getGroup().equals(original.getGroup()) || !dependency.getName().equals(original.getName())) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPluginIdentifier(Dependency dependency) {
        return dependency.getName().equals(dependency.getGroup().concat(".gradle.plugin"));
    }
}
