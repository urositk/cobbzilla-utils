package org.cobbzilla.util.jdbc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class ResultSetBean {

    public static final ResultSetBean EMPTY = new ResultSetBean();

    @Getter private final ArrayList<Map<String, Object>> rows = new ArrayList<>();
    public boolean isEmpty () { return rows.isEmpty(); }

    public Map<String, Object> first () { return rows.get(0); }
    public int count () { return Integer.parseInt(rows.get(0).entrySet().iterator().next().getValue().toString()); }

    public ResultSetBean (ResultSet rs) throws SQLException {
        final ResultSetMetaData rsMetaData = rs.getMetaData();
        final int numColumns = rsMetaData.getColumnCount();
        while (rs.next()){
            final HashMap<String, Object> row = row2map(rs, rsMetaData, numColumns);
            rows.add(row);
        }
    }

    public static HashMap<String, Object> row2map(ResultSet rs) throws SQLException {
        final ResultSetMetaData rsMetaData = rs.getMetaData();
        final int numColumns = rsMetaData.getColumnCount();
        return row2map(rs, rsMetaData, numColumns);
    }

    public static HashMap<String, Object> row2map(ResultSet rs, ResultSetMetaData rsMetaData) throws SQLException {
        final int numColumns = rsMetaData.getColumnCount();
        return row2map(rs, rsMetaData, numColumns);
    }

    public static HashMap<String, Object> row2map(ResultSet rs, ResultSetMetaData rsMetaData, int numColumns) throws SQLException {
        final HashMap<String, Object> row = new HashMap<>(numColumns);
        for(int i=1; i<=numColumns; ++i){
            row.put(rsMetaData.getColumnName(i), rs.getObject(i));
        }
        return row;
    }

    public static List<String> getColumns(ResultSet rs, ResultSetMetaData rsMetaData) throws SQLException {
        int columnCount = rsMetaData.getColumnCount();
        final List<String> columns = new ArrayList<>(columnCount);
        for (int i=1; i<=columnCount; ++i) {
            columns.add(rsMetaData.getColumnName(i));
        }
        return columns;
    }

}
