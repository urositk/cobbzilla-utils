package org.cobbzilla.util.handlebars;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.io.AbstractTemplateLoader;
import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.io.IOUtils;
import org.cobbzilla.util.collection.SingletonList;
import org.cobbzilla.util.http.HttpContentTypes;
import org.cobbzilla.util.io.FileResolver;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.io.PathListFileResolver;
import org.cobbzilla.util.reflect.ReflectionUtil;
import org.cobbzilla.util.string.LocaleUtil;
import org.cobbzilla.util.string.StringUtil;
import org.cobbzilla.util.time.TimeUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.regex.Pattern.quote;
import static org.cobbzilla.util.daemon.DaemonThreadFactory.fixedPool;
import static org.cobbzilla.util.daemon.ZillaRuntime.*;
import static org.cobbzilla.util.io.StreamUtil.loadResourceAsStream;
import static org.cobbzilla.util.io.StreamUtil.stream2string;
import static org.cobbzilla.util.json.JsonUtil.json;
import static org.cobbzilla.util.security.ShaUtil.sha256_hex;
import static org.cobbzilla.util.string.Base64.encodeBytes;
import static org.cobbzilla.util.string.Base64.encodeFromFile;
import static org.cobbzilla.util.string.StringUtil.*;

@AllArgsConstructor @Slf4j
public class HandlebarsUtil extends AbstractTemplateLoader {

    public static final char HB_START_CHAR = '{';
    public static final char HB_END_CHAR = '}';

    public static final String HB_START = StringUtils.repeat(HB_START_CHAR, 2);
    public static final String HB_END = StringUtils.repeat(HB_END_CHAR, 2);

    public static final String HB_LSTART = StringUtils.repeat(HB_START_CHAR, 3);
    public static final String HB_LEND = StringUtils.repeat(HB_END_CHAR, 3);

    public static final String DEFAULT_FLOAT_FORMAT = "%1$,.3f";

    private String sourceName = "unknown";

    public static Map<String, Object> apply(Handlebars handlebars, Map<String, Object> map, Map<String, Object> ctx) {
        return apply(handlebars, map, ctx, HB_START_CHAR, HB_END_CHAR);
    }

