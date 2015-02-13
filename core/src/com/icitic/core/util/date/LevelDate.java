package com.icitic.core.util.date;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Objects;

/**
 * 用整数表示的不同层级的日期
 * 
 * 年(4位)： yyyy
 * 
 * 半年(7位)：yyyy001 - yyyy002
 * 
 * 季度(5位)：yyyy1 - yyyy4
 * 
 * 月(6位)： yyyymm
 * 
 * 旬(7位)： yyyymms
 * 
 * 日(8位)： yyyymmdd
 * 
 * @author lijinghui
 * 
 */
public class LevelDate implements Cloneable, Comparable<LevelDate> {

	public DateType getDateType() {
		return dateType;
	}

	private DateType dateType;

	IntDate intDate;

	public int getValue() {
		return intDate.getValue(dateType);
	}

	public LevelDate(DateType dateType, IntDate intDate) {
		this.dateType = dateType;
		this.intDate = intDate.normanlize(dateType);
	}

	public LevelDate(int value) {
		init(value, Integer.toString(value));
	}

	public LevelDate(String str) {
		int value = Integer.parseInt(str);
		init(value, str);
	}

	private void init(int value, String str) {
		try {
			doInit(value, str.length());
		} catch (Throwable e) {
			throw new IllegalArgumentException("invalid period value: " + str);
		}
	}

	private void doInit(int value, int len) {
		int year, month, day;
		switch (len) {
		case 4: // 年
			dateType = DateType.年;
			year = value;
			month = 1;
			day = 1;
			break;
		case 5: // 季度
			dateType = DateType.季;
			year = value / 10;
			int season = value % 10;
			checkArgument(season >= 1 && season <= 4);
			month = season * 3 - 2;
			day = 1;
			break;
		case 6: // 月
			dateType = DateType.月;
			year = value / 100;
			month = value % 100;
			day = 1;
			checkArgument(month >= 1 && month <= 12);
			break;
		case 7: // 半年或旬
			year = value / 1000;
			int halfYear = value % 1000;
			if (halfYear == 1 || halfYear == 2) {
				dateType = DateType.半年;
				month = halfYear * 6 - 5;
				day = 1;
			} else {
				dateType = DateType.旬;
				month = halfYear / 10;
				int xun = halfYear % 10;
				checkArgument(month >= 1 && month <= 12);
				checkArgument(xun >= 1 && xun <= 3);
				day = xun * 10 - 9;
			}
			break;
		case 8: // 日
			dateType = DateType.日;
			year = value / 10000;
			month = value % 10000 / 100;
			day = value % 100;
			checkArgument(month >= 1 && month <= 12);
			checkArgument(day >= 1 && day <= IntDate.getMonthLastDay(year, month));
			break;
		default:
			throw new IllegalArgumentException();
		}
		intDate = new IntDate(year, month, day);
	}

	public LevelDate changeLevel(DateType newLevel) {
		this.dateType = newLevel;
		intDate.normanlize(dateType);
		return this;
	}

	public LevelDate adjust(int amount) {
		intDate.adjust(dateType, amount);
		return this;
	}

	/**
	 * 返回上一个周期
	 * 
	 * @param type
	 * @return
	 */
	public LevelDate prevPeriod() {
		intDate.adjust(dateType, -1);
		return this;
	}

	/**
	 * 进入下一个周期
	 * 
	 * @param type
	 * @return
	 */
	public LevelDate nextPeriod() {
		intDate.adjust(dateType, 1);
		return this;
	}

	public String toDateString() {
		return intDate.toDateString(dateType);
	}

	public String toDateShortString() {
		return intDate.toDateShortString(dateType);
	}

	@Override
	public String toString() {
		return Integer.toString(getValue());
	}

	/**
	 * 获取某个层级的指定下级层级的取值范围
	 * 
	 * @param fromLevel
	 * @param toLevel
	 * @return
	 */
	public DateRange getRange(DateType toLevel) {
		if (dateType.equals(toLevel)) 
			return new DateRange(dateType, intDate.clone(), intDate.clone());
		checkArgument(dateType.compareTo(toLevel) < 0);
		IntDate begin = intDate.clone();
		IntDate end = intDate.clone().adjust(dateType, 1).adjust(toLevel, -1);
		return new DateRange(toLevel, begin, end);
	}

	@Override
	public LevelDate clone() {
		try {
			LevelDate clone = (LevelDate) super.clone();
			clone.intDate = clone.intDate.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public int hashCode() {
		return getValue();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LevelDate other = (LevelDate) obj;
		if (dateType != other.dateType)
			return false;
		return Objects.equal(intDate, other.intDate);
	}

	@Override
	public int compareTo(LevelDate o) {
		return intDate.compareTo(o.intDate);
	}

}
