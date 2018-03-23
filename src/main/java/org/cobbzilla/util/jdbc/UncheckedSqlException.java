package org.cobbzilla.util.jdbc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.SQLException;

@NoArgsConstructor @AllArgsConstructor
public class UncheckedSqlException extends RuntimeException {

    @Getter @Setter private SQLException sqlException;

    @Override public String getMessage() {
        return sqlException != null ? sqlException.getMessage() : "(null SQLException)";
    }

}
