package org.cobbzilla.util.collection;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ComparisonOperator {

    lt ("<", "<", "-lt"),
    le ("<=", "<=", "-le"),
    eq ("=", "==", "-eq"),
    ge (">=", ">=", "-ge"),
    gt (">", ">", "-gt"),
    ne ("!=", "!=", "-ne");

    @Getter private String sql;
    @Getter private String java;
    @Getter private String shell;

    @JsonCreator public static ComparisonOperator fromString(String val) { return valueOf(val.toLowerCase()); }

}
