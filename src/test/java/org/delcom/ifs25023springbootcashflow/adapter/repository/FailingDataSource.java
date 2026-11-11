package org.delcom.ifs25023springbootcashflow.adapter.repository;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/** DataSource palsu yang selalu gagal, untuk menguji penanganan SQLException. */
final class FailingDataSource implements DataSource {

    @Override
    public Connection getConnection() throws SQLException {
        throw new SQLException("database mati");
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new SQLException("database mati");
    }

    @Override
    public PrintWriter getLogWriter() {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
    }

    @Override
    public void setLoginTimeout(int seconds) {
    }

    @Override
    public int getLoginTimeout() {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("tidak didukung");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return false;
    }
}
