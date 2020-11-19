package org.dru.dusap.ecs;

import org.dru.dusap.messaging.DefaultMessagePipe;
import org.dru.dusap.messaging.MessagePipe;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class EcsContainer {
    private final int id;
    private final MessagePipe<EcsEntity> onEntityAdded;
    private final MessagePipe<EcsEntity> onEntityRemoved;
    private EcsEntity[] entities;
    private int entityCount;

    public EcsContainer(final int id) {
        this.id = id;
        onEntityAdded = new DefaultMessagePipe<>();
        onEntityRemoved = new DefaultMessagePipe<>();
        entities = new EcsEntity[0];
        entityCount = 0;
    }

    public void forEach(final Consumer<EcsEntity> action) {
        Objects.requireNonNull(action, "action");
        for (int index = 0; index < entityCount; index++) {
            action.accept(entities[index]);
        }
    }

    public final int getId() {
        return id;
    }

    public final MessagePipe<EcsEntity> getOnEntityAdded() {
        return onEntityAdded;
    }

    public final MessagePipe<EcsEntity> getOnEntityRemoved() {
        return onEntityRemoved;
    }

    boolean hasEntity(final EcsEntity entity) {
        Objects.requireNonNull(entity, "entity");
        return entity.getIndex(getId()) != -1;
    }

    void addEntity(final EcsEntity entity) {
        if (hasEntity(entity)) {
            throw new IllegalArgumentException("entity already added: id=" + entity.getId()
                    + ", containerId=" + getId());
        }
        if (entityCount >= entities.length) {
            entities = Arrays.copyOf(entities, (entityCount + 1) * 3 / 2);
        }
        entity.setIndex(getId(), entityCount);
        entities[entityCount++] = entity;
        getOnEntityAdded().dispatchMessage(entity);
    }

    void removeEntity(final EcsEntity entity) {
        Objects.requireNonNull(entity, "entity");
        final int index = entity.getIndex(getId());
        if (index == -1) {
            throw new IllegalArgumentException("entity not added: entityId=" + entity.getId()
                    + ", containerId=" + getId());
        }
        entity.clearIndex(getId());
        entities[index] = entities[--entityCount];
        entities[index].setIndex(getId(), index);
        getOnEntityRemoved().dispatchMessage(entity);
    }
}
