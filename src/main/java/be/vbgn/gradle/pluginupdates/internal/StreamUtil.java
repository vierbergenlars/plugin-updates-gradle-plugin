package be.vbgn.gradle.pluginupdates.internal;

import java.util.stream.Stream;

public class StreamUtil {
    private static boolean isDebug() {
        return Boolean.getBoolean("be.vbgn.gradle.pluginupdates.debug");
    }
    public static <T> Stream<T> parallelIfNoDebug(Stream<T> stream) {
        if(isDebug()) {
            return stream.sequential();
        }
        return stream.parallel();
    }

}
