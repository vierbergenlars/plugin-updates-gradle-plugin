package be.vbgn.gradle.pluginupdates.dsl;

public class BadNotationException extends IllegalArgumentException {

    BadNotationException(String s) {
        throw new IllegalArgumentException(s);
    }
}
