package com.habr.cron.dev;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class IntervalListTest
{
    private ListOfIntervalMatcher list;

    @BeforeMethod
    public void setUp() throws Exception
    {
        // 1-10,12-18,21-30,41-50
        list = new ListOfIntervalMatcher(4);
        list.addRange(1, 10, 1);
        list.addRange(12, 18, 1);
        list.addRange(21, 30, 1);
        list.addRange(41, 50, 1);

        list.finishRange();
    }

    @Test(dataProvider = "matchDP")
    public void testMatch(int value, boolean expected) throws Exception
    {
        boolean actual = list.match(value);
        assertEquals(actual, expected);
    }
    @DataProvider
    public Object[][] matchDP()
    {
        return new Object[][]
        {
                {0, false},
                {1, true},
                {2, true},
                {10, true},
                {11, false},
                {12, true},
                {13, true},
                {18, true},
                {19, false},
                {20, false},
                {21, true},
                {51, false},
                {100, false},
        };
    }

    @Test
    public void testBounds() throws Exception
    {
        assertEquals(list.getLow(), 1);
        assertEquals(list.getHigh(), 50);
    }

    @Test(dataProvider = "nextDP")
    public void testNext(int value, int expected) throws Exception
    {
        int actual = list.getNext(value);
        assertEquals(actual, expected);
    }
    @DataProvider
    public Object[][] nextDP()
    {
        return new Object[][]{
                {-10, 1},
                {0, 1},
                {1, 2},
                {2, 3},
                {9, 10},
                {10, 12},
                {12, 13},
                {17, 18},
                {18, 21},
                {27, 28},
                {29, 30},
                {30, 41},
                {49, 50},
                {50, 51},
        };
    }


    @Test
    public void testSpecialCase() throws Exception
    {
        ListOfIntervalMatcher matcher = new ListOfIntervalMatcher(1);
        matcher.addRange(5, 5, 1);
        matcher.finishRange();

        assertTrue(matcher.match(5));
        assertFalse(matcher.match(4));
        assertFalse(matcher.match(6));

        assertEquals(matcher.getNext(3), 5);
        assertTrue(matcher.hasNext(3));

        assertEquals(matcher.getNext(4), 5);
        assertTrue(matcher.hasNext(4));

        assertEquals(matcher.getNext(5), 6);
        assertFalse(matcher.hasNext(5));

        assertEquals(matcher.getNext(6), 7);
        assertFalse(matcher.hasNext(6));
    }
}
