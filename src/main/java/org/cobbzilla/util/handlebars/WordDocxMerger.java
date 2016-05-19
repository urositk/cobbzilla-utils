package org.cobbzilla.util.handlebars;

import com.github.jknack.handlebars.Handlebars;
import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.ConverterTypeVia;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.http.HtmlScreenCapture;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.xml.TidyHandlebarsSpanMerger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Map;

import static org.cobbzilla.util.io.FileUtil.temp;
import static org.cobbzilla.util.xml.TidyUtil.tidy;

@Slf4j
public class WordDocxMerger {

    public static File[] merge(InputStream in,
                               Map<String, Object> context,
                               HtmlScreenCapture capture,
                               Handlebars handlebars) throws Exception {

        final IXDocReport report = XDocReportRegistry.getRegistry().loadReport(in, TemplateEngineKind.Velocity);
        final Options options = Options.getTo(ConverterTypeTo.XHTML).via(ConverterTypeVia.XWPF);

        @Cleanup final ByteArrayOutputStream out = new ByteArrayOutputStream();
        report.convert(report.createContext(), options, out);

        // tidy HTML file merge consecutive <span> tags (which might occur in the middle of a {{variable}}), then apply Handlebars
        final File mergedHtml = temp(".html");
        FileUtil.toFile(mergedHtml, StringTemplateLoader.apply(handlebars, tidy(out.toString(), TidyHandlebarsSpanMerger.instance), context));

        // convert HTML -> PDF
        final File pdfOutput = temp(".pdf");
        capture.capture(mergedHtml, pdfOutput);

        return new File[] { mergedHtml, pdfOutput };
    }

}
