package be.vbgn.gradle.pluginupdates.version;

import java.io.Serializable;
import javax.annotation.Nonnull;

public class Version implements Comparable<Version>, Serializable {

    @Nonnull
    private NumberWildcard major;
    @Nonnull
    private NumberWildcard minor;
    @Nonnull
    private NumberWildcard micro;
    @Nonnull
    private NumberWildcard patch;
    @Nonnull
    private String qualifier;

    public Version() {
        this(NumberWildcard.empty(), NumberWildcard.empty(), NumberWildcard.empty(), NumberWildcard.empty(), "");
    }

    public Version(@Nonnull NumberWildcard major, @Nonnull NumberWildcard minor, @Nonnull NumberWildcard micro,
            @Nonnull NumberWildcard patch, @Nonnull String qualifier) {
        this.major = major;
        this.minor = minor;
        this.micro = micro;
        this.patch = patch;
        this.qualifier = qualifier;
        validateEmptyAfterWildcard(major, minor, micro, patch);
        if ((major.hasWildcard() || minor.hasWildcard() || micro.hasWildcard() || patch.hasWildcard()) && !qualifier
                .isEmpty()) {
            throw new IllegalArgumentException("No nonempty qualifier can follow a wildcard number.");
        }
    }

    private static void validateEmptyAfterWildcard(NumberWildcard... numbers) {
        boolean foundWildcard = false;
        boolean foundEmpty = false;
        for (NumberWildcard number : numbers) {
            if (foundWildcard && !number.isEmpty()) {
                throw new IllegalArgumentException("No nonempty number can follow a wildcard number.");
            }
            if (foundEmpty && !number.isEmpty()) {
                throw new IllegalArgumentException("No nonempty number can follow an empty number.");
            }
            if (number.hasWildcard()) {
                foundWildcard = true;
            }
            if (number.isEmpty()) {
                foundEmpty = true;
            }
        }

    }

    @Nonnull
    public NumberWildcard getMajor() {
        return major;
    }

    @Nonnull
    public NumberWildcard getMinor() {
        return minor;
    }

    @Nonnull
    public NumberWildcard getMicro() {
        return micro;
    }

    @Nonnull
    public NumberWildcard getPatch() {
        return patch;
    }

    @Nonnull
    public String getQualifier() {
        return qualifier;
    }

    @Nonnull
    public Version withMajor(@Nonnull NumberWildcard major) {
        if (major.hasWildcard()) {
            return new Version(major, NumberWildcard.empty(), NumberWildcard.empty(), NumberWildcard.empty(), "");
        }
        return new Version(major, minor, micro, patch, qualifier);
    }

    @Nonnull
    public Version withMinor(@Nonnull NumberWildcard minor) {
        if (minor.hasWildcard()) {
            return new Version(major, minor, NumberWildcard.empty(), NumberWildcard.empty(), "");
        }
        return new Version(major, minor, micro, patch, qualifier);
    }

    @Nonnull
    public Version withMicro(@Nonnull NumberWildcard micro) {
        if (micro.hasWildcard()) {
            return new Version(major, minor, micro, NumberWildcard.empty(), "");
        }
        return new Version(major, minor, micro, patch, qualifier);
    }

    @Nonnull
    public Version withPatch(@Nonnull NumberWildcard patch) {
        if (patch.hasWildcard()) {
            return new Version(major, minor, micro, patch, "");
        }
        return new Version(major, minor, micro, patch, qualifier);
    }

    @Nonnull
    public Version withQualifier(@Nonnull String qualifier) {
        return new Version(major, minor, micro, patch, qualifier);
    }

    public boolean matches(@Nonnull Version other) {
        if (!getMajor().contains(other.getMajor())) {
            return false;
        } else if (getMajor().hasWildcard() && getMinor().isEmpty()) {
            return true;
        }
        if (!getMinor().contains(other.getMinor())) {
            return false;
        } else if (getMinor().hasWildcard() && getMicro().isEmpty()) {
            return true;
        }
        if (!getMicro().contains(other.getMicro())) {
            return false;
        } else if (getMicro().hasWildcard() && getPatch().isEmpty()) {
            return true;
        }
        if (!getPatch().contains(other.getPatch())) {
            return false;
        }

        return getQualifier().equals(other.getQualifier());
    }

