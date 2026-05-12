package com.walkitalki.core.time;

public final class FakeClock implements Clock {
    private long nowMillis;

    private FakeClock(long nowMillis) {
        this.nowMillis = nowMillis;
    }

    public static FakeClock at(long nowMillis) {
        return new FakeClock(nowMillis);
    }

    @Override
    public long nowMillis() {
        return nowMillis;
    }

    public FakeClock advanceMillis(long millis) {
        if (millis < 0L) {
            throw new IllegalArgumentException("millis must not be negative");
        }
        nowMillis += millis;
        return this;
    }
}
