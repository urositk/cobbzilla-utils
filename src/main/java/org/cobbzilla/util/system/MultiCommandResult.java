package org.cobbzilla.util.system;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

public class MultiCommandResult {

    @Getter private final Map<String, CommandResult> results = new LinkedHashMap<>();
    @Getter @Setter private boolean success = false;
    @Getter private Exception exception = null;

    public boolean hasException () { return exception != null;}

    public void add(String command, CommandResult commandResult) { results.put(command, commandResult); }

    public void exception(String command, Exception exception) {
        add(command, new CommandResult(exception));
        this.exception = exception;
    }

    @Override
    public String toString() {
        return "MultiCommandResult{" +
                "results=" + results +
                ", success=" + success +
                ", exception=" + exception +
                '}';
    }
}
