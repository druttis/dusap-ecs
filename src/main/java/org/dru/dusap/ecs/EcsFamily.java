package org.dru.dusap.ecs;

import java.util.function.Consumer;
import java.util.function.Predicate;

public final class EcsFamily extends EcsContainer implements Predicate<EcsEntity> {
    private final Consumer<EcsEntity> onComponentAdded;
    private final Consumer<EcsEntity> onComponentRemoved;
    private final EcsAspect aspect;

    EcsFamily(final int id, final EcsAspect aspect) {
        super(id);
        this.aspect = aspect;
        onComponentAdded = this::componentAdded;
        onComponentRemoved = this::componentRemoved;
    }

    @Override
    void addEntity(final EcsEntity entity) {
        super.addEntity(entity);
        entity.getOnComponentAdded().addConsumer(onComponentAdded);
        entity.getOnComponentRemoved().addConsumer(onComponentRemoved);
    }

    @Override
    void removeEntity(final EcsEntity entity) {
        super.removeEntity(entity);
        entity.getOnComponentAdded().removeConsumer(onComponentAdded);
        entity.getOnComponentRemoved().removeConsumer(onComponentRemoved);
    }

    @Override
    public boolean test(final EcsEntity entity) {
        return aspect.test(entity);
    }

    private void componentAdded(final EcsEntity entity) {
        if (!hasEntity(entity) && test(entity)) {
            addEntity(entity);
        }
    }

    private void componentRemoved(final EcsEntity entity) {
        if (hasEntity(entity) && !test(entity)) {
            removeEntity(entity);
        }
    }
}
