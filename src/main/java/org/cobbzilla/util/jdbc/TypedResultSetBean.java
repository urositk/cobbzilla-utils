package org.cobbzilla.util.jdbc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.reflect.ReflectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.cobbzilla.util.reflect.ReflectionUtil.instantiate;
import static org.cobbzilla.util.string.StringUtil.snakeCaseToCamelCase;

@Slf4j
public class TypedResultSetBean<T> extends ResultSetBean implements Iterable<T> {

    public TypedResultSetBean(ResultSet rs) throws SQLException { super(rs); }
    public TypedResultSetBean(PreparedStatement ps) throws SQLException { super(ps); }
    public TypedResultSetBean(Connection conn, String sql) throws SQLException { super(conn, sql); }

    @Getter(lazy=true) private final Class<T> rowType = ReflectionUtil.getFirstTypeParam(getClass());
    @Getter(lazy=true, value=AccessLevel.PRIVATE) private final List<T> typedRows = getTypedRows(getRowType());

    @Override public Iterator<T> iterator() { return new ArrayList<>(getTypedRows()).iterator(); }

    private List<T> getTypedRows(Class<T> clazz) {
        final List<T> typedRows = new ArrayList<>();
        for (Map<String, Object> row : getRows()) {
            final T thing = instantiate(clazz);
            for (String name : row.keySet()) {
                final String field = snakeCaseToCamelCase(name);
                try {
                    final Object value = row.get(name);
                    readField(thing, field, value);
                } catch (Exception e) {
                    log.warn("getTypedRows: error setting "+field+": "+e);
                }
            }
            typedRows.add(thing);
        }
        return typedRows;
    }

    protected void readField(T thing, String field, Object value) {
        if (value !=  null) ReflectionUtil.set(thing, field, value);
    }

}
