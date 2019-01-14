package be.vbgn.gradle.pluginupdates.version;

import java.io.Serializable;
import javax.annotation.Nonnull;

/**
 * Single, numeric or wildcard component of a version number
 */
public class NumberWildcard implements Comparable<NumberWildcard>, Serializable {

    private final static NumberWildcard WILDCARD = new NumberWildcard(-1, true);
    private final static NumberWildcard EMPTY = new NumberWildcard(-1, false);

    /**
     * Exception thrown when an attempt is made to retrieve the numeric component of a {@link NumberWildcard} that does not have a numeric component.
     */
    public static class NoNumberComponentException extends RuntimeException {

        private NoNumberComponentException() {
            super("The number or wildcard does not have a number component.");
        }
    }


    /**
     * Numeric part of the component
     * <p>
     * -1 indicates the absence of a numeric part.
     * Valid values are >=0
     */
    private int number = -1;

    /**
     * Wildcard part of the component
     */
    private boolean wildcard;

    /**
     * Creates a numeric variant without a wildcard
     *
     * @param number The number to use. Must be a positive number or 0.
     */
    @Nonnull
    public static NumberWildcard number(int number) {
        if (number < 0) {
            throw new IllegalArgumentException("A version number can not be negative.");
        }
        return new NumberWildcard(number, false);
    }

    /**
     * Creates a wildcard
     *
     * @implNote The wildcard instance is a singleton.
     * Because there are other ways to create a wildcard, this should not be relied on for comparison.
     */
    @Nonnull
    public static NumberWildcard wildcard() {
        return WILDCARD;
    }

    /**
     * Creates an empty
     * <p>
     * An empty {@linkplain NumberWildcard} signifies the absence of any value. It contains neither a number, nor is a wildcard.
     *
     * @implNote The empty instance is a singleton.
     * Because there are other ways to create an empty, this should not be relied on for comparison.
     */
    @Nonnull
    public static NumberWildcard empty() {
        return EMPTY;
    }

    /**
     * Internal constructor
     *
     * @param number   The number to use. -1 signifies a lack of number component.
     * @param wildcard Is a wildcard?
     */
    private NumberWildcard(int number, boolean wildcard) {
        this.number = number;
        this.wildcard = wildcard;
    }

    public boolean hasNumberComponent() {
        return number >= 0;
    }

    /**
     * @return The numeric component
     * @throws NoNumberComponentException When this instance does not have a numeric component
     * @see #hasNumberComponent()
     */
    public int getNumberComponent() {
        if (!hasNumberComponent()) {
            throw new NoNumberComponentException();
        }
        return number;
    }

    /**
     * Creates a copy with a new numeric component
     *
     * @param number
     * @throws IllegalArgumentException When the numeric component is not positive or 0
     */
    @Nonnull
    public NumberWildcard withNumberComponent(int number) {
        if (number < 0) {
            throw new IllegalArgumentException("A version number can not be negative.");
        }
        return new NumberWildcard(number, wildcard);
    }

    /**
     * Creates a copy without a numeric component
     */
    @Nonnull
    public NumberWildcard withoutNumberComponent() {
        return new NumberWildcard(-1, wildcard);
    }

    public boolean hasWildcard() {
        return wildcard;
    }

    /**
     * Creates a copy with a wildcard
     */
    @Nonnull
    public NumberWildcard withWildcard() {
        return new NumberWildcard(number, true);
    }

    /**
     * Creates a copy without a wildcard
     */
    @Nonnull
    public NumberWildcard withoutWildcard() {
        return new NumberWildcard(number, false);
    }

    /**
     * Checks this component is empty
     * <p>
     * A {@linkplain NumberWildcard} is empty when it does not contain a number component nor a wildcard.
     *
     * @see #hasNumberComponent()
     * @see #hasWildcard()
     */
    public boolean isEmpty() {
        return !hasNumberComponent() && !hasWildcard();
    }

    @Override
    public String toString() {
        return (hasNumberComponent() ? getNumberComponent() : "") + (hasWildcard() ? "+" : "");
    }

    /**
     * Checks if this contains an other {@linkplain NumberWildcard}
     * <p>
     * A {@linkplain NumberWildcard} contains an other {@link NumberWildcard} when:
     * <ol>
     * <li>They are {@link #equals(Object)}</li>
     * <li>This one is a {@link #WILDCARD} without numeric component</li>
     * <li>This one is a {@link #WILDCARD}, and its numeric component is smaller than or equal to the numeric component of the other</li>
     * </ol>
     */
    public boolean contains(@Nonnull NumberWildcard other) {
        if (this.equals(other)) {
            return true;
        }
        if (this.isEmpty() ^ other.isEmpty()) {
            return false;
        }
        if (this.hasWildcard()) {
            if (!this.hasNumberComponent()) {
                return true;
            } else if (other.hasNumberComponent()) {
                return this.getNumberComponent() <= other.getNumberComponent();
            } else {
                return false;
            }
        } else {
            if (other.hasWildcard()) {
                return false;
            }
            if (this.hasNumberComponent() && other.hasNumberComponent()) {
                return this.getNumberComponent() == other.getNumberComponent();
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NumberWildcard that = (NumberWildcard) o;

        if (number != that.number) {
            return false;
        }
        return wildcard == that.wildcard;
    }

    @Override
    public int hashCode() {
        int result = number;
        result = 31 * result + (wildcard ? 1 : 0);
        return result;
    }

    /**
     * Compares this with an other {@linkplain NumberWildcard}
     * <p>
     * If only one of the two has a wildcard, that one is considered larger.
     * Otherwise, the numeric components are compared. If the numeric component is missing on one side, that side will be considered smaller.
     *
     * @return A number smaller than 0 if this {@linkplain NumberWildcard} is smaller than the other version.
     * A number larger than 0 if this {@linkplain NumberWildcard} is larger than the other version.
     * 0 if both are equal
     */
    @Override
    public int compareTo(@Nonnull NumberWildcard other) {
        if (hasWildcard() && other.hasWildcard()) {
            return compareNumberComponents(this, other);
        } else if (!hasWildcard() && !other.hasWildcard()) {
            return compareNumberComponents(this, other);
        } else {
            // If only one of the two has a wildcard, that one is always larger
            return compareBooleans(hasWildcard(), other.hasWildcard());
        }
    }

    private static int compareNumberComponents(NumberWildcard a, NumberWildcard b) {
        if (a.hasNumberComponent() && b.hasNumberComponent()) {
            // Both have number components -> compare number components
            return a.getNumberComponent() - b.getNumberComponent();
        } else if (!a.hasNumberComponent() && !b.hasNumberComponent()) {
            // None have number components -> equal
            return 0;
        } else {
            // One has number component -> the one with number component is larger
            // Rationale: In case of wildcard: Number component sets the minimum version number that can be picked by the wildcard
            // In case of no wildcard: Empty object is always smaller than a number component
            return compareBooleans(a.hasNumberComponent(), b.hasNumberComponent());
        }

    }

    private static int compareBooleans(boolean a, boolean b) {
        return (a ? 1 : 0) - (b ? 1 : 0);
    }
}

