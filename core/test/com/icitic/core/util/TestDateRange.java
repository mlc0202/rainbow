package com.icitic.core.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.icitic.core.util.date.DateRange;
import com.icitic.core.util.date.DateType;
import com.icitic.core.util.date.IntDate;
import com.icitic.core.util.date.LevelDate;

public class TestDateRange {

	@Test
	public void test() {
		List<String> rangeStrings = ImmutableList.of("2011001", "2011002", "2012001", "2012002", "2013001", "2013002");

		DateRange range = new DateRange("2011001", "2013002");
		assertEquals(DateType.半年, range.getType());

		List<String> strings = new ArrayList<String>();
		for (Iterator<LevelDate> i = range.iterator(); i.hasNext();) {
			LevelDate ld = i.next();
			strings.add(ld.toString());
		}
		assertEquals(rangeStrings, strings);

		range = new DateRange(DateType.半年, new IntDate(20110101), new IntDate(20130602));
		strings = Utils.transform(range.getAll(), Functions.toStringFunction());
	}

}