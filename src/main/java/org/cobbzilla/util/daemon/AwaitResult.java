package org.cobbzilla.util.daemon;

import lombok.Getter;
import lombok.ToString;

import java.util.*;
import java.util.concurrent.Future;

@ToString
public class AwaitResult<T> {

    @Getter private Map<Future, T> successes = new HashMap<>();
    public void success(Future f, T thing) { successes.put(f, thing); }
    public int numSuccesses () { return successes.size(); }

    @Getter private Map<Future, Exception> failures = new HashMap<>();
    public void fail(Future f, Exception e) { failures.put(f, e); }
    public int numFails () { return failures.size(); }

    @Getter private List<Future> timeouts = new ArrayList<>();
    public void timeout (Collection<Future<?>> timedOut) { timeouts.addAll(timedOut); }
    public boolean timedOut() { return !timeouts.isEmpty(); }
    public int numTimeouts () { return timeouts.size(); }

    public boolean allSucceeded() { return failures.isEmpty() && timeouts.isEmpty(); }

}
