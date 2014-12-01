package org.cobbzilla.util.json.main;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.json.JsonEdit;
import org.cobbzilla.util.json.JsonEditOperation;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.cobbzilla.util.string.StringUtil.empty;

public class JsonEditor {

    @Getter @Setter private JsonEditorOptions options = new JsonEditorOptions();

    public static void main(String[] args) throws Exception {
        JsonEditor editor = new JsonEditor();
        if (editor.init(args)) editor.run();
    }

    private boolean init(String[] args) {
        final CmdLineParser parser = new CmdLineParser(getOptions());
        try {
            parser.parseArgument(args);
            return true;

        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            return false;
        }
    }

    public void run() throws Exception {
        @Cleanup InputStream in = getInputStream();
        JsonEdit edit = new JsonEdit()
                .setJsonData(in)
                .addOperation(new JsonEditOperation()
                        .setType(options.getOperationType())
                        .setPath(options.getPath())
                        .setJson(options.getValue()));

        final String json = edit.edit();

        if (options.hasOutfile()) {
            FileUtil.toFile(options.getOutfile(), json);
        } else {
            if (empty(json)) {
                System.exit(1);
            } else {
                System.out.print(json);
            }
        }
        System.exit(0);
    }

    private InputStream getInputStream() throws FileNotFoundException {
        return options.hasJsonFile() ? new FileInputStream(options.getJsonFile()) : System.in;
    }
}