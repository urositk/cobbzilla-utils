package org.cobbzilla.util.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonUtil {

    public static final ObjectMapper FULL_MAPPER = new ObjectMapper()
            .configure(SerializationFeature.INDENT_OUTPUT, true);

    public static final ObjectWriter FULL_WRITER = FULL_MAPPER.writer();

    public static final ObjectMapper NOTNULL_MAPPER = FULL_MAPPER
            .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    public static final ObjectMapper PUBLIC_MAPPER = buildMapper();

    public static final ObjectWriter PUBLIC_WRITER = buildWriter(PUBLIC_MAPPER, PublicView.class);

    public static ObjectMapper buildMapper() {
        return new ObjectMapper()
                .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    public static ObjectWriter buildWriter(Class<? extends PublicView> view) {
        return buildMapper().writerWithView(view);
    }
    public static ObjectWriter buildWriter(ObjectMapper mapper, Class<? extends PublicView> view) {
        return mapper.writerWithView(view);
    }

    public static class PublicView {}

    public static String toJson (Object o) throws Exception {
        return JsonUtil.NOTNULL_MAPPER.writeValueAsString(o);
    }

    public static <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return JsonUtil.FULL_MAPPER.readValue(json, clazz);
    }

}
