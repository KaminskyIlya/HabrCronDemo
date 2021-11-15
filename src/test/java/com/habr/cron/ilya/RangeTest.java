package com.habr.cron.ilya;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.annotations.TestInstance;

import static com.habr.cron.ilya.LastDayOfMonthMatcher.LAST_DAY_OF_MONTH_CODE;
import static org.testng.Assert.*;

public class RangeTest {

    @Test
    public void testIsAsterisk() throws Exception
    {
        assertEquals(true, Range.ASTERISK.isAsterisk());

        Range range = new Range();
        assertEquals(true, range.isAsterisk());
    }

    @Test
    public void testIsStepped() throws Exception
    {
        Range range = new Range(2, false);
        assertEquals(true, range.isStepped());

        range = new Range(1, 10, 2);
        assertEquals(true, range.isStepped());

        range = new Range(1, 10, 1);
        assertEquals(false, range.isStepped());
    }

    @Test
    public void testGetValue() throws Exception
    {
        Range range = new Range(50);
        assertEquals(50, range.getValue());
    }

    @Test(expectedExceptions = AssertionError.class)
    public void getValueMustException() throws Exception
    {
        Range range = new Range(1, 50, 10);
        assertEquals(50, range.getValue());
    }

    @Test
    public void testIsConstant() throws Exception
    {
        Range range = new Range(2, 2, 1);
        assertEquals(true, range.isConstant());

        range = new Range(2, 2, 10);
        assertEquals(true, range.isConstant());

        range = new Range(2, 5, 1);
        assertEquals(false, range.isConstant());

        range = new Range(2, 5, 2);
        assertEquals(false, range.isConstant());
    }

    @Test
    public void testIsLastDay() throws Exception
    {
        Range range = new Range(LAST_DAY_OF_MONTH_CODE);
        assertEquals(true, range.isLastDay());
    }

    @Test
    public void testIsLeapDay() throws Exception
    {
        Range range = new Range(LastDayOfMonthMatcher.FEBRUARY_LEAP_DAY);
        assertEquals(true, range.isLeapDay());
    }

    @Test
    public void testIsFebruary() throws Exception
    {
        Range range = new Range(2);
        assertEquals(true, range.isFebruary());
    }

    @Test
    public void testIsByLastDay() throws Exception
    {
        Range range = new Range(1, LAST_DAY_OF_MONTH_CODE, 3);
        assertEquals(true, range.isByLastDay());
    }

    @Test
    public void testShiftBy() throws Exception
    {
        Range range = new Range(1, 2, 3);
        range.shiftBy(2);
        assertEquals(3, range.min);
        assertEquals(4, range.max);
    }

    @Test(dataProvider = "toString_dataProvider")
    public void testToString(Range range, String expected) throws Exception
    {
        assertEquals(expected, range.toString());
    }
    @DataProvider
    private Object[][] toString_dataProvider()
    {
        return new Object[][] {
                {Range.ASTERISK, "*"},
                {new Range(3, false), "*/3"},
                {new Range(3), "3"},
                {new Range(1, 3), "1-3"},
                {new Range(1, 10, 2), "1-10/2"},
        };
    }

    @Test
    public void testCompareTo() throws Exception
    {
        // The method under test now is not used anywhere

        assertEquals(new Range(1, 7).compareTo(new Range(9, 10)), -1); // full in left
        assertEquals(new Range(9, 10).compareTo(new Range(1, 7)), 1); // full in right

        assertEquals(new Range(1, 7).compareTo(new Range(7, 10)), -1); // full left
        assertEquals(new Range(7, 10).compareTo(new Range(1, 7)), 1); // full in right

        assertEquals(new Range(1, 7).compareTo(new Range(5, 10)), -1); // partial left
        assertEquals(new Range(5, 10).compareTo(new Range(1, 7)),  1); // partial right

        assertEquals(new Range(1, 7).compareTo(new Range(3, 5)), 0); // contain
        assertEquals(new Range(3, 5).compareTo(new Range(1, 7)), 0); // included

        assertEquals(new Range(1, 7).compareTo(new Range(1, 5)), 0); // contain
        assertEquals(new Range(1, 7).compareTo(new Range(5, 7)), 0); // contain

        assertEquals(new Range(1, 5).compareTo(new Range(1, 7)), 0); // included
        assertEquals(new Range(5, 7).compareTo(new Range(1, 7)), 0); // included
    }
}