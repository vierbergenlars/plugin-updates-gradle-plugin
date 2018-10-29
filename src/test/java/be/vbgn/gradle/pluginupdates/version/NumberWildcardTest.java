package be.vbgn.gradle.pluginupdates.version;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;

public class NumberWildcardTest {

    @Test
    public void number() {
        assertEquals("5", NumberWildcard.number(5).toString());
        assertEquals("5", NumberWildcard.empty().withNumberComponent(5).toString());

        assertTrue(NumberWildcard.number(2).hasNumberComponent());
        assertFalse(NumberWildcard.number(2).hasWildcard());
        assertFalse(NumberWildcard.number(2).isEmpty());
    }

    @Test
    public void wildcard() {
        assertEquals("+", NumberWildcard.wildcard().toString());
        assertEquals("+", NumberWildcard.empty().withWildcard().toString());

        assertTrue(NumberWildcard.wildcard().hasWildcard());
        assertFalse(NumberWildcard.wildcard().hasNumberComponent());
        assertFalse(NumberWildcard.wildcard().isEmpty());
    }

    @Test
    public void empty() {
        assertEquals("", NumberWildcard.empty().toString());
        assertEquals("", NumberWildcard.wildcard().withoutWildcard().toString());
        assertEquals("", NumberWildcard.number(5).withoutNumberComponent().toString());

        assertTrue(NumberWildcard.empty().isEmpty());
        assertFalse(NumberWildcard.empty().hasNumberComponent());
        assertFalse(NumberWildcard.empty().hasWildcard());
    }

    public void wildcardNumber() {
        assertEquals("3+", NumberWildcard.number(3).withWildcard().toString());
        assertEquals("3+", NumberWildcard.wildcard().withNumberComponent(3).toString());

        assertTrue(NumberWildcard.number(2).withWildcard().hasNumberComponent());
        assertTrue(NumberWildcard.number(2).withWildcard().hasWildcard());
        assertFalse(NumberWildcard.number(2).withWildcard().isEmpty());
    }

    @Test
    public void compareTo() {
        List<NumberWildcard> toSort = new LinkedList<>();
        toSort.add(NumberWildcard.empty());
        toSort.add(NumberWildcard.wildcard());
        toSort.add(NumberWildcard.number(3));
        toSort.add(NumberWildcard.number(2).withWildcard());
        toSort.add(NumberWildcard.number(3));
        toSort.add(NumberWildcard.number(3).withWildcard());
        toSort.add(NumberWildcard.empty());
        toSort.add(NumberWildcard.number(2));
        toSort.add(NumberWildcard.number(2).withWildcard());

        Collections.sort(toSort);

        List<NumberWildcard> sortedList = new LinkedList<>();
        sortedList.add(NumberWildcard.empty());
        sortedList.add(NumberWildcard.empty());
        sortedList.add(NumberWildcard.number(2));
        sortedList.add(NumberWildcard.number(3));
        sortedList.add(NumberWildcard.number(3));
        sortedList.add(NumberWildcard.wildcard());
        sortedList.add(NumberWildcard.number(2).withWildcard());
        sortedList.add(NumberWildcard.number(2).withWildcard());
        sortedList.add(NumberWildcard.number(3).withWildcard());

        assertArrayEquals(toSort.toArray(), sortedList.toArray());

    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeNumber() {
        NumberWildcard.number(-3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeNumberWith() {
        NumberWildcard.empty().withNumberComponent(-1);
    }

    @Test(expected = NumberWildcard.NoNumberComponentException.class)
    public void getNumberComponentWithoutNumber() {
        NumberWildcard.empty().getNumberComponent();
    }

}
