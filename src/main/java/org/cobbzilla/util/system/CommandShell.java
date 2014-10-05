package org.cobbzilla.util.system;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.*;
import org.apache.commons.io.output.TeeOutputStream;
import org.cobbzilla.util.collection.MapBuilder;
import org.cobbzilla.util.io.FileUtil;

import java.io.*;
import java.util.*;

import static org.cobbzilla.util.string.StringUtil.UTF8;
import static org.cobbzilla.util.string.StringUtil.UTF8cs;

@Slf4j
public class CommandShell {

    protected static final String EXPORT_PREFIX = "export ";

    public static final String CHMOD = "chmod";
    public static final String CHGRP = "chgrp";
    public static final String CHOWN = "chown";

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
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            // strip quotes if found
                            value = value.substring(1, value.length()-1);
                        }
                        map.put(key, value);
                    }
                }
            }
        }
        return map;
    }

    public static Map<String, String> loadShellExportsOrDie (String f) {
        try { return loadShellExports(f); } catch (Exception e) {
            throw new IllegalStateException("loadShellExportsOrDie: "+e, e);
        }
    }

    public static Map<String, String> loadShellExportsOrDie (File f) {
        try { return loadShellExports(f); } catch (Exception e) {
            throw new IllegalStateException("loadShellExportsOrDie: "+e, e);
        }
    }

    public static void replaceShellExport (String f, String name, String value) throws IOException {
        replaceShellExports(new File(f), MapBuilder.build(name, value));
    }

    public static void replaceShellExport (File f, String name, String value) throws IOException {
        replaceShellExports(f, MapBuilder.build(name, value));
    }

    public static void replaceShellExports (String f, Map<String, String> exports) throws IOException {
        replaceShellExports(new File(f), exports);
    }

    public static void replaceShellExports (File f, Map<String, String> exports) throws IOException {

        // validate -- no quote chars allowed for security reasons
        for (String key : exports.keySet()) {
            if (key.contains("\"") || key.contains("\'")) throw new IllegalArgumentException("replaceShellExports: name cannot contain a quote character: "+key);
            String value = exports.get(key);
            if (value.contains("\"") || value.contains("\'")) throw new IllegalArgumentException("replaceShellExports: value for "+key+" cannot contain a quote character: "+value);
        }

        // read entire file as a string
        final String contents = FileUtil.toString(f);

        // walk file line by line and look for replacements to make, overwrite file.
        final Set<String> replaced = new HashSet<>(exports.size());
        try (Writer w = new FileWriter(f)) {
            for (String line : contents.split("\n")) {
                line = line.trim();
                boolean found = false;
                for (String key : exports.keySet()) {
                    if (!line.startsWith("#") && line.matches("^\\s*export\\s+" + key + "\\s*=.*")) {
                        w.write("export " + key + "=\"" + exports.get(key) + "\"");
                        replaced.add(key);
                        found = true;
                        break;
                    }
                }
                if (!found) w.write(line);
                w.write("\n");
            }

            for (String key : exports.keySet()) {
                if (!replaced.contains(key)) {
                    w.write("export "+key+"=\""+exports.get(key)+"\"\n");
                }
            }
        }
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
        final TeeOutputStream teeOut = new TeeOutputStream(out, System.out);

        final ByteArrayOutputStream err = new ByteArrayOutputStream();
        final TeeOutputStream teeErr = new TeeOutputStream(err, System.err);

        final ByteArrayInputStream in = (input == null) ? null : new ByteArrayInputStream(input.getBytes(UTF8cs));
        final ExecuteStreamHandler handler = new PumpStreamHandler(teeOut, teeErr, in);
        executor.setStreamHandler(handler);
        if (workingDir != null) executor.setWorkingDirectory(workingDir);
        int exitValue = -1;
        try {
            exitValue = executor.execute(cmdLine, environment);
            result.add(cmdLine, new CommandResult(exitValue, out.toString(UTF8), err.toString(UTF8)));
            if (exitValue != 0) {
                // shouldn't happen since executor.execute will throw an exception on a non-zero exit status
                final String baseMessage = "non-zero value (" + exitValue + ") returned from cmdLine: " + cmdLine;
                final String message = baseMessage + ": out=" + out.toString(UTF8) + ", err=" + err.toString(UTF8);
                log.info(message);
                result.exception(cmdLine, new IllegalStateException(baseMessage));
            }

        } catch (Exception e) {
            result.add(cmdLine, new CommandResult(exitValue, out.toString(UTF8), err.toString(UTF8)));
            result.exception(cmdLine, e);
        }
        return result;
    }

    public static int chmod (File file, String perms) throws IOException {
        return chmod(file.getAbsolutePath(), perms, false);
    }
    public static int chmod (File file, String perms, boolean recursive) throws IOException {
        return chmod(file.getAbsolutePath(), perms, recursive);
    }

    public static int chmod (String file, String perms) throws IOException {
        return chmod(file, perms, false);
    }

    public static int chmod (String file, String perms, boolean recursive) throws IOException {
        final CommandLine commandLine = new CommandLine(CHMOD);
        if (recursive) commandLine.addArgument("-R");
        commandLine.addArgument(perms);
        commandLine.addArgument(file);
        final Executor executor = new DefaultExecutor();
        return executor.execute(commandLine);
    }

    public static int chgrp(String group, File path) throws IOException {
        return chgrp(group, path, false);
    }

    public static int chgrp(String group, File path, boolean recursive) throws IOException {
        return chgrp(group, path.getAbsolutePath(), recursive);
    }

    public static int chgrp(String group, String path) throws IOException {
        return chgrp(group, path, false);
    }

    public static int chgrp(String group, String path, boolean recursive) throws IOException {
        final Executor executor = new DefaultExecutor();
        final CommandLine command = new CommandLine(CHGRP);
        if (recursive) command.addArgument("-R");
        command.addArgument(group).addArgument(path);
        return executor.execute(command);
    }

    public static int chown(String group, File path) throws IOException {
        return chown(group, path, false);
    }

    public static int chown(String group, File path, boolean recursive) throws IOException {
        return chown(group, path.getAbsolutePath(), recursive);
    }

    public static int chown(String group, String path) throws IOException {
        return chown(group, path, false);
    }

    public static int chown(String group, String path, boolean recursive) throws IOException {
        final Executor executor = new DefaultExecutor();
        final CommandLine command = new CommandLine(CHOWN);
        if (recursive) command.addArgument("-R");
        command.addArgument(group).addArgument(path);
        return executor.execute(command);
    }

    public static String toString(String command) {
        try {
            return exec(command).getStdout().trim();
        } catch (IOException e) {
            throw new IllegalStateException("Error executing: "+command+": "+e, e);
        }
    }

    public static String hostname () {
        try {
            return exec("hostname").getStdout().trim();
        } catch (IOException e) {
            throw new IllegalStateException("CommandShell.hostname() error: "+e, e);
        }
    }

    public static File tempScript (String contents) {
        contents = "#!/bin/bash\n\n"+contents;
        try {
            final File temp = File.createTempFile("tempScript", ".sh");
            FileUtil.toFile(temp, contents);
            chmod(temp, "700");
            return temp;

        } catch (Exception e) {
            throw new IllegalStateException("tempScript("+contents+") failed: "+e, e);
        }
    }

    public static String execScript (String contents){
        try {
            @Cleanup("delete") final File script = tempScript(contents);
            return exec(new CommandLine(script)).getStdout();
        } catch (Exception e) {
            throw new IllegalStateException("Error executing: "+e);
        }
    }

}
