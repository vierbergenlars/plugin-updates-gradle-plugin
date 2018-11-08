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


    @Test
    public void wildcardNumber() {
        assertEquals("3+", NumberWildcard.number(3).withWildcard().toString());
        assertEquals("3+", NumberWildcard.wildcard().withNumberComponent(3).toString());

        assertTrue(NumberWildcard.number(2).withWildcard().hasNumberComponent());
        assertTrue(NumberWildcard.number(2).withWildcard().hasWildcard());
        assertFalse(NumberWildcard.number(2).withWildcard().isEmpty());
    }

    @Test
    public void contains() {
        /*
         * x.contains(y)
         *  x   /  y | number | wildcard | none | both
         *  number   |        |          |      |
         *  wildcard |        |          |      |
         *  none     |        |          |      |
         *  both     |        |          |      |
         */

        // number.contains(number)
        assertTrue(NumberWildcard.number(1).contains(NumberWildcard.number(1))); // 1 contains 1
        assertFalse(NumberWildcard.number(2).contains(NumberWildcard.number(1))); // 2 does not contain 1
        // number.contains(wildcard)
        assertFalse(NumberWildcard.number(1).contains(NumberWildcard.wildcard())); // 1 does not contain +
        // number.contains(none)
        assertFalse(NumberWildcard.number(1).contains(NumberWildcard.empty())); // 1 does not contain empty
        // number.contains(both)
        assertFalse(
                NumberWildcard.number(1).contains(NumberWildcard.number(1).withWildcard())); // 1 does not contain 1+
        assertFalse(
                NumberWildcard.number(2).contains(NumberWildcard.number(1).withWildcard())); // 2 does not contain 1+

        // wildcard.contains(number)
        assertTrue(NumberWildcard.wildcard().contains(NumberWildcard.number(1))); // + contains 1
        // wildcard.contains(wildcard)
        assertTrue(NumberWildcard.wildcard().contains(NumberWildcard.wildcard())); // + contains +
        // wildcard.contains(none)
        assertFalse(NumberWildcard.wildcard().contains(NumberWildcard.empty())); // + does not contain empty
        // wildcard.contains(both)
        assertTrue(NumberWildcard.wildcard().contains(NumberWildcard.number(1).withWildcard())); // + contains 1+

        // none.contains(number)
        assertFalse(NumberWildcard.empty().contains(NumberWildcard.number(1))); // empty does not contain 1
        // none.contains(wildcard)
        assertFalse(NumberWildcard.empty().contains(NumberWildcard.wildcard())); // empty does not contain +
        // none.contains(none)
        assertTrue(NumberWildcard.empty().contains(NumberWildcard.empty())); // empty contains empty
        // none.contains(both)
        assertFalse(NumberWildcard.empty()
                .contains(NumberWildcard.wildcard().withNumberComponent(1))); // empty does not contain 1+

        // both.contains(number)
        assertTrue(NumberWildcard.number(1).withWildcard().contains(NumberWildcard.number(1))); // 1+ contains 1
        assertTrue(NumberWildcard.number(1).withWildcard().contains(NumberWildcard.number(2))); // 1+ contains 2
        assertFalse(
                NumberWildcard.number(2).withWildcard().contains(NumberWildcard.number(1))); // 2+ does not contain 1
        // both.contains(wildcard)
        assertFalse(
                NumberWildcard.number(1).withWildcard().contains(NumberWildcard.wildcard())); // 1+ does not contain +
        // both.contains(none)
        assertFalse(NumberWildcard.wildcard().withNumberComponent(1)
                .contains((NumberWildcard.empty()))); // 1+ does not contain empty
        // both.contains(both)
        assertTrue(NumberWildcard.wildcard().withNumberComponent(1)
                .contains(NumberWildcard.wildcard().withNumberComponent(1))); // 1+ contains 1+
        assertFalse(NumberWildcard.wildcard().withNumberComponent(2)
                .contains(NumberWildcard.wildcard().withNumberComponent(1))); // 2+ does not contain 1+
        assertTrue(NumberWildcard.wildcard().withNumberComponent(1)
                .contains(NumberWildcard.wildcard().withNumberComponent(2))); // 1+ contains 2+

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
