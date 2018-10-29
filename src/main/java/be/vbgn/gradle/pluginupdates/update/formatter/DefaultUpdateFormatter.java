package be.vbgn.gradle.pluginupdates.update.formatter;

import be.vbgn.gradle.pluginupdates.dependency.Dependency;
import be.vbgn.gradle.pluginupdates.dependency.FailedDependency;
import be.vbgn.gradle.pluginupdates.update.Update;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import javax.annotation.Nonnull;
import org.gradle.api.Transformer;

public class DefaultUpdateFormatter implements UpdateFormatter {

    @Nonnull
    public String format(@Nonnull Update update) {
        Dependency original = update.getOriginal();

        Dependency previous = original;
        DependencyDiff diff = DependencyDiff.NONE;
        for (Dependency dependency : update.getUpdates()) {
            if (dependency instanceof FailedDependency) {
                continue;
            }
            diff = DependencyDiff.findDiff(diff, DependencyDiff.findDiff(previous, dependency));
            previous = dependency;
        }

        return diff.format(original, update.getUpdates());

    }

    private enum DependencyDiff {
        GROUP(1, d -> "", d -> d.getGroup() + ":" + d.getName() + ":" + d.getVersion().toString()),
        NAME(2, d -> d.getGroup() + ":", d -> d.getName() + ":" + d.getVersion().toString()),
        VERSION(3, d -> d.getGroup() + ":" + d.getName() + ":", d -> d.getVersion().toString()),
        NONE(4, d -> d.getGroup() + ":" + d.getName() + ":" + d.getVersion().toString(), d -> "");

        @Nonnull
        private Transformer<String, Dependency> commonParts;
        @Nonnull
        private Transformer<String, Dependency> separateParts;
        private int weight;

        DependencyDiff(
                int weight,
                @Nonnull Transformer<String, Dependency> commonParts,
                @Nonnull Transformer<String, Dependency> separateParts) {
            this.weight = weight;
            this.commonParts = commonParts;
            this.separateParts = separateParts;
        }


        @Nonnull
        public String format(@Nonnull Dependency original, @Nonnull Collection<Dependency> updates) {
            String formatted = commonParts.transform(original);
            formatted += "[";
            formatted += updates.stream()
                    .map(separateParts::transform)
                    .reduce(separateParts.transform(original), (a, b) -> a + " -> " + b);
            formatted += "]";
            return formatted;

        }

        @Nonnull
        static private DependencyDiff findDiff(@Nonnull Dependency a, @Nonnull Dependency b) {
            DependencyDiff[] dependencyDiffs = DependencyDiff.values();
            Arrays.sort(dependencyDiffs, Comparator.comparingInt(c -> -c.weight));

            for (DependencyDiff dependencyDiff : dependencyDiffs) {
                if (dependencyDiff.commonParts.transform(a).equals(dependencyDiff.commonParts.transform(b))) {
                    return dependencyDiff;
                }
            }
            return GROUP;
        }

        @Nonnull
        static private DependencyDiff findDiff(@Nonnull DependencyDiff a, @Nonnull DependencyDiff b) {
            return a.weight < b.weight ? a : b;
        }
    }

}
