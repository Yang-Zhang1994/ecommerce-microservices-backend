/**
 * Quartz driver delegate for PostgreSQL when JOB_DATA / BLOB columns use BYTEA.
 * StdJDBCDelegate uses ResultSet.getBlob(), which in PostgreSQL JDBC expects an OID
 * (Large Object); on BYTEA columns this causes "Bad value for type long : \x".
 * This delegate overrides blob reading to use getBytes() for bytea columns.
 */
package io.renren.modules.job.quartz;

import org.quartz.impl.jdbcjobstore.PostgreSQLDelegate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PostgreSQLByteaDelegate extends PostgreSQLDelegate {

    @Override
    protected Object getObjectFromBlob(ResultSet rs, String colName) throws ClassNotFoundException, IOException, SQLException {
        // PostgreSQL BYTEA columns must be read with getBytes(), not getBlob()
        byte[] bytes = rs.getBytes(colName);
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return ois.readObject();
        }
    }
}