    public static Map<String, Object> apply(Handlebars handlebars, Map<String, Object> map, Map<String, Object> ctx, char altStart, char altEnd) {
        if (empty(map)) return map;
        final Map<String, Object> merged = new LinkedHashMap<>();
        final String hbStart = StringUtils.repeat(altStart, 2);
        final String hbEnd = StringUtils.repeat(altEnd, 2);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            final Object value = entry.getValue();
            if (value instanceof String) {
                final String val = (String) value;
                if (val.contains(hbStart) && val.contains(hbEnd)) {
                    merged.put(entry.getKey(), apply(handlebars, value.toString(), ctx, altStart, altEnd));
                } else {
                    merged.put(entry.getKey(), entry.getValue());
                }

            } else if (value instanceof Map) {
                // recurse
                merged.put(entry.getKey(), apply(handlebars, (Map<String, Object>) value, ctx, altStart, altEnd));

            } else {
                log.info("apply: ");
                merged.put(entry.getKey(), entry.getValue());
            }
        }
        return merged;
    }

    public static String apply(Handlebars handlebars, String value, Map<String, Object> ctx) {
        return apply(handlebars, value, ctx, (char) 0, (char) 0);
    }

    public static final String DUMMY_START3 = "~~~___~~~";
    public static final String DUMMY_START2 = "~~__~~";
    public static final String DUMMY_END3 = "___~~~___";
    public static final String DUMMY_END2 = "__~~__";
    public static String apply(Handlebars handlebars, String value, Map<String, Object> ctx, char altStart, char altEnd) {
        if (altStart != 0 && altEnd != 0 && (altStart != HB_START_CHAR && altEnd != HB_END_CHAR)) {
            final String s3 = StringUtils.repeat(altStart, 3);
            final String s2 = StringUtils.repeat(altStart, 2);
            final String e3 = StringUtils.repeat(altEnd, 3);
            final String e2 = StringUtils.repeat(altEnd, 2);
            // escape existing handlebars delimiters with dummy placeholders (we'll put them back later)
            value = value.replaceAll(quote(HB_LSTART), DUMMY_START3).replaceAll(HB_LEND, DUMMY_END3)
                    .replaceAll(quote(HB_START), DUMMY_START2).replaceAll(HB_END, DUMMY_END2)
                    // replace our custom start/end delimiters with handlebars standard ones
                    .replaceAll(quote(s3), HB_LSTART).replaceAll(quote(e3), HB_LEND)
                    .replaceAll(quote(s2), HB_START).replaceAll(quote(e2), HB_END);
            // run handlebars, then put the real handlebars stuff back (removing the dummy placeholders)
            value = apply(handlebars, value, ctx)
                    .replaceAll(DUMMY_START3, HB_LSTART).replaceAll(DUMMY_END3, HB_LEND)
                    .replaceAll(DUMMY_START2, HandlebarsUtil.HB_START).replaceAll(DUMMY_END2, HB_END);
            return value;
        }
        try {
            @Cleanup final StringWriter writer = new StringWriter(value.length());
            handlebars.compile(value).apply(ctx, writer);
            return writer.toString();
        } catch (HandlebarsException e) {
            final Throwable cause = e.getCause();
            if (cause != null && ((cause instanceof FileNotFoundException) || (cause instanceof RequiredVariableUndefinedException))) {
                log.error(e.getMessage()+": \""+value+"\"");
                throw e;
            }
            return die("apply("+value+"): "+e, e);
        } catch (Exception e) {
            return die("apply("+value+"): "+e, e);
        } catch (Error e) {
            log.warn("apply: "+e, e);
            throw e;
        }
    }

    /**
     * Using reflection, we find all public getters of a thing (and if the getter returns an object, find all
     * of its public getters, recursively and so on). We limit our results to those getters that have corresponding
     * setters: methods whose sole parameter is of a compatible type with the return type of the getter.
     * For each such property whose value is a String, we apply handlebars using the provided context.
     * @param handlebars the handlebars template processor
     * @param thing the object to operate upon
     * @param ctx the context to apply
     * @param <T> the return type
     * @return the thing, possibly with String-valued properties having been modified
     */
    public static <T> T applyReflectively(Handlebars handlebars, T thing, Map<String, Object> ctx) {
        return applyReflectively(handlebars, thing, ctx, HB_START_CHAR, HB_END_CHAR);
    }

    public static <T> T applyReflectively(Handlebars handlebars, T thing, Map<String, Object> ctx, char altStart, char altEnd) {
        for (Method getterCandidate : thing.getClass().getMethods()) {

            if (!getterCandidate.getName().startsWith("get")) continue;
            if (!canApplyReflectively(getterCandidate.getReturnType())) continue;

            final String setterName = ReflectionUtil.setterForGetter(getterCandidate.getName());
            for (Method setterCandidate : thing.getClass().getMethods()) {
                if (!setterCandidate.getName().equals(setterName)
                        || setterCandidate.getParameterTypes().length != 1
                        || !setterCandidate.getParameterTypes()[0].isAssignableFrom(getterCandidate.getReturnType())) {
                    continue;
                }
                try {
                    final Object value = getterCandidate.invoke(thing, (Object[]) null);
                    if (value == null) break;
                    if (value instanceof String) {
                        if (value.toString().contains("" + altStart + altStart)) {
                            setterCandidate.invoke(thing, apply(handlebars, (String) value, ctx, altStart, altEnd));
                        }

                    } else if (value instanceof JsonNode) {
                        setterCandidate.invoke(thing, json(apply(handlebars, json(value), ctx, altStart, altEnd), JsonNode.class));

                    } else if (value instanceof Map) {
                        setterCandidate.invoke(thing, apply(handlebars, (Map<String, Object>) value, ctx, altStart, altEnd));

                    } else {
                        // recurse
                        setterCandidate.invoke(thing, applyReflectively(handlebars, value, ctx, altStart, altEnd));
                    }
                } catch (HandlebarsException e) {
                    throw e;

                } catch (Exception e) {
                    // no setter for getter
                    log.warn("applyReflectively: " + e);
                }
            }
        }
        return thing;
    }

    private static boolean canApplyReflectively(Class<?> returnType) {
        if (returnType.equals(String.class)) return true;
        try {
            return !(returnType.isPrimitive() || (returnType.getPackage() != null && returnType.getPackage().getName().equals("java.lang")));
        } catch (NullPointerException npe) {
            log.warn("canApplyReflectively("+returnType+"): "+npe);
            return false;
        }
    }

    @Override public TemplateSource sourceAt(String source) throws IOException {
        return new StringTemplateSource(sourceName, source);
    }

    public static final CharSequence EMPTY_SAFE_STRING = "";

    private static final AtomicReference<ContextMessageSender> messageSender = new AtomicReference<>();

    public static void setMessageSender(ContextMessageSender sender) {
        synchronized (messageSender) {
            final ContextMessageSender current = messageSender.get();
            if (current != null && current != sender && !current.equals(sender)) die("setMessageSender: already set to "+current);
            messageSender.set(sender);
        }
    }

    public static void registerUtilityHelpers (final Handlebars hb) {
        hb.registerHelper("exists", (src, options) -> empty(src) ? null : options.apply(options.fn));

        hb.registerHelper("not_exists", (src, options) -> !empty(src) ? null : options.apply(options.fn));

        hb.registerHelper("sha256", (src, options) -> {
            if (empty(src)) return "";
            src = apply(hb, src.toString(), (Map<String, Object>) options.context.model());
            src = sha256_hex(src.toString());
            return new Handlebars.SafeString(src.toString());
        });

        hb.registerHelper("format_float", (val, options) -> {
            if (empty(val)) return "";
            if (options.params.length > 2) return die("format_float: too many parameters. Usage: {{format_float expr [format] [locale]}}");
            final String format = options.params.length > 0 && !empty(options.param(0)) ? options.param(0) : DEFAULT_FLOAT_FORMAT;
            final Locale locale = LocaleUtil.fromString(options.params.length > 1 && !empty(options.param(1)) ? options.param(1) : null);

            val = apply(hb, val.toString(), (Map<String, Object>) options.context.model());
            val = String.format(locale, format, Double.valueOf(val.toString()));
            return new Handlebars.SafeString(val.toString());
        });

        hb.registerHelper("json", (src, options) -> {
            if (empty(src)) return "";
            return new Handlebars.SafeString(json(src));
        });

        hb.registerHelper("context", (src, options) -> {
            if (empty(src)) return "";
            if (options.params.length > 0) return die("context: too many parameters. Usage: {{context [recipient]}}");
            final String ctxString = options.context.toString();
            final String recipient = src.toString();
            final String subject = options.params.length > 1 ? options.param(0) : null;
            sendContext(recipient, subject, ctxString,HttpContentTypes.TEXT_PLAIN);
            return new Handlebars.SafeString(ctxString);
        });

        hb.registerHelper("context_json", (src, options) -> {
            if (empty(src)) return "";
            try {
                if (options.params.length > 0) return die("context: too many parameters. Usage: {{context [recipient]}}");
                final String json = json(options.context.model());
                final String recipient = src.toString();
                final String subject = options.params.length > 1 ? options.param(0) : null;
                sendContext(recipient, subject, json, HttpContentTypes.APPLICATION_JSON);
                return new Handlebars.SafeString(json);
            } catch (Exception e) {
                return new Handlebars.SafeString("Error calling json(options.context): "+e.getClass()+": "+e.getMessage());
            }
        });

        hb.registerHelper("required", (src, options) -> {
            if (src == null) throw new RequiredVariableUndefinedException("required: undefined variable");
            return new Handlebars.SafeString(src.toString());
        });

        hb.registerHelper("safe_name", (src, options) -> {
            if (empty(src)) return "";
            return new Handlebars.SafeString(safeSnakeName(src.toString()));
        });

        hb.registerHelper("urlEncode", (src, options) -> {
            if (empty(src)) return "";
            src = apply(hb, src.toString(), (Map<String, Object>) options.context.model());
            src = urlEncode(src.toString());
            return new Handlebars.SafeString(src.toString());
        });

        hb.registerHelper("lastElement", (thing, options) -> {
            if (thing == null) return null;
            final Iterator iter = getIterator(thing);
            final String path = options.param(0);
            Object lastElement = null;
            while (iter.hasNext()) {
                lastElement = iter.next();
            }
            final Object val = ReflectionUtil.get(lastElement, path);
            if (val != null) return new Handlebars.SafeString(""+val);
            return EMPTY_SAFE_STRING;
        });

        hb.registerHelper("find", (thing, options) -> {
            if (thing == null) return null;
            final Iterator iter = getIterator(thing);
            final String path = options.param(0);
            final String arg = options.param(1);
            final String output = options.param(2);
            while (iter.hasNext()) {
                final Object item = iter.next();
                try {
                    final Object val = ReflectionUtil.get(item, path);
                    if (val != null && val.equals(arg)) return new Handlebars.SafeString(""+ReflectionUtil.get(item, output));
                } catch (Exception e) {
                    log.warn("find: "+e);
                }
            }
            return EMPTY_SAFE_STRING;
        });

        hb.registerHelper("compare", (val1, options) -> {
            final String operator = options.param(0);
            final Object val2 = getComparisonArgParam(options);
            final Comparable v1 = cval(val1);
            final Object v2 = cval(val2);
            return (v1 == null && v2 == null) || (v1 != null && compare(operator, v1, v2)) ? options.fn(options) : options.inverse(options);
        });

        hb.registerHelper("string_compare", (val1, options) -> {
            final String operator = options.param(0);
            final Object val2 = getComparisonArgParam(options);
            final String v1 = val1 == null ? null : val1.toString();
            final String v2 = val2 == null ? null : val2.toString();
            return compare(operator, v1, v2) ? options.fn(options) : options.inverse(options);
        });

        hb.registerHelper("long_compare", (val1, options) -> {
            final String operator = options.param(0);
            final Object val2 = getComparisonArgParam(options);
            final Long v1 = val1 == null ? null : Long.valueOf(val1.toString());
            final Long v2 = val2 == null ? null : Long.valueOf(val2.toString());
            return compare(operator, v1, v2) ? options.fn(options) : options.inverse(options);
        });

        hb.registerHelper("double_compare", (val1, options) -> {
            final String operator = options.param(0);
            final Object val2 = getComparisonArgParam(options);
            final Double v1 = val1 == null ? null : Double.valueOf(val1.toString());
            final Double v2 = val2 == null ? null : Double.valueOf(val2.toString());
            return compare(operator, v1, v2) ? options.fn(options) : options.inverse(options);
        });

        hb.registerHelper("big_compare", (val1, options) -> {
            final String operator = options.param(0);
            final Object val2 = getComparisonArgParam(options);
            final BigDecimal v1 = val1 == null ? null : big(val1.toString());
            final BigDecimal v2 = val2 == null ? null : big(val2.toString());
            return compare(operator, v1, v2) ? options.fn(options) : options.inverse(options);
        });

        hb.registerHelper("expr", (val1, options) -> {
            final String operator = options.param(0);
            final Object val2 = getComparisonArgParam(options);
            final String v1 = val1.toString();
            final String v2 = val2.toString();

            final BigDecimal result;
            switch (operator) {
                case "+": result = big(v1).add(big(v2)); break;
                case "-": result = big(v1).subtract(big(v2)); break;
                case "*": result = big(v1).multiply(big(v2)); break;
                case "/": result = big(v1).divide(big(v2), BigDecimal.ROUND_HALF_EVEN); break;
                case "%": result = big(v1).remainder(big(v2)).abs(); break;
                case "^": result = big(v1).pow(big(v2).intValue()); break;
                default: return die("expr: invalid operator: "+operator);
            }

            // can't use trigraph (?:) operator here, if we do then for some reason rval always ends up as a double
            final Number rval;
            if (v1.contains(".") || v2.contains(".")) {
                rval = result.doubleValue();
            } else {
                rval = result.intValue();
            }
            return new Handlebars.SafeString(rval.toString());
        });

        hb.registerHelper("truncate", (Helper<Integer>) (max, options) -> {
            final String val = options.param(0, " ");
            if (empty(val)) return "";
            if (max == -1 || max >= val.length()) return val;
            return new Handlebars.SafeString(val.substring(0, max));
        });

        hb.registerHelper("truncate_and_url_encode", (Helper<Integer>) (max, options) -> {
            final String val = options.param(0, " ");
            if (empty(val)) return "";
            if (max == -1 || max >= val.length()) return simpleUrlEncode(val);
            return new Handlebars.SafeString(simpleUrlEncode(val.substring(0, max)));
        });

        hb.registerHelper("truncate_and_double_url_encode", (Helper<Integer>) (max, options) -> {
            final String val = options.param(0, " ");
            if (empty(val)) return "";
            if (max == -1 || max >= val.length()) return simpleUrlEncode(simpleUrlEncode(val));
            return new Handlebars.SafeString(simpleUrlEncode(simpleUrlEncode(val.substring(0, max))));
        });

        hb.registerHelper("length", (thing, options) -> {
            if (empty(thing)) return "0";
            if (thing.getClass().isArray()) return ""+((Object[]) thing).length;
            if (thing instanceof Collection) return ""+((Collection) thing).size();
            if (thing instanceof ArrayNode) return ""+((ArrayNode) thing).size();
            return "";
        });

        hb.registerHelper("adjust_rate", (thing, options) -> {
            if (empty(thing) || (int) thing == 0) return formatDollarsAndCentsPlain(longDollarVal(0));

            if (empty(options)) return formatDollarsAndCentsPlain(longDollarVal(thing));

            final Integer adjustment = options.param(0);
            if (adjustment == null || adjustment == 0) return formatDollarsAndCentsPlain(longDollarVal(thing));

            final int result = (big((int) thing)).add(big(adjustment)).intValue();
            return formatDollarsAndCentsPlain(longDollarVal(result));
        });

    }

    public static Object getComparisonArgParam(Options options) {
        if (options.params.length <= 1) return die("getComparisonArgParam: missing argument");
        return options.param(1);
    }

    public static String getEmailRecipient(Handlebars hb, Options options, int index) {
        return options.params.length > index && !empty(options.param(index))
                            ? apply(hb, options.param(index).toString(), (Map<String, Object>) options.context.model())
                            : null;
    }

    private static final ExecutorService contextSender = fixedPool(10);

    public static void sendContext(String recipient, String subject, String message, String contentType) {
        contextSender.submit(() -> {
            if (!empty(recipient) && !empty(message)) {
                synchronized (messageSender) {
                    final ContextMessageSender sender = messageSender.get();
                    if (sender != null) {
                        try {
                            sender.send(recipient, subject, message, contentType);
                        } catch (Exception e) {
                            log.error("context: error sending message: " + e, e);
                        }
                    }
                }
            }
        });
    }

    private static Iterator getIterator(Object thing) {
        if (thing instanceof Collection) {
            return ((Collection) thing).iterator();
        } else if (thing instanceof Map) {
            return ((Map) thing).values().iterator();
        } else if (Object[].class.isAssignableFrom(thing.getClass())) {
            return new ArrayIterator(thing);
        } else {
            return die("find: invalid argument type "+thing.getClass().getName());
        }
    }

    private static Comparable cval(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return (Comparable) v;
        if (v instanceof String) {
            final String s = v.toString();
            try {
                return Long.parseLong(s);
            } catch (Exception e) {
                try {
                    return big(s);
                } catch (Exception e2) {
                    return s;
                }
            }
        } else {
            return die("don't know to compare objects of class "+v.getClass());
        }
    }

    public static <T> boolean compare(String operator, Comparable<T> v1, T v2) {
        if (v1 == null) return v2 == null;
        if (v2 == null) return false;
        boolean result;
        final List<String> parts;
        switch (operator) {
            case "==":  result =  v1.equals(v2); break;
            case "!=":  result = !v1.equals(v2); break;
            case ">":   result = v1.compareTo(v2) > 0; break;
            case ">=":  result = v1.compareTo(v2) >= 0; break;
            case "<":   result = v1.compareTo(v2) < 0; break;
            case "<=":  result = v1.compareTo(v2) <= 0; break;
            case "in":
                parts = StringUtil.split(v2.toString(), ", \n\t");
                for (String part : parts) {
                    if (v1.equals(part)) return true;
                }
                return false;
            case "not_in":
                parts = StringUtil.split(v2.toString(), ", \n\t");
                for (String part : parts) {
                    if (v1.equals(part)) return false;
                }
                return true;
            default: result = false;
        }
        return result;
    }

    public static void registerCurrencyHelpers(Handlebars hb) {
        hb.registerHelper("dollarsNoSign", (src, options) -> {
            if (empty(src)) return "";
            return new Handlebars.SafeString(formatDollarsNoSign(longDollarVal(src)));
        });

        hb.registerHelper("dollarsWithSign", (src, options) -> {
            if (empty(src)) return "";
            return new Handlebars.SafeString(formatDollarsWithSign(longDollarVal(src)));
        });

        hb.registerHelper("dollarsAndCentsNoSign", (src, options) -> {
            if (empty(src)) return "";
            return new Handlebars.SafeString(formatDollarsAndCentsNoSign(longDollarVal(src)));
        });

        hb.registerHelper("dollarsAndCentsWithSign", (src, options) -> {
            if (empty(src)) return "";
            return new Handlebars.SafeString(formatDollarsAndCentsWithSign(longDollarVal(src)));
        });

        hb.registerHelper("dollarsAndCentsPlain", (src, options) -> {
            if (empty(src)) return "";
            return new Handlebars.SafeString(formatDollarsAndCentsPlain(longDollarVal(src)));
        });
    }

    @Getter @Setter private static String defaultTimeZone = "US/Eastern";

    private abstract static class DateHelper implements Helper<Object> {

        protected DateTimeZone getTimeZone (Options options) { return getTimeZone(options, 0); }

        protected DateTimeZone getTimeZone (Options options, int index) {
            final String timeZoneName = options.param(index, getDefaultTimeZone());
            try {
                return DateTimeZone.forID(timeZoneName);
            } catch (Exception e) {
                return die("date_short: invalid timezone: "+timeZoneName);
            }
        }

        protected long zonedTimestamp (Object src, Options options) { return zonedTimestamp(src, options, 0); }

        protected long zonedTimestamp (Object src, Options options, int index) {
            if (empty(src)) src = "now";
            final DateTimeZone timeZone = getTimeZone(options, index);
            return longVal(src, timeZone);
        }

        protected CharSequence print (DateTimeFormatter formatter, Object src, Options options) {
            return new Handlebars.SafeString(formatter.print(new DateTime(zonedTimestamp(src, options),
                                                                          getTimeZone(options))));
        }
    }

    public static void registerDateHelpers(Handlebars hb) {

        hb.registerHelper("date_format", new DateHelper() {
            public CharSequence apply(Object src, Options options) {
                final DateTimeFormatter formatter = DateTimeFormat.forPattern(options.param(0));
                return new Handlebars.SafeString(formatter.print(new DateTime(zonedTimestamp(src, options, 1),
                        getTimeZone(options, 1))));
            }
        });

        hb.registerHelper("date_short", new DateHelper() {
            public CharSequence apply(Object src, Options options) {
                return print(TimeUtil.DATE_FORMAT_MMDDYYYY, src, options);
            }
        });

        hb.registerHelper("date_yyyy_mm_dd", new DateHelper() {
            public CharSequence apply(Object src, Options options) {
                return print(TimeUtil.DATE_FORMAT_YYYY_MM_DD, src, options);
            }
        });

        hb.registerHelper("date_mmm_dd_yyyy", new DateHelper() {
            public CharSequence apply(Object src, Options options) {
                return print(TimeUtil.DATE_FORMAT_MMM_DD_YYYY, src, options);
            }
        });

        hb.registerHelper("date_long", new DateHelper() {
            public CharSequence apply(Object src, Options options) {
                return print(TimeUtil.DATE_FORMAT_MMMM_D_YYYY, src, options);
            }
        });

        hb.registerHelper("timestamp", new DateHelper() {
            public CharSequence apply(Object src, Options options) {
                return new Handlebars.SafeString(Long.toString(zonedTimestamp(src, options)));
            }
        });
    }

    private static long longVal(Object src, DateTimeZone timeZone) {
        return longVal(src, timeZone, true);
    }

    private static long longVal(Object src, DateTimeZone timeZone, boolean tryAgain) {
        if (src == null) return now();
        String srcStr = src.toString().trim();

        if (srcStr.equals("") || srcStr.equals("0") || srcStr.equals("now")) return now();

        if (srcStr.startsWith("now")) {
            // Multiple periods may be added to the original timestamp (separated by comma), but in the correct order.
            final String[] splitSrc = srcStr.substring(3).split(",");
            DateTime result = new DateTime(now(), timeZone).withTimeAtStartOfDay();
            for (String period : splitSrc) {
                int sign = 1;
                if (period.startsWith("-")) {
                    sign = -1;
                }
                result = result.plus(Period.parse(period, TimeUtil.PERIOD_FORMATTER).multipliedBy(sign));
            }
            return result.getMillis();
        }

        if (!tryAgain) return die("longVal: unparseable long: "+src);

        try {
            return ((Number) src).longValue();
        } catch (Exception e) {
            // try to parse it in different formats
            Object t = TimeUtil.parse(src.toString());
            return longVal(t, timeZone, false);
        }
    }

    public static long longDollarVal(Object src) {
        final Long val = ReflectionUtil.toLong(src);
        return val == null ? 0 : val;
    }

    public static final String CLOSE_XML_DECL = "?>";

    public static void registerXmlHelpers(final Handlebars hb) {
        hb.registerHelper("strip_xml_declaration", (src, options) -> {
            if (empty(src)) return "";
            String xml = src.toString().trim();
            if (xml.startsWith("<?xml")) {
                final int closeDecl = xml.indexOf(CLOSE_XML_DECL);
                if (closeDecl != -1) {
                    xml = xml.substring(closeDecl + CLOSE_XML_DECL.length()).trim();
                }
            }
            return new Handlebars.SafeString(xml);
        });
    }

    public static void registerJurisdictionHelpers(final Handlebars hb, JurisdictionResolver jurisdictionResolver) {
        hb.registerHelper("us_state", (src, options) -> {
            if (empty(src)) return "";
            return new Handlebars.SafeString(jurisdictionResolver.usState(src.toString()));
        });
        hb.registerHelper("us_zip", (src, options) -> {
            if (empty(src)) return "";
            return new Handlebars.SafeString(jurisdictionResolver.usZip(src.toString()));
        });
    }

    public static final String DEFAULT_FILE_RESOLVER = "_";
    private static final Map<String, FileResolver> fileResolverMap = new HashMap<>();

    public static void setFileIncludePath(String path) { setFileIncludePaths(DEFAULT_FILE_RESOLVER, new SingletonList<>(path)); }

    public static void setFileIncludePaths(Collection<String> paths) { setFileIncludePaths(DEFAULT_FILE_RESOLVER, paths); }

    public static void setFileIncludePaths(String name, Collection<String> paths) {
        fileResolverMap.put(name, new PathListFileResolver(paths));
    }

    @AllArgsConstructor
    private static class FileLoaderHelper implements Helper<String> {

        private boolean isBase64EncoderOn;

        @Override public CharSequence apply(String filename, Options options) throws IOException {
            if (empty(filename)) return EMPTY_SAFE_STRING;

            final String include = options.get("includePath", DEFAULT_FILE_RESOLVER);
            final FileResolver fileResolver = fileResolverMap.get(include);
            if (fileResolver == null) return die("apply: no file resolve found for includePath="+include);

            final boolean escapeSpecialChars = options.get("escape", false);

            File f = fileResolver.resolve(filename);
            if (f == null && filename.startsWith(File.separator)) {
                // looks like an absolute path, try the filesystem
                f = new File(filename);
                if (!f.exists() || !f.canRead()) f = null;
            }

            if (f == null) {
                // try classpath
                try {
                    String content = isBase64EncoderOn
                            ? encodeBytes(IOUtils.toByteArray(loadResourceAsStream(filename)))
                            : stream2string(filename);
                    if (escapeSpecialChars) {
                        content = new String(JsonStringEncoder.getInstance().quoteAsString(content));
                    }
                    return new Handlebars.SafeString(content);
                } catch (Exception e) {
                    throw new FileNotFoundException("Cannot find readable file " + filename + ", resolver: " + fileResolver);
                }
            }

            try {
                String string = isBase64EncoderOn ? encodeFromFile(f) : FileUtil.toString(f);
                if (escapeSpecialChars) string = new String(JsonStringEncoder.getInstance().quoteAsString(string));
                return new Handlebars.SafeString(string);
            } catch (IOException e) {
                return die("Cannot read file from: " + f, e);
            }
        }
    }

    public static void registerFileHelpers(final Handlebars hb) {
        hb.registerHelper("rawImagePng", (src, options) -> {
            if (empty(src)) return "";

            final String include = options.get("includePath", DEFAULT_FILE_RESOLVER);
            final FileResolver fileResolver = fileResolverMap.get(include);
            if (fileResolver == null) return die("rawImagePng: no file resolve found for includePath="+include);

            final File f = fileResolver.resolve(src.toString());
            String imgSrc = (f == null) ? src.toString() : f.getAbsolutePath();

            final Object width = options.get("width");
            final String widthAttr = empty(width) ? "" : "width=\"" + width + "\" ";
            return new Handlebars.SafeString(
                    "<img " + widthAttr + "src=\"data:image/png;base64," + imgSrc + "\"/>");
        });

        hb.registerHelper("base64File", new FileLoaderHelper(true));
        hb.registerHelper("textFile", new FileLoaderHelper(false));
    }
}
