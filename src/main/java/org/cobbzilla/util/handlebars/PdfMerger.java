package org.cobbzilla.util.handlebars;

import com.github.jknack.handlebars.Handlebars;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.FileUtil.temp;
import static org.cobbzilla.util.reflect.ReflectionUtil.instantiate;

@Slf4j
public class PdfMerger {

    public static final String NULL_FORM_VALUE = "þÿ";

    public static void merge(InputStream in,
                             File outfile,
                             Map<String, Object> context,
                             Handlebars handlebars) throws Exception {
        final File[] out = merge(in, context, handlebars);
        if (empty(out)) die("merge: no outfiles generated");
        if (out.length > 1) die("merge: multiple outfiles generated");
        if (!out[0].renameTo(outfile)) die("merge: error renaming "+abs(out[0])+"->"+abs(outfile));
    }

    public static File[] merge(InputStream in,
                               Map<String, Object> context,
                               Handlebars handlebars) throws Exception {

        final Map<String, String> fieldMappings = (Map<String, String>) context.get("fields");

        // load the document
        final PDDocument pdfDocument = PDDocument.load(in);

        // get the document catalog
        final PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();

        // as there might not be an AcroForm entry a null check is necessary
        if (acroForm != null) {
            acroForm.setNeedAppearances(false);

            // Retrieve an individual field and set its value.
            for (PDField field : acroForm.getFields()) {
                try {
                    String fieldValue = fieldMappings == null ? null : fieldMappings.get(field.getFullyQualifiedName());
                    if (!empty(fieldValue)) {
                        fieldValue = HandlebarsUtil.apply(handlebars, fieldValue, context);
                    }
                    if (field instanceof PDCheckBox) {
                        PDCheckBox box = (PDCheckBox) field;
                        if (!empty(fieldValue)) {
                            if (Boolean.valueOf(fieldValue)) {
                                box.check();
                            } else {
                                box.unCheck();
                            }
                        }

                    } else {
                        String formValue = field.getValueAsString();
                        if (formValue.equals(NULL_FORM_VALUE)) formValue = null;
                        if (empty(formValue) && field instanceof PDTextField) {
                            formValue = ((PDTextField) field).getDefaultValue();
                            if (formValue.equals(NULL_FORM_VALUE)) formValue = null;
                        }
                        if (empty(formValue)) formValue = fieldValue;
                        if (!empty(formValue)) {
                            formValue = HandlebarsUtil.apply(handlebars, formValue, context);
                            field.setValue(formValue);
                        }
                    }
                } catch (Exception e) {
                    die("merge: "+e, e);
                }
                field.setReadOnly(true);
                field.getCOSObject().setInt("Ff", 1);
            }
            acroForm.flatten();
        }

        // add images
        final Map<String, Object> imageInsertions = (Map<String, Object>) context.get("imageInsertions");
        if (!empty(imageInsertions)) {
            for (Object insertion : imageInsertions.values()) {
                insertImage(pdfDocument, insertion, Base64ImageInsertion.class);
            }
        }

        // add text
        final Map<String, Object> textInsertions = (Map<String, Object>) context.get("textInsertions");
        if (!empty(textInsertions)) {
            for (Object insertion : textInsertions.values()) {
                insertImage(pdfDocument, insertion, TextImageInsertion.class);
            }
        }

        final File output = temp(".pdf");

        // Save and close the filled out form.
        pdfDocument.save(output);
        pdfDocument.close();

        return new File[] { output };
    }

    protected static void insertImage(PDDocument pdfDocument, Object insert, Class<? extends ImageInsertion> clazz) throws IOException {
        final ImageInsertion insertion;
        if (insert instanceof ImageInsertion) {
            insertion = (ImageInsertion) insert;
        } else if (insert instanceof Map) {
            insertion = instantiate(clazz);
            insertion.init((Map<String, Object>) insert);
        } else {
            die("insertImage("+clazz.getSimpleName()+"): invalid object: "+insert);
            return;
        }

        // write image to temp file
        @Cleanup("delete") final File imageTemp = insertion.getImageFile();

        // open stream for writing inserted image
        final PDPage page = pdfDocument.getDocumentCatalog().getPages().get(insertion.getPage());
        final PDPageContentStream contentStream = new PDPageContentStream(pdfDocument, page, PDPageContentStream.AppendMode.APPEND, true);

        // draw image on page
        final PDImageXObject image = PDImageXObject.createFromFile(abs(imageTemp), pdfDocument);
        contentStream.drawImage(image, insertion.getX(), insertion.getY(), insertion.getWidth(), insertion.getHeight());
        contentStream.close();
    }
}
