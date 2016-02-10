package org.cobbzilla.util.graphics;

import lombok.NoArgsConstructor;

import java.util.HashMap;

@NoArgsConstructor
public class ImageTransformConfig extends HashMap<String, String> {

    private static final int DEFAULT_WIDTH = 200;
    private static final int DEFAULT_HEIGHT = 200;

    public static final String PARAM_WIDTH = "w";
    public static final String PARAM_HEIGHT = "h";

    public ImageTransformConfig (String config) {
        for (String param : config.split("-")) {
            String[] args = param.split("_");
            put(args[0], args[1]);
        }
    }

    public int getInt(String param, int defaultValue) {
        return containsKey(param) ? Integer.parseInt(get(param)) : defaultValue;
    }

    public int getWidth () { return getInt(PARAM_WIDTH, DEFAULT_WIDTH); }
    public int getHeight () { return getInt(PARAM_HEIGHT, DEFAULT_HEIGHT); }

    @Override public String toString () {
        return PARAM_WIDTH +"_"+getWidth()+"-"+
               PARAM_HEIGHT+"_"+getHeight();
    }
}
