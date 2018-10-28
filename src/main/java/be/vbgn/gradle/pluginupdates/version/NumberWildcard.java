package be.vbgn.gradle.pluginupdates.version;

import javax.annotation.Nonnull;

public class NumberWildcard implements Comparable<NumberWildcard> {

    private final static NumberWildcard WILDCARD = new NumberWildcard(-1, true);
    private final static NumberWildcard EMPTY = new NumberWildcard(-1, false);

    public static class NoNumberComponentException extends RuntimeException {

        private NoNumberComponentException() {
            super("The number or wildcard does not have a number component.");
        }
    }

    private int number = -1;
    private boolean wildcard;

    @Nonnull
    public static NumberWildcard number(int number) {
        if (number < 0) {
            throw new IllegalArgumentException("A version number can not be negative.");
        }
        return new NumberWildcard(number, false);
    }

    @Nonnull
    public static NumberWildcard wildcard() {
        return WILDCARD;
    }

    @Nonnull
    public static NumberWildcard empty() {
        return EMPTY;
    }

    private NumberWildcard(int number, boolean wildcard) {
        this.number = number;
        this.wildcard = wildcard;
    }

    public boolean hasNumberComponent() {
        return number >= 0;
    }

    public int getNumberComponent() {
        if (!hasNumberComponent()) {
            throw new NoNumberComponentException();
        }
        return number;
    }

    @Nonnull
    public NumberWildcard withNumberComponent(int number) {
        if (number < 0) {
            throw new IllegalArgumentException("A version number can not be negative.");
        }
        return new NumberWildcard(number, wildcard);
    }

    @Nonnull
    public NumberWildcard withoutNumberComponent() {
        return new NumberWildcard(-1, wildcard);
    }

    public boolean hasWildcard() {
        return wildcard;
    }

    @Nonnull
    public NumberWildcard withWildcard() {
        return new NumberWildcard(number, true);
    }

    @Nonnull
    public NumberWildcard withoutWildcard() {
        return new NumberWildcard(number, false);
    }

    public boolean isEmpty() {
        return !hasNumberComponent() && !hasWildcard();
    }

    @Override
    public String toString() {
        return (hasNumberComponent() ? getNumberComponent() : "") + (hasWildcard() ? "+" : "");
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

