package be.vbgn.gradle.pluginupdates.version;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;

public class VersionTest {

    @Test
    public void versionTriplet() {
        Version version = new Version(NumberWildcard.number(2), NumberWildcard.number(1), NumberWildcard.number(0),
                NumberWildcard.empty(), "");

        assertEquals("2.1.0", version.toString());

        Version version2 = version.withMajor(NumberWildcard.number(1));
        assertEquals("1.1.0", version2.toString());

        assertEquals("2.1.0-SNAPSHOT", version.withQualifier("SNAPSHOT").toString());
    }

    @Test
    public void versionQuadruplet() {
        Version version = new Version(NumberWildcard.number(2), NumberWildcard.number(1), NumberWildcard.number(0),
                NumberWildcard.number(8), "");
        assertEquals("2.1.0.8", version.toString());

        Version version1 = version.withPatch(NumberWildcard.empty());
        assertEquals("2.1.0", version1.toString());
        assertEquals("2.1.0.8-SNAPSHOT", version.withQualifier("SNAPSHOT").toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalVersion1() {
        Version version = new Version(NumberWildcard.empty(), NumberWildcard.number(1), NumberWildcard.empty(),
                NumberWildcard.empty(), "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalVersion2() {
        Version version = new Version(NumberWildcard.wildcard(), NumberWildcard.number(1), NumberWildcard.empty(),
                NumberWildcard.empty(), "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalVersion3() {
        Version version = new Version(NumberWildcard.number(2).withWildcard(), NumberWildcard.number(1),
                NumberWildcard.empty(),
                NumberWildcard.empty(), "");
    }

    @Test
    public void withMajor() {
        Version version = new Version(NumberWildcard.number(2), NumberWildcard.number(1), NumberWildcard.number(0),
                NumberWildcard.empty(), "SNAPSHOT");

        assertEquals("2.1.0-SNAPSHOT", version.toString());
        assertEquals("3.1.0-SNAPSHOT", version.withMajor(NumberWildcard.number(3)).toString());
        assertEquals("4+", version.withMajor(NumberWildcard.number(4).withWildcard()).toString());
        assertEquals("+", version.withMajor(NumberWildcard.wildcard()).toString());
    }

    @Test
    public void withMinor() {
        Version version = new Version(NumberWildcard.number(2), NumberWildcard.number(1), NumberWildcard.number(0),
                NumberWildcard.empty(), "SNAPSHOT");

        assertEquals("2.1.0-SNAPSHOT", version.toString());
        assertEquals("2.3.0-SNAPSHOT", version.withMinor(NumberWildcard.number(3)).toString());
        assertEquals("2.4+", version.withMinor(NumberWildcard.number(4).withWildcard()).toString());
        assertEquals("2.+", version.withMinor(NumberWildcard.wildcard()).toString());
    }

    @Test
    public void withMicro() {
        Version version = new Version(NumberWildcard.number(2), NumberWildcard.number(1), NumberWildcard.number(0),
                NumberWildcard.empty(), "SNAPSHOT");

        assertEquals("2.1.0-SNAPSHOT", version.toString());
        assertEquals("2.1.3-SNAPSHOT", version.withMicro(NumberWildcard.number(3)).toString());
        assertEquals("2.1.4+", version.withMicro(NumberWildcard.number(4).withWildcard()).toString());
        assertEquals("2.1.+", version.withMicro(NumberWildcard.wildcard()).toString());
    }

    @Test
    public void withPatch() {
        Version version = new Version(NumberWildcard.number(2), NumberWildcard.number(1), NumberWildcard.number(0),
                NumberWildcard.empty(), "SNAPSHOT");

        assertEquals("2.1.0-SNAPSHOT", version.toString());
        assertEquals("2.1.0.3-SNAPSHOT", version.withPatch(NumberWildcard.number(3)).toString());
        assertEquals("2.1.0.4+", version.withPatch(NumberWildcard.number(4).withWildcard()).toString());
        assertEquals("2.1.0.+", version.withPatch(NumberWildcard.wildcard()).toString());
    }

    @Test
    public void withQualifier() {
        Version version = new Version(NumberWildcard.number(2), NumberWildcard.number(1), NumberWildcard.number(0),
                NumberWildcard.empty(), "SNAPSHOT");

        assertEquals("2.1.0-SNAPSHOT", version.toString());
        assertEquals("2.1.0", version.withQualifier("").toString());
        assertEquals("2.1.0-bla", version.withQualifier("bla").toString());
    }

    @Test
    public void parseNumber() {
        assertEquals("1.2.3", Version.parse("1.2.3").toString());
        assertEquals("1.2.3.4", Version.parse("1.2.3.4").toString());
        assertEquals("1.2.3.4+", Version.parse("1.2.3.4+").toString());
        assertEquals("1.2.3.4", Version.parse("1.2.3_4").toString());
        assertEquals("1.2.3.4-SNAPSHOT", Version.parse("1.2.3_4-SNAPSHOT").toString());
        assertEquals("1.2.3.+", Version.parse("1.2.3_+").toString());
        assertEquals("1.2.+", Version.parse("1.2.+").toString());
        assertEquals("1.2+", Version.parse("1.2+").toString());
        assertEquals("1.2", Version.parse("1.2").toString());
        assertEquals("1.+", Version.parse("1.+").toString());
        assertEquals("1+", Version.parse("1+").toString());
        assertEquals("1", Version.parse("1").toString());
    }

    @Test
    public void matches() {
        assertTrue(Version.parse("+").matches(Version.parse("1.2.3")));
        assertTrue(Version.parse("1+").matches(Version.parse("1.2.3")));
        assertFalse(Version.parse("1").matches(Version.parse("1.2.3")));
        assertTrue(Version.parse("1+").matches(Version.parse("2.2.3")));
        assertTrue(Version.parse("1.+").matches(Version.parse("1.2.3")));
        assertFalse(Version.parse("1.+").matches(Version.parse("2.2.3")));
        assertFalse(Version.parse("2+").matches(Version.parse("1.2.3")));
        assertFalse(Version.parse("1.2").matches(Version.parse("1.2.3")));
        assertTrue(Version.parse("1.2+").matches(Version.parse("1.2.3")));
        assertTrue(Version.parse("1.1+").matches(Version.parse("1.2.3")));
        assertFalse(Version.parse("1.3+").matches(Version.parse("1.2.3")));
        assertTrue(Version.parse("1.2.+").matches(Version.parse("1.2.3")));
        assertFalse(Version.parse("1.1.+").matches(Version.parse("1.2.3")));
        assertTrue(Version.parse("1.2.1+").matches(Version.parse("1.2.3")));
        assertTrue(Version.parse("1.2.3+").matches(Version.parse("1.2.3")));
        assertTrue(Version.parse("1.2.3").matches(Version.parse("1.2.3")));
        assertFalse(Version.parse("1.2.1").matches(Version.parse("1.2.3")));
        assertTrue(Version.parse("1.2.3+").matches(Version.parse("1.2.3")));
        assertFalse(Version.parse("1.2.3.+").matches(Version.parse("1.2.3")));
    }

    @Test
    public void compareTo() {
        List<Version> toSort = new LinkedList<>();
        toSort.add(Version.parse("1.2.3"));
        toSort.add(Version.parse("1.2+"));
        toSort.add(Version.parse("1.2.3-SNAPSHOT"));
        toSort.add(Version.parse("1.+"));
        toSort.add(Version.parse("2.1.2"));
        toSort.add(Version.parse("1.2.+"));
        toSort.add(Version.parse("1.2.3"));
        toSort.add(Version.parse("1.2.3+"));
        toSort.add(Version.parse("1+"));
        toSort.add(Version.parse("2.1.2"));
        toSort.add(Version.parse("1.2.3.4"));
        toSort.add(Version.parse("1.+"));
        toSort.add(Version.parse("1.2.+"));
        toSort.add(Version.parse("1.2.3.4"));
        toSort.add(Version.parse("1.2.3-SNAPSHOT"));
        toSort.add(Version.parse("1.2.3+"));
        toSort.add(Version.parse("1.2"));
        toSort.add(Version.parse("1.2.4"));

        Collections.sort(toSort);

        List<Version> sortedCollection = new LinkedList<>();
        sortedCollection.add(Version.parse("1.2"));
        sortedCollection.add(Version.parse("1.2.3-SNAPSHOT"));
        sortedCollection.add(Version.parse("1.2.3-SNAPSHOT"));
        sortedCollection.add(Version.parse("1.2.3"));
        sortedCollection.add(Version.parse("1.2.3"));
        sortedCollection.add(Version.parse("1.2.3.4"));
        sortedCollection.add(Version.parse("1.2.3.4"));
        sortedCollection.add(Version.parse("1.2.4"));
        sortedCollection.add(Version.parse("1.2.+"));
        sortedCollection.add(Version.parse("1.2.+"));
        sortedCollection.add(Version.parse("1.2.3+"));
        sortedCollection.add(Version.parse("1.2.3+"));
        sortedCollection.add(Version.parse("1.+"));
        sortedCollection.add(Version.parse("1.+"));
        sortedCollection.add(Version.parse("1.2+"));
        sortedCollection.add(Version.parse("2.1.2"));
        sortedCollection.add(Version.parse("2.1.2"));
        sortedCollection.add(Version.parse("1+"));

        assertArrayEquals(toSort.toArray(), sortedCollection.toArray());
    }

}
