package cn.doraro.flexedge.core.msgnet.store.evt_alert;

import cn.doraro.flexedge.core.UAManager;
import cn.doraro.flexedge.core.UAPrj;
import cn.doraro.flexedge.core.msgnet.MNManager;
import cn.doraro.flexedge.core.msgnet.MNNet;
import cn.doraro.flexedge.core.msgnet.modules.RelationalDB_M;
import cn.doraro.flexedge.core.msgnet.modules.RelationalDB_Table;
import cn.doraro.flexedge.core.msgnet.nodes.NS_TagAlertTrigger;
import cn.doraro.flexedge.core.store.Source;
import cn.doraro.flexedge.core.store.SourceJDBC;
import cn.doraro.flexedge.core.store.StoreManager;
import cn.doraro.flexedge.core.store.gdb.DBResult;
import cn.doraro.flexedge.core.store.gdb.DBUtil;
import cn.doraro.flexedge.core.store.gdb.DataTable;
import cn.doraro.flexedge.core.store.gdb.connpool.DBConnPool;
import cn.doraro.flexedge.core.util.Convert;

import java.sql.Connection;
import java.util.List;

public class EvtAlertTb {
    UAPrj prj = null;
    MNNet net = null;
    NS_TagAlertTrigger nAlertT = null;
    RelationalDB_Table rdbTb = null;

    public EvtAlertTb(MNNet net, NS_TagAlertTrigger atn, RelationalDB_Table rdbtb) {
        this.net = net;
        this.nAlertT = atn;
        this.rdbTb = rdbtb;
        this.prj = net.getBelongTo().getBelongToPrj();
    }

    public static EvtAlertTb getByUID(String uid) {
        List<String> ss = Convert.splitStrWith(uid, "-");
        UAPrj prj = UAManager.getInstance().getPrjByName(ss.get(0));
        if (prj == null)
            return null;
        MNManager nmm = MNManager.getInstance(prj);
        return nmm.getEvtAlertTbByUID(uid);
    }

    public String getUID() {
        return this.prj.getName() + "-" + this.net.getName() + "-"
                + this.nAlertT.getId();
    }

    public NS_TagAlertTrigger getNodeAlertTrigger() {
        return this.nAlertT;
    }

    public RelationalDB_Table getNodeRDBTable() {
        return this.rdbTb;
    }

    public String getTableName() {
        return this.rdbTb.getTableName();
    }

    public MNNet getMNNet() {
        return this.net;
    }

    public UAPrj getPrj() {
        return this.prj;
    }

    public String getTitle() {
        return this.nAlertT.getTitle();
    }

    public SourceJDBC getSourceJDBC() {
        RelationalDB_M dbm = (RelationalDB_M) this.rdbTb.getOwnRelatedModule();
        String sorn = dbm.getSourceName();
        if (Convert.isNullOrEmpty(sorn))
            return null;
        Source sor = StoreManager.getSourceByName(sorn);
        if (sor == null || !(sor instanceof SourceJDBC))
            return null;
        return (SourceJDBC) sor;
    }

    public DBConnPool getConnPool() {
        SourceJDBC sj = getSourceJDBC();
        if (sj == null)
            return null;
        return sj.getConnPool();
    }


    public List<EvtAlertItem> listEvtAlertItems(int pageidx, int pagesize, int[] total_cc, StringBuilder failedr) throws Exception {
        if (pagesize > 0) {
            if (pageidx < 0)
                throw new IllegalArgumentException("invalid pageidx " + pageidx);
        }

        DBConnPool cp = this.getConnPool();
        if (cp == null) {
            failedr.append("no conn pool found");
            return null;
        }

        String tablen = rdbTb.getTableName();
        if (Convert.isNullOrEmpty(tablen)) {
            failedr.append("no table name found");
            return null;
        }

        Connection conn = null;
        try {
            conn = cp.getConnection();
            String sql = "select count(*) from " + tablen;
            DataTable dt = DBUtil.executeQuerySql(conn, sql);
            //DBResult res = GDB.executeSql(true, conn, sql);
            Number n = dt.getFirstColOfFirstRowNumber();
            total_cc[0] = n.intValue();

            if (pagesize <= 0)
                sql = String.format("select * from %s order by TriggerDT desc", tablen);
            else
                sql = String.format("select * from %s order by TriggerDT desc LIMIT %d OFFSET %d", tablen, pagesize,
                        pageidx * pagesize);
            dt = DBUtil.executeQuerySql(conn, sql);
            return DBResult.transTable2XORMObjList(EvtAlertItem.class, dt);
        } finally {
            if (conn != null)
                cp.free(conn);
        }
    }
}
