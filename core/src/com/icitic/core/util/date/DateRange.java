package com.icitic.core.util.date;

import static com.google.common.base.Preconditions.*;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;

public class DateRange {

	private IntDate begin;

	private IntDate end;

	private DateType type;

	public LevelDate getBegin() {
		return new LevelDate(type, begin);
	}

	public LevelDate getEnd() {
		return new LevelDate(type, end);
	}

	public IntDate getBeginDate(){
		return begin;
	}
	
	public IntDate getEndDate() {
		return end;
	}
	
	public DateType getType() {
		return type;
	}

	public DateRange(DateType type, IntDate begin, IntDate end) {
		this.type = type;
		this.begin = begin;
		this.end = end;
	}

	public DateRange(String begin, String end) {
		LevelDate d1 = new LevelDate(begin);
		LevelDate d2 = new LevelDate(end);
		checkArgument(d1.getDateType() == d2.getDateType());
		checkArgument(d1.compareTo(d2) < 0);
		this.type = d1.getDateType();
		this.begin = d1.intDate;
		this.end = d2.intDate;
	}

	@Override
	public String toString() {
		return new StringBuilder().append(begin.toDateString(type)).append("-").append(end.toDateString(type))
				.toString();
	}

	public List<LevelDate> getAll() {
		ImmutableList.Builder<LevelDate> builder = ImmutableList.builder();
		IntDate cur = begin.clone();
		while (cur.getValue() <= end.getValue()) {
			builder.add(new LevelDate(type, cur));
			cur = cur.clone().adjust(type, 1);
		}
		return builder.build();
	}

	public List<String> getValues() {
		ImmutableList.Builder<String> builder = ImmutableList.builder();
		IntDate cur = begin.clone();
		while (cur.getValue() <= end.getValue()) {
			builder.add(cur.toString());
			cur.adjust(type, 1);
		}
		return builder.build();
	}

	public Iterator<LevelDate> iterator() {
		return new AbstractIterator<LevelDate>() {
			private LevelDate cur;

			@Override
			protected LevelDate computeNext() {
				if (cur == null)
					cur = new LevelDate(type, begin);
				else
					cur.nextPeriod();
				if (cur.intDate.compareTo(end) > 0)
					endOfData();
				return cur;
			}

		};
	}
}
