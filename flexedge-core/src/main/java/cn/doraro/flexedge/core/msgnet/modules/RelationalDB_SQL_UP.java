package cn.doraro.flexedge.core.msgnet.modules;

import cn.doraro.flexedge.core.msgnet.MNConn;
import cn.doraro.flexedge.core.msgnet.MNMsg;
import cn.doraro.flexedge.core.msgnet.MNNodeMid;
import cn.doraro.flexedge.core.msgnet.RTOut;
import cn.doraro.flexedge.core.store.SourceJDBC;
import cn.doraro.flexedge.core.store.gdb.connpool.DBConnPool;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class RelationalDB_SQL_UP extends MNNodeMid {
    @Override
    public String getTP() {
        return "r_db_sql_up";
    }

    @Override
    public String getTPTitle() {
        return "SQL Insert/Update";
    }

    @Override
    public String getColor() {
        return "#e7b686";
    }

    @Override
    public String getIcon() {
        return "\\uf1c0";
    }


    @Override
    public int getOutNum() {
        return 2;
    }

    @Override
    public String getOutColor(int idx) {
        if (idx == 1)
            return "red";
        return null;
    }

    @Override
    public boolean isParamReady(StringBuilder failedr) {
        return true;
    }

    @Override
    public JSONObject getParamJO() {
        return null;
    }

    @Override
    protected void setParamJO(JSONObject jo) {

    }

    private DBConnPool RT_getConnPool() {
        RelationalDB_M m = (RelationalDB_M) this.getOwnRelatedModule();
        if (m == null)
            return null;
        SourceJDBC sorjdbc = m.getSourceJDBC();
        if (sorjdbc == null)
            return null;
        return sorjdbc.getConnPool();
    }

    @Override
    protected RTOut RT_onMsgIn(MNConn in_conn, MNMsg msg) throws Exception {
        String sql = msg.getPayloadStr();
        if (sql == null || (sql = sql.trim()).equals(""))
            return null;
        DBConnPool cp = RT_getConnPool();
        if (cp == null)
            return null;

        Connection conn = null;
        try {
            RT_DEBUG_INF.fire("sql_up", sql);
            conn = cp.getConnection();
            int r = runUpSql(conn, sql);

            //RT_DEBUG_ERR.clear("sql_up");
            MNMsg outm = new MNMsg().asPayload(r);
            return RTOut.createOutIdx().asIdxMsg(0, outm);
        } catch (Exception ee) {
            RT_DEBUG_ERR.fire("sql_up", sql, ee);
            MNMsg outm = new MNMsg().asPayload(ee.getMessage());
            return RTOut.createOutIdx().asIdxMsg(1, outm);
        } finally {
            if (conn != null)
                cp.free(conn);
        }
    }

    private int runUpSql(Connection conn, String sql) throws SQLException {
        try (Statement st = conn.createStatement();) {
            return st.executeUpdate(sql);
        }
    }

    @Override
    public String RT_getInTitle() {
        return "Input SQL for insert/update";
    }

    @Override
    public String RT_getOutTitle(int idx) {
        if (idx == 0)
            return "Insert or Update row affected";
        else
            return "error out";
    }


}
