package org.dru.dusap.ecs;

import org.dru.dusap.messaging.DefaultMessagePipe;
import org.dru.dusap.messaging.MessagePipe;

import java.util.Arrays;
import java.util.Objects;

public final class EcsEntity {
    private final EcsEngine engine;
    private final int id;
    private final MessagePipe<EcsEntity> onComponentAdded;
    private final MessagePipe<EcsEntity> onComponentRemoved;
    private final EcsBitSet bitSet;
    private Object[] components;
    private int[] indexes;

    EcsEntity(final int id, final EcsEngine engine) {
        Objects.requireNonNull(engine, "entityComponentSystem");
        this.id = id;
        this.engine = engine;
        onComponentAdded = new DefaultMessagePipe<>();
        onComponentRemoved = new DefaultMessagePipe<>();
        bitSet = new EcsBitSet(engine.getMaxComponentCount());
        components = new Object[0];
        indexes = new int[0];
    }

    public int getId() {
        return id;
    }

    public EcsEngine getEngine() {
        return engine;
    }

    MessagePipe<EcsEntity> getOnComponentAdded() {
        return onComponentAdded;
    }

    MessagePipe<EcsEntity> getOnComponentRemoved() {
        return onComponentRemoved;
    }

    EcsBitSet getBitSet() {
        return bitSet;
    }

    boolean hasComponent(final int id) {
        return getBitSet().get(id);
    }

    public boolean hasComponent(final EcsMapping<?> mapping) {
        checkMapping(mapping);
        return hasComponent(mapping.getId());
    }

    public boolean hasComponent(final Class<?> type) {
        return hasComponent(getEngine().getMapping(type));
    }

    Object getComponent(final int id) {
        if (!hasComponent(id)) {
            throw new IllegalArgumentException("component not set: " + id);
        }
        return components[id];
    }

    public <T> T getComponent(final EcsMapping<T> mapping) {
        checkMapping(mapping);
        return mapping.getType().cast(getComponent(mapping.getId()));
    }

    public <T> T getComponent(final Class<T> type) {
        return getComponent(getEngine().getMapping(type));
    }

    void setComponent(final int id, final Object component) {
        Objects.requireNonNull(component, "component");
        if (!getBitSet().setIfCleared(id)) {
            throw new IllegalArgumentException("component already set: " + id);
        }
        if (id >= components.length) {
            components = Arrays.copyOf(components, (id + 1) * 3 / 2);
        }
        components[id] = component;
        getOnComponentAdded().dispatchMessage(this);
    }

    public <T> void setComponent(final EcsMapping<T> mapping, final T component) {
        checkMapping(mapping);
        setComponent(mapping.getId(), component);
    }

    public void setComponent(final Object component) {
        Objects.requireNonNull(component, "component");
        setComponent(getEngine().getMapping(component.getClass()).getId(), component);
    }

    public void clearComponent(final int id) {
        if (!getBitSet().clearIfSet(id)) {
            throw new IllegalArgumentException("component not set: " + id);
        }
        components[id] = null;
        getOnComponentRemoved().dispatchMessage(this);
    }

    public void clearComponent(final EcsMapping<?> mapping) {
        checkMapping(mapping);
        clearComponent(mapping.getId());
    }

    public void clearComponent(final Class<?> type) {
        clearComponent(getEngine().getMapping(type).getId());
    }

    public void destroyEntity() {
        getEngine().removeEntity(this);
    }

    int getIndex(final int id) {
        return (id < indexes.length ? indexes[id] : -1);
    }

    void setIndex(final int id, final int index) {
        if (index < 0) {
            throw new IllegalArgumentException("negative index: " + index);
        }
        final int length = indexes.length;
        if (id >= length) {
            final int newLength = (id + 1) * 3 / 2;
            indexes = Arrays.copyOf(indexes, newLength);
            Arrays.fill(indexes, length, newLength, -1);
        }
        indexes[id] = index;
    }

    void clearIndex(final int id) {
        if (id < indexes.length) {
            indexes[id] = -1;
        }
    }

    private void checkMapping(final EcsMapping<?> mapping) {
        Objects.requireNonNull(mapping, "mapping");
        if (mapping.getEngine() != getEngine()) {
            throw new IllegalArgumentException("engine mismatch");
        }
    }
}
