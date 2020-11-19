package org.dru.dusap.ecs;

import java.util.Objects;
import java.util.function.Predicate;

public final class EcsAspect implements Predicate<EcsEntity> {
    private final EcsBitSet allOf;
    private final EcsBitSet anyOf;
    private final EcsBitSet noneOf;

    EcsAspect(final EcsBitSet allOf, final EcsBitSet anyOf, final EcsBitSet noneOf) {
        this.allOf = allOf;
        this.anyOf = anyOf;
        this.noneOf = noneOf;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final EcsAspect aspect = (EcsAspect) o;
        return allOf.equals(aspect.allOf) &&
                anyOf.equals(aspect.anyOf) &&
                noneOf.equals(aspect.noneOf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allOf, anyOf, noneOf);
    }

    @Override
    public boolean test(final EcsEntity entity) {
        return (allOf == null || entity.getBitSet().containsAll(allOf))
                && (anyOf == null || entity.getBitSet().intersects(anyOf))
                && (noneOf == null || !entity.getBitSet().intersects(noneOf));
    }
}
