package org.cobbzilla.util.jdbc;

import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

import java.sql.SQLException;

@AllArgsConstructor
public class UncheckedSqlException extends RuntimeException {

    @Delegate private final SQLException sqlException;

}
