package org.cobbzilla.util.cache;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.cobbzilla.util.daemon.ZillaRuntime.now;

public abstract class AutoRefreshingReference<T> {

    @Getter private final AtomicReference<T> object = new AtomicReference<>();
    @Getter private final AtomicLong lastSet = new AtomicLong();

    public abstract T refresh();
    public abstract long getTimeout();

    public T get() {
        if (object.get() == null || now() - lastSet.get() > getTimeout()) update();
        return object.get();
    }

    public void update() {
        object.set(refresh());
        lastSet.set(now());
    }

    public void set(T thing) {
        object.set(thing);
        lastSet.set(now());
    }

}
