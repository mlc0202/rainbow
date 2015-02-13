package com.icitic.core.util;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.icitic.core.util.date.DateRange;
import com.icitic.core.util.date.DateType;
import com.icitic.core.util.date.IntDate;
import com.icitic.core.util.date.LevelDate;

public class TestIntDate {

	@Test
	public void test() {
		IntDate id = new IntDate(20110331);
		assertEquals(2011, id.getYear());
		assertEquals(3, id.getMonth());
		assertEquals(31, id.getDay());
		assertEquals(20110331, id.getValue());
		assertEquals(4, id.getWeekDay());
		assertEquals(3, id.getXun());
		assertEquals(1, id.getSeason());
		assertEquals(1, id.getHalfYear());
	}

	@Test
	public void testAdjust() {
		IntDate d = new IntDate(2011, 7, 1);
		d.adjust(DateType.旬, 1);
		assertEquals(20110711, d.getValue());
		d.adjust(DateType.旬, 1);
		assertEquals(20110721, d.getValue());
		d.adjust(DateType.旬, 1);
		assertEquals(20110801, d.getValue());
		d.adjust(DateType.旬, -3);
		assertEquals(20110701, d.getValue());

		d.adjust(DateType.旬, 2);
		assertEquals(20110721, d.getValue());
		d.adjust(DateType.旬, 2);
		assertEquals(20110811, d.getValue());
		d.adjust(DateType.旬, -3);
		assertEquals(20110711, d.getValue());
		d.adjust(DateType.旬, -1);
		assertEquals(20110701, d.getValue());
		
		d.adjust(DateType.旬, 3);
		assertEquals(20110801, d.getValue());
		d.adjust(DateType.旬, 4);
		assertEquals(20110911, d.getValue());
		d.adjust(DateType.旬, -7);
		assertEquals(20110701, d.getValue());
	}
	
	@Test
	public void testRange() {
		LevelDate ld = new LevelDate(2011);
		DateRange r = ld.getRange(DateType.半年);
		assertEquals("2011年上半年-2011年下半年", r.toString());
		r = ld.getRange(DateType.季);
		assertEquals("2011年一季度-2011年四季度", r.toString());
		r = ld.getRange(DateType.月);
		assertEquals("2011年1月-2011年12月", r.toString());
		r = ld.getRange(DateType.旬);
		assertEquals("2011年1月上旬-2011年12月下旬", r.toString());
		r = ld.getRange(DateType.日);
		assertEquals("2011年1月1日-2011年12月31日", r.toString());
	}
	
	
	@Test
	public void testRangeGetAll() {
		LevelDate ld = new LevelDate(2011);
		DateRange r = ld.getRange(DateType.半年);
		List<LevelDate> list = r.getAll();
		assertEquals(2, list.size());
		
		ld = new LevelDate(201103);
		r = ld.getRange(DateType.日);
		list = r.getAll();
		assertEquals(31, list.size());
	}
	
	
}
