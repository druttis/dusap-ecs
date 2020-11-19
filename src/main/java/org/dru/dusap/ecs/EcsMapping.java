package org.dru.dusap.ecs;

import java.util.Objects;

public final class EcsMapping<T> {
    private final EcsEngine engine;
    private final int id;
    private final Class<T> type;

    EcsMapping(final EcsEngine engine, final int id, final Class<T> type) {
        this.engine = engine;
        this.id = id;
        this.type = type;
    }

    public EcsEngine getEngine() {
        return engine;
    }

    public int getId() {
        return id;
    }

    public Class<T> getType() {
        return type;
    }
}
