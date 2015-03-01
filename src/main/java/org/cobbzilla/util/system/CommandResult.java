package org.cobbzilla.util.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import static org.cobbzilla.util.string.StringUtil.UTF8;

public class CommandResult {

    // useful for mocks
    public static final CommandResult OK = new CommandResult(0, null, null);

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

    public CommandResult (int exitValue, ByteArrayOutputStream out, ByteArrayOutputStream err) {
        this.exitStatus = exitValue;
        try {
            this.stdout = out == null ? null : out.toString(UTF8);
            this.stderr = err == null ? null : err.toString(UTF8);
        } catch (UnsupportedEncodingException e) {
            // should never happen
            throw new IllegalStateException("CommandResult: couldn't convert stream to string: "+e, e);
        }
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
