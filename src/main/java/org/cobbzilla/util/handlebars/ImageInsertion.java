package org.cobbzilla.util.handlebars;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.codec.binary.Base64InputStream;
import org.cobbzilla.util.string.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;

@NoArgsConstructor @Accessors(chain=true)
public class ImageInsertion {

    @Getter @Setter private int page = 0;
    @Getter @Setter private float x;
    @Getter @Setter private float y;
    @Getter @Setter private float width;
    @Getter @Setter private float height;
    @Getter @Setter private String image; // base64-encoded image data
    @Getter @Setter private String format;

    @JsonIgnore public InputStream getImageStream () {
        return new Base64InputStream(new ByteArrayInputStream(image.getBytes()));
    }

    public ImageInsertion (String spec) {
        for (String part : StringUtil.split(spec, ", ")) {
            final int eqPos = part.indexOf("=");
            if (eqPos == -1) die("invalid image insertion (missing '='): "+spec);
            if (eqPos == part.length()-1) die("invalid image insertion (no value): "+spec);
            final String key = part.substring(0, eqPos).trim();
            final String value = part.substring(eqPos+1).trim();
            switch (key) {
                case "page":   this.page   = Integer.parseInt(value); break;
                case "x":      this.x      = Float.parseFloat(value); break;
                case "y":      this.y      = Float.parseFloat(value); break;
                case "width":  this.width  = Float.parseFloat(value); break;
                case "height": this.height = Float.parseFloat(value); break;
                case "image":  this.image  = value; break;
                case "format": this.format = value; break;
                default: die("invalid parameter: "+key);
            }
        }
    }

}
