package be.vbgn.gradle.pluginupdates;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a feature method or specification relates to one or more
 * issues in an external issue tracking system.
 *
 * @author Peter Niederwieser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Issue {
    /**
     * The IDs of the issues that the annotated element relates to.
     *
     * @return the IDs of the issues that the annotated element relates to
     */
    String[] value();
}

