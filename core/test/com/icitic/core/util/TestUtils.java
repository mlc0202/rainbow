package com.icitic.core.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class TestUtils {

    @Test
    public void testIsNullOrEmptyCollectionOfQ() {
        assertTrue(Utils.isNullOrEmpty((Collection<?>) null));
        assertTrue(Utils.isNullOrEmpty(new ArrayList<Object>()));
    }

    @Test
    public void testIsNullOrEmptyString() {
        assertTrue(Utils.isNullOrEmpty((String) null));
        assertTrue(Utils.isNullOrEmpty(""));
        assertFalse(Utils.isNullOrEmpty(" "));
        assertFalse(Utils.isNullOrEmpty("a"));
    }

    @Test
    public void testHasContent() {
        assertFalse(Utils.hasContent((String) null));
        assertFalse(Utils.hasContent(""));
        assertFalse(Utils.hasContent(" "));
        assertFalse(Utils.hasContent("\t"));
        assertFalse(Utils.hasContent("\t \t"));
        assertTrue(Utils.hasContent("\ta \t"));
    }

    @Test
    public void testSplit() {
        assertEquals(0, Utils.split("", ',').length);
        assertArrayEquals(new String[] { "afff" }, Utils.split("afff", '|'));
        assertArrayEquals(new String[] { "af", "ff" }, Utils.split("af|ff", '|'));
        assertArrayEquals(new String[] { "afff", "" }, Utils.split("afff|", '|'));
        assertArrayEquals(new String[] { "", "afff", "" }, Utils.split("|afff|", '|'));
        assertArrayEquals(new String[] { "", "af", "ff", "" }, Utils.split("|af|ff|", '|'));
        assertArrayEquals(new String[] { "", "af", "", "ff", "" }, Utils.split("|af||ff|", '|'));
    }

    @Test
    public void testUnionList() {
        List<Integer> first = Lists.newArrayList(1, 3, 5);
        List<Integer> second = Lists.newArrayList(2, 3, 4);
        Utils.unionList(first, second);
        Collections.sort(first);
        assertEquals(Lists.newArrayList(1, 2, 3, 4, 5), first);
    }
    
    public void testTrimString() {
    	String a = " hello\t kitty\r\n";
    	assertEquals("hellokitty", Utils.trimBlank(a));
    	assertEquals(" hello kitty", Utils.trimString(a, '\t', '\r', '\n'));
    }
}
