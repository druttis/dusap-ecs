package org.dru.dusap.ecs;

import java.util.stream.Stream;

public final class EcsBuilder {
    private final EcsEngine engine;
    private EcsBitSet allOff;
    private EcsBitSet anyOff;
    private EcsBitSet noneOff;

    EcsBuilder(final EcsEngine engine) {
        this.engine = engine;
    }

    @SuppressWarnings("UnusedReturnValue")
    public EcsBuilder allOf(Class<?> firstComponentClass, Class<?>... restComponentClasses) {
        allOff = setInBitSet(allOff, firstComponentClass, restComponentClasses);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public EcsBuilder anyOf(final Class<?> firstComponentClass, final Class<?>... restComponentClasses) {
        anyOff = setInBitSet(anyOff, firstComponentClass, restComponentClasses);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public EcsBuilder noneOf(final Class<?> firstComponentClass, final Class<?>... restComponentClasses) {
        noneOff = setInBitSet(noneOff, firstComponentClass, restComponentClasses);
        return this;
    }

    EcsAspect getAspect() {
        return new EcsAspect(allOff, anyOff, noneOff);
    }

    private EcsBitSet setInBitSet(final EcsBitSet bitSet, final Class<?> firstComponentClass,
                                  final Class<?>... restComponentClasses) {
        final EcsBitSet result = (bitSet != null ? bitSet : new EcsBitSet(engine.getMaxComponentCount()));
        result.set(engine.getMapping(firstComponentClass).getId());
        Stream.of(restComponentClasses).forEach(componentClass ->
                result.set(engine.getMapping(componentClass).getId()));
        return result;
    }
}
