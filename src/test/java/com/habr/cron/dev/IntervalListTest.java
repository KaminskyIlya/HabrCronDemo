package com.habr.cron.dev;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

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
//                {50, 51}, // throws Assertion error, it's right
        };
    }
}
