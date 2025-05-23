package cn.doraro.flexedge.core.station;

import cn.doraro.flexedge.core.Config;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StationLocSaver {
    static boolean forTest = false;
    private static HashMap<String, Object> name2saver = new HashMap<>();
    private static HashMap<String, Object> nor_name2saver = new HashMap<>();
    private String dbUrl = null;// "jdbc:sqlite:mqtt_persistence.db";
    private Connection conn;
    private String tableName = "";
    private Long savedNum = null;
    private int MAX_BUF_LEN = 100;
    private ArrayList<Item> bufferedItems = new ArrayList<>();
    private long lastPutDT = System.currentTimeMillis();

    private StationLocSaver(String prjname) throws SQLException {
        String fp = null;
        if (forTest)
            fp = "../data_dyn/station/rt_data_test/db_" + prjname + ".db";
        else
            fp = Config.getDataDynDirBase() + "station/rt_data/db_" + prjname + ".db";
        File f = new File(fp);
        if (!f.getParentFile().exists())
            f.getParentFile().mkdirs();

        dbUrl = "jdbc:sqlite:" + fp;
        tableName = "rtd_" + prjname;

        conn = DriverManager.getConnection(dbUrl);
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + "key TEXT PRIMARY KEY,"
                + "message BLOB)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        }
    }

    private StationLocSaver(String tp, String name) throws SQLException {
        String fp = null;
        if (forTest)
            fp = "../data_dyn/station/" + tp + "_test/db_" + name + ".db";
        else
            fp = Config.getDataDynDirBase() + "station/" + tp + "/db_" + name + ".db";
        File f = new File(fp);
        if (!f.getParentFile().exists())
            f.getParentFile().mkdirs();

        dbUrl = "jdbc:sqlite:" + fp;
        tableName = tp + "_" + name;

        conn = DriverManager.getConnection(dbUrl);
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + "key TEXT PRIMARY KEY,"
                + "message BLOB)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        }
    }

    public static StationLocSaver getSaver(String name) {
        Object s = name2saver.get(name);
        if (s != null) {
            if (s instanceof String)
                return null;

            return (StationLocSaver) s;
        }

        synchronized (StationLocSaver.class) {
            s = name2saver.get(name);
            if (s != null) {
                if (s instanceof String)
                    return null;

                return (StationLocSaver) s;
            }

            try {
                StationLocSaver sver = new StationLocSaver(name);
                name2saver.put(name, sver);
                return sver;
            } catch (Exception ee) {
                ee.printStackTrace();
                name2saver.put(name, "");
                return null;
            }

        }
    }

    static StationLocSaver getSaverNor(String name) {
        Object s = nor_name2saver.get(name);
        if (s != null) {
            if (s instanceof String)
                return null;

            return (StationLocSaver) s;
        }

        synchronized (StationLocSaver.class) {
            s = nor_name2saver.get(name);
            if (s != null) {
                if (s instanceof String)
                    return null;

                return (StationLocSaver) s;
            }

            try {
                StationLocSaver sver = new StationLocSaver("nor", name);
                nor_name2saver.put(name, sver);
                return sver;
            } catch (Exception ee) {
                ee.printStackTrace();
                nor_name2saver.put(name, "");
                return null;
            }
        }
    }

    public String getTableName() {
        return tableName;
    }

    public void close() throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

//	public Item get(String key) throws SQLException
//	{
//		String selectSQL = "SELECT message FROM " + tableName + " WHERE key = ?";
//		try (PreparedStatement pstmt = conn.prepareStatement(selectSQL))
//		{
//			pstmt.setString(1, key);
//			try (ResultSet rs = pstmt.executeQuery())
//			{
//				if (rs.next())
//				{
//					byte[] msg = rs.getBytes("message");
//					return new Item(key, msg);
//				}
//				return null;
//			}
//		}
//	}

    public int putBatch(List<Item> items, long max_len) throws SQLException {
        int n = items.size();
        if (n <= 0)
            return 0;

        try {
            long saven = getSavedNum();

            conn.setAutoCommit(false);
            for (Item item : items) {
                insert(item.key, item.msg);
            }

            saven += n;
            if (max_len > 0 && saven > max_len) {
                long deln = saven - max_len;
                if (deln > n)
                    deln = n;

                List<String> keys = getFirstKeys(deln);
                for (String k : keys) {
                    deleteByKey(k);
                }
                saven -= deln;
            }
            conn.commit();
            this.savedNum = saven;
            return items.size();
        } catch (SQLException ee) {
            conn.rollback();
            throw ee;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /**
     * 单个速度可能会很慢
     *
     * @param key
     * @param msg
     * @param max_len
     * @return
     * @throws SQLException
     */
    private void insert(String key, byte[] msg) throws SQLException {
        String insertSQL = "INSERT OR REPLACE INTO " + tableName + "(key, message) VALUES(?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, key);
            pstmt.setBytes(2, msg);
            pstmt.executeUpdate();
        }

//		if(bret)
//		{
//			if(n<0)
//				return bret ;
//
//			n ++ ;
//			this.savedNum = n ;
//
//			if(this.savedNum>max_len)
//			{//删除最最的一个
//
//			}
//		}

        //return bret ;
    }

    public Item getLastItem() throws SQLException {
        List<Item> rets = getLastItems(1);
        if (rets.size() <= 0)
            return null;
        return rets.get(0);
    }

    public List<Item> getLastItems(int num) throws SQLException {
        String selectSQL = "SELECT key,message FROM " + tableName + " order by key DESC LIMIT " + num;
        ArrayList<Item> rets = new ArrayList<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(selectSQL)) {
            while (rs.next()) {
                String key = rs.getString("key");
                byte[] msg = rs.getBytes("message");
                Item item = new Item(key, msg);
                rets.add(item);
            }
            return rets;
        }
    }

    public boolean deleteByKey(String key) throws SQLException {
        String deleteSQL = "DELETE FROM " + tableName + " WHERE key = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            pstmt.setString(1, key);
            return pstmt.executeUpdate() == 1;
        }
    }

    public int deleteBatchByItems(List<Item> items) throws SQLException {
        if (items == null || items.size() <= 0)
            return 0;

        ArrayList<String> keys = new ArrayList<>(items.size());
        for (Item item : items) {
            keys.add(item.key);
        }
        return deleteBatchByKeys(keys);
    }

    public int deleteBatchByKeys(List<String> keys) throws SQLException {
        if (keys == null || keys.size() <= 0)
            return 0;

        try {
            long saven = getSavedNum();
            conn.setAutoCommit(false);
            int del_cc = 0;
            for (String k : keys) {
                if (deleteByKey(k))
                    del_cc++;
            }

            saven -= del_cc;
            conn.commit();
            this.savedNum = saven;
            return del_cc;
        } catch (Exception ee) {
            conn.rollback();
            return 0;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public List<String> getFirstKeys(long num) throws SQLException {
        String selectSQL = "SELECT key FROM " + tableName + " order by key LIMIT " + num;
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(selectSQL)) {
            ArrayList<String> rets = new ArrayList<>();
            while (rs.next()) {
                rets.add(rs.getString("key"));
            }
            return rets;
        }
    }

    public List<Item> getFirstItems(int num) throws SQLException {
        String selectSQL = "SELECT key,message FROM " + tableName + " order by key LIMIT " + num;
        ArrayList<Item> rets = new ArrayList<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(selectSQL)) {
            while (rs.next()) {
                String key = rs.getString("key");
                byte[] msg = rs.getBytes("message");
                Item item = new Item(key, msg);
                rets.add(item);
            }
            return rets;
        }
    }

    public boolean containsKey(String key) throws SQLException {
        String selectSQL = "SELECT key FROM " + tableName + " WHERE key = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Item getItemByKey(String key) throws SQLException {
        String selectSQL = "SELECT key,message FROM " + tableName + " WHERE key = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    key = rs.getString("key");
                    byte[] msg = rs.getBytes("message");
                    return new Item(key, msg);
                }
                return null;
            }
        }
    }

    public void clear() throws SQLException {
        String clearSQL = "DELETE FROM " + tableName;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(clearSQL);
        }
    }

    public long getSavedNum() {
        if (savedNum != null)
            return savedNum;

        try {
            savedNum = countNum();
            return savedNum;
        } catch (SQLException e) {
            e.printStackTrace();
            // throw new SQLException(e);
            return -1;
        }
    }

    public long countNum() throws SQLException {
        String selectSQL = "SELECT count(*) FROM " + tableName;
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(selectSQL)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("no count result");
        }
    }

    public long RT_getSavedBufferedNum() {
        return this.getSavedNum() + bufferedItems.size();
    }

    public int RT_getBufferedNum() {
        return this.bufferedItems.size();
    }

    synchronized public void RT_putItemBuffered(String key, byte[] bs, long keep_max_len) {
        this.bufferedItems.add(new Item(key, bs));

        this.lastPutDT = System.currentTimeMillis();

        if (this.bufferedItems.size() >= MAX_BUF_LEN) {
            try {
                RT_flushBuffered(keep_max_len);
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    }

    /**
     * @param timeout
     */
    public void RT_checkFlush(long timeout) throws SQLException {
        if (System.currentTimeMillis() - this.lastPutDT < timeout)
            return;

        RT_flushBuffered(-1);
    }

    synchronized void RT_flushBuffered(long keep_max_len) throws SQLException {
        if (bufferedItems.size() <= 0)
            return;
        this.putBatch(bufferedItems, keep_max_len);
        bufferedItems = new ArrayList<>();
    }

    public static class Item {
        String key = null;

        byte[] msg;

        public Item(String key, byte[] msg) {
            this.key = key;
            this.msg = msg;
        }

        public String getKey() {
            return this.key;
        }

        public byte[] getMsg() {
            return this.msg;
        }
    }
}
