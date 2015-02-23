package org.cobbzilla.util.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class CommandResult {

    @Getter @Setter private String stdout;
    @Getter @Setter private String stderr;

    @Getter @Setter private Integer exitStatus;

    @JsonIgnore public boolean isZeroExitStatus () { return exitStatus != null && exitStatus == 0; }

    @JsonIgnore @Getter private Exception exception;
    public boolean hasException () { return exception != null; }
    public String getExceptionString () { return hasException() ? exception.toString() : null; }
    public void setExceptionString (String ex) { exception = new Exception(ex); }

    public CommandResult (Integer exitStatus, String stdout, String stderr) {
        this.exitStatus = (exitStatus == null) ? -1 : exitStatus;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public CommandResult (Exception e) { this.exception = e; }

    @Override
    public String toString() {
        return "{" +
                "exitStatus=" + exitStatus +
                ", stdout='" + stdout + '\'' +
                ", stderr='" + stderr + '\'' +
                ", exception=" + getExceptionString() +
                '}';
    }
}
