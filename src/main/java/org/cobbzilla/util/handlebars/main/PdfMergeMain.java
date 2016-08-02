package org.cobbzilla.util.handlebars.main;

import com.github.jknack.handlebars.Handlebars;
import lombok.Cleanup;
import lombok.Getter;
import org.cobbzilla.util.handlebars.PdfMerger;
import org.cobbzilla.util.main.BaseMain;

import java.io.File;
import java.io.InputStream;

import static org.cobbzilla.util.io.FileUtil.abs;

public class PdfMergeMain extends BaseMain<PdfMergeOptions> {

    public static void main (String[] args) { main(PdfMergeMain.class, args); }

    @Getter protected Handlebars handlebars;

    @Override protected void run() throws Exception {
        final PdfMergeOptions options = getOptions();
        @Cleanup final InputStream in = options.getInputStream();
        if (options.hasOutfile()) {
            final File outfile = options.getOutfile();
            PdfMerger.merge(in, outfile, options.getContext(), getHandlebars());
            out(abs(outfile));

        } else {
            final File[] output = PdfMerger.merge(in, options.getContext(), getHandlebars());
            for (File f : output) {
                out(abs(f));
            }
        }
    }

}
