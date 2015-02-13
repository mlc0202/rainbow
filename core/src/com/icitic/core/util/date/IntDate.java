package com.icitic.core.util.date;

import static com.google.common.base.Preconditions.checkArgument;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.icitic.core.util.Utils;

/**
 * 用整数表示的日期
 * 
 * 日(8位)： yyyymmdd
 * 
 * @author lijinghui
 * 
 */
public class IntDate implements Cloneable, Comparable<IntDate> {

	private static final int[] lastDay = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

	public static int getMonthLastDay(int year, int month) {
		int value = lastDay[month - 1];
		if (month == 2 && isLeapYear(year))
			return 29;
		return value;
	}

	public static boolean isLeapYear(int year) {
		if (year % 4 == 0) {
			if (year % 100 == 0)
				return year % 400 == 0;
			return true;
		}
		return false;
	}

	public static IntDate today() {
		return new IntDate(Calendar.getInstance());
	}

	private int year;

	private int month;

	private int day;

	public IntDate(int year, int month, int day) {
		checkArgument(year >= 1970 && year <= 2970, "invalid year [%s]", year);
		checkArgument(month >= 1 && month <= 12, "invalid month [%s]", month);
		checkArgument(day >= 1 && day <= getMonthLastDay(year, month), "invlid day [%s-%s-%s]", year, month, day);
		this.year = year;
		this.month = month;
		this.day = day;
	}

	public IntDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		year = calendar.get(Calendar.YEAR);
		month = calendar.get(Calendar.MONTH) + 1;
		day = calendar.get(Calendar.DAY_OF_MONTH);
	}

	public IntDate(Calendar calendar) {
		year = calendar.get(Calendar.YEAR);
		month = calendar.get(Calendar.MONTH) + 1;
		day = calendar.get(Calendar.DAY_OF_MONTH);
	}

	public IntDate(int value) {
		this(value / 10000, value % 10000 / 100, value % 100);
	}

	public IntDate(String str) {
		this(Integer.parseInt(str));
	}

	/**
	 * 按日期类型做标准化
	 * 
	 * @param type
	 * @return
	 */
	public IntDate normanlize(DateType type) {
		switch (type) {
		case 年:
			month = 1;
			day = 1;
			break;
		case 半年:
			month = getHalfYear() * 6 - 5;
			day = 1;
			break;
		case 季:
			month = getSeason() * 3 - 2;
			day = 1;
			break;
		case 月:
			day = 1;
			break;
		case 旬:
			day = getXun() * 10 - 9;
		default:
			break;
		}
		return this;
	}

	private Calendar getCalendar() {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(year, month - 1, day);
		return calendar;
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public int getDay() {
		return day;
	}

	public int getWeekDay() {
		int weekDay = getCalendar().get(Calendar.DAY_OF_WEEK);
		if (weekDay == Calendar.SUNDAY)
			weekDay = 7;
		else
			weekDay--;
		return weekDay;
	}

	public int getXun() {
		int xun = (day + 9) / 10;
		if (xun == 4)
			xun = 3;
		return xun;
	}

	public int getSeason() {
		return (month + 2) / 3;
	}

	public int getHalfYear() {
		return month < 7 ? 1 : 2;
	}

	public Date toDate() {
		return getCalendar().getTime();
	}

	/**
	 * 调整日期
	 * 
	 * @param type
	 *            日期类型
	 * @param amount
	 *            调整量，可以是负数
	 */
	public IntDate adjust(DateType type, int amount) {
		switch (type) {
		case 年:
			year += amount;
			return this;
		case 半年:
			amount = amount * 2;
		case 季:
			amount = amount * 3;
		case 月:
			year += amount / 12;
			month += amount % 12;
			break;
		case 旬:
			year += amount / 36;
			amount = amount % 36;
			month += amount / 3;
			amount = amount % 3;
			if (amount != 0) {
				int xun = getXun() + amount;
				if (xun > 3) {
					month++;
					xun -= 3;
				} else if (xun < 1) {
					month--;
					xun += 3;
				}
				day = xun * 10 - 10 + day % 10;
			}
			break;
		default:
			if (amount == 1) {
				if (day == getMonthLastDay(year, month)) {
					day = 1;
					month++;
				} else
					day++;
			} else if (amount == -1) {
				if (day == 1) {
					if (month == 1) {
						month = 12;
						year--;
					} else
						month--;
					day = getMonthLastDay(year, month);
				} else
					day--;
			} else {
				Calendar calendar = getCalendar();
				calendar.add(Calendar.DAY_OF_YEAR, amount);
				year = calendar.get(Calendar.YEAR);
				month = calendar.get(Calendar.MONTH) + 1;
				day = calendar.get(Calendar.DAY_OF_MONTH);
				return this;
			}
		}
		if (month > 12) {
			year++;
			month -= 12;
		} else if (month <= 0) {
			year--;
			month += 12;
		}
		int lastDay = getMonthLastDay(year, month);
		if (day > lastDay)
			day = lastDay;
		return this;
	}

	/**
	 * 返回不同级别的日期数值
	 * 
	 * @param type
	 * @return
	 */
	public int getValue(DateType type) {
		switch (type) {
		case 年:
			return getYear();
		case 半年:
			return getYear() * 1000 + getHalfYear();
		case 季:
			return getYear() * 10 + getSeason();
		case 月:
			return getYear() * 100 + getMonth();
		case 旬:
			return getYear() * 1000 + getMonth() * 10 + getXun();
		default:
			return getValue();
		}
	}

	public int getValue() {
		return year * 10000 + month * 100 + day;
	}

	/**
	 * 显示用的字符串
	 * 
	 * @param type
	 * @return
	 */
	public String toDateString(DateType type) {
		switch (type) {
		case 年:
			return String.format("%d年", year);
		case 半年:
			return String.format("%d年%s半年", year, getHalfYear() == 1 ? "上" : "下");
		case 季:
			return String.format("%d年%s季度", year, Utils.toStringCn(getSeason()));
		case 月:
			return String.format("%d年%d月", year, month);
		case 旬:
			String xun;
			switch (getXun()) {
			case 1:
				xun = "上";
				break;
			case 2:
				xun = "中";
				break;
			default:
				xun = "下";
			}
			return String.format("%d年%d月%s旬", year, month, xun);
		default:
			return String.format("%d年%d月%d日", year, month, day);
		}
	}

	/**
	 * 显示用的字符串
	 * 
	 * @param type
	 * @return
	 */
	public String toDateShortString(DateType type) {
		switch (type) {
		case 年:
			return String.format("%d年", year);
		case 半年:
			return String.format("%s半年", getHalfYear() == 1 ? "上" : "下");
		case 季:
			return String.format("%s季度", Utils.toStringCn(getSeason()));
		case 月:
			return String.format("%s月", Utils.toStringCn(month));
		case 旬:
			String xun;
			switch (getXun()) {
			case 1:
				xun = "上";
				break;
			case 2:
				xun = "中";
				break;
			default:
				xun = "下";
			}
			return String.format("%s旬", xun);
		default:
			return String.format("%d日", day);
		}
	}

	public String toDateString() {
		return toDateString(DateType.日);
	}

	@Override
	public String toString() {
		return Integer.toString(getValue());
	}

	public String toString(String format) {
		SimpleDateFormat s = new SimpleDateFormat(format);
		return s.format(toDate());
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
		IntDate other = (IntDate) obj;
		return (day == other.day) && (month == other.month) && (year == other.year);
	}

	@Override
	protected IntDate clone() {
		try {
			return (IntDate) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public int compareTo(IntDate o) {
		return getValue() - o.getValue();
	}

}
