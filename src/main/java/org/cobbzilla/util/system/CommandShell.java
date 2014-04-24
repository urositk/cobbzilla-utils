package org.cobbzilla.util.system;

import org.apache.commons.exec.*;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.cobbzilla.util.string.StringUtil.UTF8;
import static org.cobbzilla.util.string.StringUtil.UTF8cs;

public class CommandShell {

    protected static final String EXPORT_PREFIX = "export ";

    public static final String CHMOD = "chmod";
    public static final String P_600 = "600";

    public static Map<String, String> loadShellExports (String userFile) throws IOException {
        File file = new File(System.getProperty("user.home") + File.separator + userFile);
        if (!file.exists()) {
            throw new IllegalArgumentException("file does not exist: "+file.getAbsolutePath());
        }
        return loadShellExports(file);
    }

    public static Map<String, String> loadShellExports (File f) throws IOException {
        final Map<String, String> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)))) {
            String line, key, value;
            int eqPos;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) continue;
                if (line.startsWith(EXPORT_PREFIX)) {
                    line = line.substring(EXPORT_PREFIX.length()).trim();
                    eqPos = line.indexOf('=');
                    if (eqPos != -1) {
                        key = line.substring(0, eqPos).trim();
                        value = line.substring(eqPos+1).trim();
                        map.put(key, value);
                    }
                }
            }
        }
        return map;
    }

    public static MultiCommandResult exec (Collection<String> commands) throws IOException {
        final MultiCommandResult result = new MultiCommandResult();
        for (String command : commands) {
            exec(command, result);
            if (result.hasException()) return result;
        }
        result.setSuccess(true);
        return result;
    }

    public static CommandResult exec (String command) throws IOException {
        return exec(CommandLine.parse(command));
    }

    public static CommandResult exec (CommandLine command) throws IOException {
        MultiCommandResult result = exec(command, null, null);
        return result.getResults().values().iterator().next();
    }

    public static CommandResult exec (CommandLine command, Map<String, String> environment) throws IOException {
        MultiCommandResult result = exec(command, null, null, null, environment);
        return result.getResults().values().iterator().next();
    }

    public static CommandResult exec (CommandLine command, String input,
                                      File workingDir, Map<String, String> environment) throws IOException {
        MultiCommandResult result = exec(command, null, input, workingDir, environment);
        return result.getResults().values().iterator().next();
    }

    public static CommandResult exec (CommandLine command, File workingDir) throws IOException {
        MultiCommandResult result = exec(command, null, null, workingDir);
        return result.getResults().values().iterator().next();
    }

    public static CommandResult exec (String command, String input) throws IOException {
        return exec(CommandLine.parse(command), input);
    }

    public static CommandResult exec (CommandLine commandLine, String input) throws IOException {
        MultiCommandResult result = exec(commandLine, null, input);
        return result.getResults().values().iterator().next();
    }

    public static MultiCommandResult exec (String command, MultiCommandResult result) throws IOException {
        return exec(command, result, null);
    }

    public static MultiCommandResult exec (String cmdLine, MultiCommandResult result, String input) throws IOException {
        return exec(CommandLine.parse(cmdLine), result, input);
    }

    public static MultiCommandResult exec (CommandLine cmdLine, MultiCommandResult result, String input) throws IOException {
        return exec(cmdLine, result, input, null);
    }

    public static MultiCommandResult exec (CommandLine cmdLine, MultiCommandResult result,
                                           String input, File workingDir) throws IOException {
        return exec(cmdLine, result, input, workingDir, null);
    }

    public static MultiCommandResult exec (CommandLine cmdLine, MultiCommandResult result,
                                           String input, File workingDir,
                                           Map<String, String> environment) throws IOException {
        if (result == null) result = new MultiCommandResult();
        final DefaultExecutor executor = new DefaultExecutor();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ByteArrayOutputStream err = new ByteArrayOutputStream();
        final ByteArrayInputStream in = (input == null) ? null : new ByteArrayInputStream(input.getBytes(UTF8cs));
        final ExecuteStreamHandler handler = new PumpStreamHandler(out, err, in);
        executor.setStreamHandler(handler);
        if (workingDir != null) executor.setWorkingDirectory(workingDir);
        int exitValue = -1;
        try {
            exitValue = executor.execute(cmdLine, environment);
            result.add(cmdLine, new CommandResult(exitValue, out.toString(UTF8), err.toString(UTF8)));
            if (exitValue != 0) {
                // shouldn't happen since executor.execute will throw an exception on a non-zero exit status
                result.exception(cmdLine, new IllegalStateException("non-zero value ("+exitValue+") returned from cmdLine: "+cmdLine));
            }

        } catch (Exception e) {
            result.add(cmdLine, new CommandResult(exitValue, out.toString(UTF8), err.toString(UTF8)));
            result.exception(cmdLine, e);
        }
        return result;
    }

    public static int chmod (File file, String perms) throws IOException {
        return chmod(file.getAbsolutePath(), perms);
    }

    public static int chmod (String file, String perms) throws IOException {
        CommandLine commandLine = new CommandLine(CHMOD);
        commandLine.addArgument(perms);
        commandLine.addArgument(file);
        Executor executor = new DefaultExecutor();
        return executor.execute(commandLine);
    }

}
