package org.cobbzilla.util.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Properties;

public class DebugPostgresqlDriver implements Driver {

    private static final Logger LOG = LoggerFactory.getLogger(DebugPostgresqlDriver.class);

    private static final String DEBUG_PREFIX = "debug:";
    private static final String POSTGRESQL_PREFIX = "jdbc:postgresql:";
    private static final String DRIVER_CLASS_NAME = "org.postgresql.Driver";

    static {
        try {
            java.sql.DriverManager.registerDriver(new DebugPostgresqlDriver());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Driver driver;

    public DebugPostgresqlDriver() {
        try {
            driver = (Driver) Class.forName(DRIVER_CLASS_NAME).newInstance();
        } catch (Exception e) {
            String msg = "Error instantiating driver: "+DRIVER_CLASS_NAME+": "+e;
            LOG.error(msg, e);
            throw new IllegalArgumentException(msg, e);
        }
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (url.startsWith(DEBUG_PREFIX)) {
            url = url.substring(DEBUG_PREFIX.length());
            if (url.startsWith(POSTGRESQL_PREFIX)) {
                return new DebugConnection(driver.connect(url, info));
            }
        }
        throw new IllegalArgumentException("can't connect: "+url);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return driver.acceptsURL(url);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return driver.getPropertyInfo(url, info);
    }

    @Override
    public int getMajorVersion() { return driver.getMajorVersion(); }

    @Override
    public int getMinorVersion() { return driver.getMinorVersion(); }

    @Override
    public boolean jdbcCompliant() { return driver.jdbcCompliant(); }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return driver.getParentLogger();
    }
}
