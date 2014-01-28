package org.cobbzilla.util.system;

import lombok.Getter;

public class CommandResult {

    @Getter private Integer exitStatus;
    @Getter private String stdout;
    @Getter private String stderr;
    @Getter private Exception exception;

    public boolean hasException () { return exception != null; }

    public CommandResult (Integer exitStatus, String stdout, String stderr) {
        this.exitStatus = exitStatus; this.stdout = stdout; this.stderr = stderr;
    }

    public CommandResult (Exception e) { this.exception = e; }

    @Override
    public String toString() {
        return "{" +
                "exitStatus=" + exitStatus +
                ", stdout='" + stdout + '\'' +
                ", stderr='" + stderr + '\'' +
                ", exception=" + exception +
                '}';
    }
}
