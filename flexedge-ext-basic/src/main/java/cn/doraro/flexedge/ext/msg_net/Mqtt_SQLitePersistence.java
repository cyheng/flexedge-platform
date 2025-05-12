// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.ext.msg_net;

import cn.doraro.flexedge.core.Config;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttPersistable;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import java.io.File;
import java.sql.*;
import java.util.Enumeration;
import java.util.Hashtable;

public class Mqtt_SQLitePersistence implements MqttClientPersistence {
    private String dbUrl;
    private Connection conn;
    private String tableName;

    Mqtt_SQLitePersistence(final String clientid) throws SQLException {
        this.dbUrl = null;
        this.tableName = "";
        final String fp = Config.getDataDynDirBase() + "msg_net/mqtt/db_" + clientid + ".db";
        final File f = new File(fp);
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        this.dbUrl = "jdbc:sqlite:" + fp;
        this.tableName = "tb_" + clientid;
        this.conn = DriverManager.getConnection(this.dbUrl);
        final String createTableSQL = "CREATE TABLE IF NOT EXISTS " + this.tableName + " (key TEXT PRIMARY KEY,message BLOB)";
        try (final Statement stmt = this.conn.createStatement()) {
            stmt.execute(createTableSQL);
        }
    }

    public String getTableName() {
        return this.tableName;
    }

    public void open(final String clientId, final String serverURI) throws MqttPersistenceException {
    }

    public void close() throws MqttPersistenceException {
    }

    public void put(final String key, final MqttPersistable message) throws MqttPersistenceException {
        final String insertSQL = "INSERT OR REPLACE INTO " + this.tableName + "(key, message) VALUES(?, ?)";
        try (final PreparedStatement pstmt = this.conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, key);
            pstmt.setBytes(2, message.getPayloadBytes());
            pstmt.executeUpdate();
        } catch (final SQLException e) {
            throw new MqttPersistenceException((Throwable) e);
        }
    }

    public MqttPersistable get(final String key) throws MqttPersistenceException {
        final String selectSQL = "SELECT message FROM " + this.tableName + " WHERE key = ?";
        try (final PreparedStatement pstmt = this.conn.prepareStatement(selectSQL)) {
            pstmt.setString(1, key);
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    final byte[] message = rs.getBytes("message");
                    final MqttPersistable mqttPersistable = (MqttPersistable) new MqttPersistable() {
                        public byte[] getPayloadBytes() {
                            return message;
                        }

                        public int getPayloadOffset() {
                            return 0;
                        }

                        public int getPayloadLength() {
                            return message.length;
                        }

                        public byte[] getHeaderBytes() {
                            return new byte[0];
                        }

                        public int getHeaderOffset() {
                            return 0;
                        }

                        public int getHeaderLength() {
                            return 0;
                        }
                    };
                    if (rs != null) {
                        rs.close();
                    }
                    if (pstmt != null) {
                        pstmt.close();
                    }
                    return (MqttPersistable) mqttPersistable;
                }
                return null;
            }
        } catch (final SQLException e) {
            throw new MqttPersistenceException((Throwable) e);
        }
    }

    public void remove(final String key) throws MqttPersistenceException {
        final String deleteSQL = "DELETE FROM " + this.tableName + " WHERE key = ?";
        try (final PreparedStatement pstmt = this.conn.prepareStatement(deleteSQL)) {
            pstmt.setString(1, key);
            pstmt.executeUpdate();
        } catch (final SQLException e) {
            throw new MqttPersistenceException((Throwable) e);
        }
    }

    public Enumeration<String> keys() throws MqttPersistenceException {
        final String selectSQL = "SELECT key FROM " + this.tableName;
        try (final Statement stmt = this.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(selectSQL)) {
            final Hashtable<String, String> keys = new Hashtable<String, String>();
            while (rs.next()) {
                keys.put(rs.getString("key"), rs.getString("key"));
            }
            return keys.keys();
        } catch (final SQLException e) {
            throw new MqttPersistenceException((Throwable) e);
        }
    }

    public boolean containsKey(final String key) throws MqttPersistenceException {
        final String selectSQL = "SELECT key FROM " + this.tableName + " WHERE key = ?";
        try (final PreparedStatement pstmt = this.conn.prepareStatement(selectSQL)) {
            pstmt.setString(1, key);
            try (final ResultSet rs = pstmt.executeQuery(selectSQL)) {
                return rs.next();
            }
        } catch (final SQLException e) {
            throw new MqttPersistenceException((Throwable) e);
        }
    }

    public void clear() throws MqttPersistenceException {
        final String clearSQL = "DELETE FROM " + this.tableName;
        try (final Statement stmt = this.conn.createStatement()) {
            stmt.execute(clearSQL);
        } catch (final SQLException e) {
            throw new MqttPersistenceException((Throwable) e);
        }
    }

    public int getSavedNum() {
        if (this.conn == null) {
            return -1;
        }
        try {
            if (this.conn.isClosed()) {
                return -1;
            }
        } catch (final Exception ee) {
            return -1;
        }
        final String selectSQL = "SELECT count(*) FROM " + this.tableName;
        try (final Statement stmt = this.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(selectSQL)) {
            if (rs.next()) {
                final int int1 = rs.getInt(1);
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                return int1;
            }
            return 0;
        } catch (final SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
