package org.dru.dusap.ecs;

public abstract class EcsIntervalSystem extends EcsSystem {
    private final long interval;
    private long when;

    protected EcsIntervalSystem(final long interval) {
        if (interval < 1L) {
            throw new IllegalArgumentException("interval has to be 1 or greater: " + interval);
        }
        this.interval = interval;
        when = -1L;
    }

    @Override
    protected final void update() {
        final long now = System.currentTimeMillis();
        if (when == -1L) {
            when = now;
        }
        if (now >= when) {
            if (now < when + interval) {
                when = when + interval;
            } else {
                when = now + interval;
            }
            updateNow();
        }
    }

    protected abstract void updateNow();
}
