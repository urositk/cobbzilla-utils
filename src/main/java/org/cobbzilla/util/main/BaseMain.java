package org.cobbzilla.util.main;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.daemon.ZillaRuntime;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import static org.cobbzilla.util.reflect.ReflectionUtil.getFirstTypeParam;
import static org.cobbzilla.util.reflect.ReflectionUtil.instantiate;

@Slf4j
public abstract class BaseMain<OPT extends BaseMainOptions> {

    @Getter private final OPT options = initOptions();
    protected OPT initOptions() { return instantiate((Class<OPT>) getFirstTypeParam(getClass())); }

    @Getter(value=AccessLevel.PROTECTED) private final CmdLineParser parser = new CmdLineParser(getOptions());

    protected abstract void run() throws Exception;

    public void runOrDie () { try { run(); } catch (Exception e) { die("runOrDie: "+e, e); } }

    @Getter private String[] args;
    public void setArgs(String[] args) throws CmdLineException {
        this.args = args;
        try {
            parser.parseArgument(args);
            if (options.isHelp()) {
                showHelpAndExit();
            }

        } catch (Exception e) {
            showHelpAndExit(e);
        }
    }

    protected void preRun() {}
    protected void postRun() {}

    public static void main(Class<? extends BaseMain> clazz, String[] args) {
        try {
            final BaseMain m = clazz.newInstance();
            m.setArgs(args);
            m.preRun();
            m.run();
            m.postRun();

        } catch (Exception e) {
            log.error("Unexpected error: " + e, e);
            ZillaRuntime.die("Unexpected error: " + e);
        }
    }

    public void showHelpAndExit() {
        parser.printUsage(System.out);
        System.exit(0);
    }

    public void showHelpAndExit(String error) { showHelpAndExit(new IllegalArgumentException(error)); }

    public void showHelpAndExit(Exception e) {
        parser.printUsage(System.err);
        System.exit(1);
    }

    public void out(String message) { System.out.println(message); }

    public void err (String message) { System.err.println(message); }

    public <T> T die (String message) {
        log.error(message);
        err(message);
        System.exit(1);
        return null;
    }

    public <T> T die (String message, Exception e) {
        log.error(message, e);
        err(message + ": " + e);
        System.exit(1);
        return null;
    }
}
