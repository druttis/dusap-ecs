package org.dru.dusap.test;

import org.dru.dusap.ecs.*;

import java.awt.*;

public class TestSystem extends EcsIntervalSystem {
    private EcsMapping<Point> mPoint;

    public TestSystem() {
        super(1000);
    }

    @Override
    protected void configure(final EcsBuilder builder) {
        builder.allOf(Point.class);
        mPoint = getMapping(Point.class);
        for (int i = 0; i < 100000; i++) {
            createEntity(entity -> {
                entity.setComponent(mPoint, new Point(0, 0));
            });
        }
    }

    @Override
    protected void updateNow() {
        forEach(entity -> entity.getComponent(mPoint).x++);
    }

    public static void main(String[] args) {
        final EcsEngine engine = new EcsEngine(1024);
        // you can force specify mapping ids (ordinal)
        engine.getMapping(Point.class);
        //
        engine.addSystem(new TestSystem());
        int a = 0;
        long time = System.currentTimeMillis();
        for (int iter = 0; iter < 10000; iter++) {
            a++;
            engine.update();
        }
        long now = System.currentTimeMillis();
        System.out.println(a);
        System.out.println(now - time);
    }
}
