package org.cobbzilla.util.daemon;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;
import java.util.concurrent.TimeoutException;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@AllArgsConstructor
public class AwaitTimeoutException extends TimeoutException {

    @Getter private final Collection results;

    public int getResultCount () { return empty(results) ? 0 : results.size(); }

}
