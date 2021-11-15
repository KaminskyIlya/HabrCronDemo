package com.habr.cron.ilya;

/**
 * Matcher of calendar element for fixed constant value.
 * Unmodified object. Thread-safe.
 *
 * Difficulty:
 *  matching one value - O(1)
 *  find nearest value - O(1)
 * Used memory:
 *  15 bytes
 */
class ConstantMatcher extends MatcherBase {
    private final int value;

    public ConstantMatcher(int value) {
        super(value, value);
        this.value = value;
    }

    public boolean match(int value, CalendarElement element) {
        return this.value == value && inBounds(value, element);
    }

    public int getMajor(int v, CalendarElement element) {
        return v < this.value ? this.value : v + 1; // makes an overflow
    }

    public int getMinor(int v, CalendarElement element) {
        return v > this.value ? this.value : v - 1; // makes an overflow
    }

    public boolean isAbove(int value, CalendarElement element) {
        return value > this.value;
    }

    public boolean isBelow(int value, CalendarElement element)
    {
        return value < this.value;
    }

}
