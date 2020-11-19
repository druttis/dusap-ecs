package org.dru.dusap.ecs;

import java.util.function.Consumer;

public abstract class EcsSystem {
    private EcsEngine engine;
    private EcsFamily family;

    protected EcsSystem() {
    }

    final EcsEngine getEngine() {
        return engine;
    }

    final void setEngine(final EcsEngine engine) {
        this.engine = engine;
    }

    final EcsFamily getFamily() {
        return family;
    }

    final void setFamily(final EcsFamily family) {
        this.family = family;
    }

    protected final <T> EcsMapping<T> getMapping(final Class<T> type) {
        return getSetEngine().getMapping(type);
    }

    protected final void createEntity(Consumer<EcsEntity> entityBuilder) {
        getSetEngine().createEntity(entityBuilder);
    }

    protected void forEach(final Consumer<EcsEntity> action) {
        final EcsFamily family = getFamily();
        if (family == null) {
            throw new IllegalStateException("family not set");
        }
        family.forEach(action);
    }

    protected abstract void configure(EcsBuilder builder);

    protected abstract void update();

    private EcsEngine getSetEngine() {
        final EcsEngine engine = getEngine();
        if (engine == null) {
            throw new IllegalStateException("engine not set");
        }
        return engine;
    }
}
