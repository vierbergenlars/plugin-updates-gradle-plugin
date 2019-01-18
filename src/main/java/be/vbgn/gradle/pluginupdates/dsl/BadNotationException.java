package be.vbgn.gradle.pluginupdates.dsl;

/**
 * Exception that will be thrown when the dependency notation used in the configuration is incorrect or incomplete.
 */
public class BadNotationException extends IllegalArgumentException {

    BadNotationException(String s) {
        super(s);
    }
}
