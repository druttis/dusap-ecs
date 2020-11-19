package org.dru.dusap.ecs;

import java.util.concurrent.atomic.AtomicLongArray;

public final class EcsBitSet {
    private final AtomicLongArray data;

    public EcsBitSet(final int sizeInBits) {
        data = new AtomicLongArray((sizeInBits >> 6) + 1);
    }

    private boolean get(final int index, final long bitval) {
        return (data.get(index) & bitval) != 0L;
    }

    public boolean get(final int pos) {
        return get(index(pos), bitval(pos));
    }

    public void set(final int pos) {
        final long bitval = bitval(pos);
        data.updateAndGet(index(pos), value -> value | bitval);
    }

    public boolean getAndSet(final int pos) {
        final long bitval = bitval(pos);
        return (data.getAndUpdate(index(pos), value -> value | bitval) & bitval) != 0L;
    }

    public boolean setIfCleared(final int pos) {
        final int index = index(pos);
        final long bitval = bitval(pos);
        final long value = data.get(index);
        return (data.compareAndSet(index, value & ~bitval, value | bitval));
    }

    public boolean clearIfSet(final int pos) {
        final int index = index(pos);
        final long bitval = bitval(pos);
        final long value = data.get(index);
        return (data.compareAndSet(index, value | bitval, value & ~bitval));
    }

    public boolean intersects(final EcsBitSet other) {
        for (int index = Math.min(data.length(), other.data.length()); --index >= 0; ) {
            if ((data.get(index) & other.data.get(index)) != 0L) {
                return true;
            }
        }
        return false;
    }

    public boolean containsAll(final EcsBitSet other) {
        final int dataLength = data.length();
        final int otherDataLength = other.data.length();
        for (int index = dataLength; index < otherDataLength; index++) {
            if (other.data.get(index) != 0L) {
                return false;
            }
        }
        for (int index = Math.min(dataLength, otherDataLength); --index >= 0; ) {
            final long value = data.get(index);
            if ((value & other.data.get(index)) != value) {
                return false;
            }
        }
        return true;
    }

    private int index(final int pos) {
        return pos >> 6;
    }

    private long bitval(final int pos) {
        return 1L << (pos & 63);
    }
}
