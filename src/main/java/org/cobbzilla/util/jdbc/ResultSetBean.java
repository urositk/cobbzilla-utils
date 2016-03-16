package org.cobbzilla.util.jdbc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class ResultSetBean {

    public static final ResultSetBean EMPTY = new ResultSetBean();

    @Getter private final ArrayList<Map<String, Object>> rows = new ArrayList<>();
    public boolean isEmpty () { return rows.isEmpty(); }

    public Map<String, Object> first () { return rows.get(0); }

    public ResultSetBean (ResultSet rs) throws SQLException {
        final ResultSetMetaData rsMetaData = rs.getMetaData();
        final int numColumns = rsMetaData.getColumnCount();
        while (rs.next()){
            final HashMap<String, Object> row = new HashMap<>(numColumns);
            for(int i=1; i<=numColumns; ++i){
                row.put(rsMetaData.getColumnName(i), rs.getObject(i));
            }
            rows.add(row);
        }
    }
}