    @Override
    public String toString() {
        String string = major.toString();
        if (!minor.isEmpty()) {
            string += "." + minor.toString();
            if (!micro.isEmpty()) {
                string += "." + micro.toString();
                if (!patch.isEmpty()) {
                    string += "." + patch.toString();
                }
            }
        }
        if (!qualifier.isEmpty()) {
            string += "-" + qualifier;
        }

        return string;
    }

    @Override
    public int compareTo(@Nonnull Version version) {
        if (!major.equals(version.major)) {
            return major.compareTo(version.major);
        }

        if (!minor.equals(version.minor)) {
            return minor.compareTo(version.minor);
        }
        if (!micro.equals(version.micro)) {
            return micro.compareTo(version.micro);
        }

        if (!patch.equals(version.patch)) {
            return patch.compareTo(version.patch);
        }

        if (qualifier.isEmpty() || version.qualifier.isEmpty()) {
            // If any qualifier is null, that version number is larger
            return (qualifier.isEmpty() ? 1 : 0) - (version.qualifier.isEmpty() ? 1 : 0);
        }

        return qualifier.compareToIgnoreCase(version.qualifier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Version version = (Version) o;

        if (!major.equals(version.major)) {
            return false;
        }
        if (!minor.equals(version.minor)) {
            return false;
        }
        if (!micro.equals(version.micro)) {
            return false;
        }
        if (!patch.equals(version.patch)) {
            return false;
        }
        return qualifier.equalsIgnoreCase(version.qualifier);
    }

    @Override
    public int hashCode() {
        int result = major.hashCode();
        result = 31 * result + minor.hashCode();
        result = 31 * result + micro.hashCode();
        result = 31 * result + patch.hashCode();
        result = 31 * result + qualifier.toLowerCase().hashCode();
        return result;
    }


    @Nonnull
    public static Version parse(@Nonnull String version) {
        Scanner scanner = new Scanner(version);

        NumberWildcard major = scanner.scanNumberWildcard();
        scanner.skipOneOf('.');
        NumberWildcard minor = scanner.scanNumberWildcard();
        scanner.skipOneOf('.');
        NumberWildcard micro = scanner.scanNumberWildcard();
        scanner.skipOneOf('.', '_');
        NumberWildcard patch = scanner.scanNumberWildcard();
        scanner.skipOneOf('.', '-');
        String qualifier = scanner.remainder();

        return new Version(major, minor, micro, patch, qualifier);

    }

    private static class Scanner {

        @Nonnull
        final private String version;
        private int ptr;

        public Scanner(@Nonnull String version) {
            this.version = version;
            ptr = 0;
        }

        private boolean isDigit() {
            if (isFinished()) {
                return false;
            }
            return Character.isDigit(version.charAt(ptr));
        }

        private int scanDigit() {
            int start = ptr;
            while (isDigit()) {
                ptr++;
            }
            return Integer.parseUnsignedInt(version.substring(start, ptr));
        }

        private boolean isWildcard() {
            if (isFinished()) {
                return false;
            }
            return version.charAt(ptr) == '+';
        }

        private boolean scanWildcard() {
            boolean isWildcard = isWildcard();
            if (isWildcard) {
                ptr++;
            }
            return isWildcard;
        }

        @Nonnull
        public NumberWildcard scanNumberWildcard() {
            NumberWildcard numberWildcard = NumberWildcard.empty();
            if (isDigit()) {
                numberWildcard = numberWildcard.withNumberComponent(scanDigit());
            }
            if (scanWildcard()) {
                numberWildcard = numberWildcard.withWildcard();
            }
            return numberWildcard;
        }

        public void skipOneOf(char... chars) {
            while (isOneOf(chars)) {
                ptr++;
            }
        }

        private boolean isOneOf(char... chars) {
            if (isFinished()) {
                return false;
            }
            char c = version.charAt(ptr);
            for (char ch : chars) {
                if (ch == c) {
                    return true;
                }
            }
            return false;
        }

        public boolean isFinished() {
            return ptr >= version.length();
        }

        @Nonnull
        public String remainder() {
            if (isFinished()) {
                return "";
            }
            return version.substring(ptr);
        }

    }
}
