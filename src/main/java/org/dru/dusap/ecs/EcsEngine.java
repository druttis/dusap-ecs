package org.dru.dusap.ecs;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class EcsEngine extends EcsContainer {
    private final int maxComponentCount;
    private final AtomicInteger entityIdCounter;
    private final Map<Class<?>, EcsMapping<?>> mappingByClass;
    private final AtomicInteger mappingIdCounter;
    private final List<EcsSystem> systems;
    private final Map<Class<? extends EcsSystem>, EcsSystem> systemByClassMap;
    private final Map<EcsAspect, EcsAspect> distinctAspectMap;
    private final Map<EcsAspect, EcsFamily> familyByAspectMap;
    private final AtomicInteger familyIdCounter;
    private final AtomicBoolean updating;
    private final List<Runnable> pending;

    public EcsEngine(final int maxComponentCount) {
        super(0);
        this.maxComponentCount = maxComponentCount;
        entityIdCounter = new AtomicInteger();
        mappingByClass = new ConcurrentHashMap<>();
        mappingIdCounter = new AtomicInteger();
        systems = new CopyOnWriteArrayList<>();
        systemByClassMap = new ConcurrentHashMap<>();
        distinctAspectMap = new ConcurrentHashMap<>();
        familyByAspectMap = new ConcurrentHashMap<>();
        familyIdCounter = new AtomicInteger(1);
        updating = new AtomicBoolean();
        pending = new CopyOnWriteArrayList<>();
    }

    @Override
    void addEntity(final EcsEntity entity) {
        if (updating.get()) {
            pending.add(() -> super.addEntity(entity));
        } else {
            super.addEntity(entity);
        }
    }

    @Override
    void removeEntity(final EcsEntity entity) {
        if (updating.get()) {
            pending.add(() -> super.removeEntity(entity));
        } else {
            super.removeEntity(entity);
        }
    }

    public int getMaxComponentCount() {
        return maxComponentCount;
    }

    public void createEntity(final Consumer<EcsEntity> builder) {
        Objects.requireNonNull(builder, "builder");
        final EcsEntity entity = new EcsEntity(entityIdCounter.getAndIncrement(), this);
        builder.accept(entity);
        addEntity(entity);
    }

    @SuppressWarnings("unchecked")
    public <T> EcsMapping<T> getMapping(final Class<T> type) {
        Objects.requireNonNull(type, "type");
        return (EcsMapping<T>) mappingByClass.computeIfAbsent(type, $ ->
                new EcsMapping<>(this, mappingIdCounter.getAndIncrement(), type));
    }

    public void addSystem(final EcsSystem system) {
        Objects.requireNonNull(system, "system");
        final Class<? extends EcsSystem> systemClass = system.getClass();
        systemByClassMap.compute(systemClass, ($, existing) -> {
            if (existing != null) {
                throw new IllegalArgumentException("system class already added: " + systemClass.getName());
            }
            system.setEngine(this);
            final EcsBuilder builder = new EcsBuilder(this);
            system.configure(builder);
            system.setFamily(getFamily(builder.getAspect()));
            systems.add(system);
            return system;
        });
    }

    public void removeSystem(final EcsSystem system) {
        Objects.requireNonNull(system, "system");
        final Class<? extends EcsSystem> systemClass = system.getClass();
        systemByClassMap.compute(systemClass, ($, existing) -> {
            if (existing != system) {
                throw new IllegalStateException("system class added with another instance: "
                        + systemClass.getName());
            }
            systems.remove(system);
            return null;
        });
    }

    public void removeSystem(final Class<? extends EcsSystem> systemClass) {
        Objects.requireNonNull(systemClass, "systemClass");
        systemByClassMap.compute(systemClass, ($, existing) -> {
            if (existing == null) {
                throw new IllegalArgumentException("system class not added: " + systemClass.getName());
            }
            systems.remove(existing);
            return null;
        });
    }

    public void update() {
        if (!updating.compareAndSet(false, true)) {
            throw new IllegalStateException("already updating");
        }
        try {
            systems.forEach(EcsSystem::update);
        } finally {
            updating.set(false);
            pending.forEach(Runnable::run);
            pending.clear();
        }
    }

    private EcsAspect getDistinctAspect(final EcsAspect aspect) {
        return distinctAspectMap.computeIfAbsent(aspect, $ -> aspect);
    }

    private EcsFamily getFamily(final EcsAspect aspect) {
        final EcsAspect distinctAspect = getDistinctAspect(aspect);
        return familyByAspectMap.computeIfAbsent(distinctAspect, $ -> {
                    final EcsFamily family = new EcsFamily(familyIdCounter.getAndIncrement(), aspect);
                    forEach(entity -> {
                        if (family.test(entity)) {
                            family.addEntity(entity);
                        }
                    });
                    getOnEntityAdded().addConsumer(entity -> {
                        if (family.test(entity)) {
                            family.addEntity(entity);
                        }
                    });
                    getOnEntityRemoved().addConsumer(entity -> {
                        if (family.test(entity)) {
                            family.removeEntity(entity);
                        }
                    });
                    return family;
                }
        );
    }
}
