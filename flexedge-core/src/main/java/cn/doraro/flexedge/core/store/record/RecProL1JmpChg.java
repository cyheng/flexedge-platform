package cn.doraro.flexedge.core.store.record;

import cn.doraro.flexedge.core.store.Source;
import cn.doraro.flexedge.core.store.SourceJDBC;
import cn.doraro.flexedge.core.store.gdb.DBUtil;
import cn.doraro.flexedge.core.store.gdb.autofit.JavaColumnInfo;
import cn.doraro.flexedge.core.store.gdb.autofit.JavaForeignKeyInfo;
import cn.doraro.flexedge.core.store.gdb.autofit.JavaTableInfo;
import cn.doraro.flexedge.core.store.gdb.connpool.DBConnPool;
import cn.doraro.flexedge.core.store.gdb.xorm.XORMProperty;
import cn.doraro.flexedge.core.store.tssdb.TSSSavePK;
import cn.doraro.flexedge.core.util.ILang;
import cn.doraro.flexedge.core.util.xmldata.XmlVal;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecProL1JmpChg extends RecProL1 implements ILang {
    public static final String TP = "jmpchg";

    public static final List<RecValStyle> FIT_VAL_STYLES = Arrays.asList(RecValStyle.discrete);
    JmpTP jmpTp = JmpTP.off_to_on;
    private JavaTableInfo jti = null;
    private SourceJDBC sorJDBC = null;
    private DBConnPool connPool = null;

    public JmpTP getJmpTP() {
        return jmpTp;
    }

    @Override
    public String getTp() {
        return TP;
    }

    protected RecPro newInstance() {
        return new RecProL1JmpChg();
    }

    public String getTpTitle() {
        return g(TP + "_tt");
    }

    public String getTpDesc() {
        return g(TP + "_desc");
    }

    @Override
    public List<RecValStyle> getSupportedValStyle() {
        return FIT_VAL_STYLES;
    }

    @Override
    public JSONObject toJO() {
        JSONObject jo = super.toJO();
        jo.put("jmptp", jmpTp.getVal());
        return jo;
    }

    @Override
    public boolean fromJO(JSONObject jo, StringBuilder failedr) {
        if (!super.fromJO(jo, failedr))
            return false;

        this.jmpTp = JmpTP.valOfInt(jo.optInt("jmptp", -1));
        if (this.jmpTp == null) {
            failedr.append("no JmpTP pn=jmptp");
            return false;
        }


        return true;
    }

    // --------------------- rt init

    @Override
    public List<RecShower> getSupportedShowers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean RT_initSaver(StringBuilder failedr) {
        //连接数据源，初始化表格，或读取必要的历史数据以支持继续运行

        Source sor = this.getSaverSource();
        if (sor == null) {
            failedr.append("no saver source found");
            return false;
        }
        if (!(sor instanceof SourceJDBC)) {
            failedr.append("not jdbc source,it may be support later");
            return false;
        }

        sorJDBC = (SourceJDBC) sor;
        if (!sorJDBC.checkConn(failedr))
            return false;

        jti = getJTI();

        connPool = sorJDBC.getConnPool();
        try {
            DBUtil.createOrUpTable(connPool, jti);
            return true;
        } catch (Exception ee) {
            ee.printStackTrace();
            return false;
        }
    }

    @Override
    protected boolean RT_initPro(StringBuilder failedr) {

        return false;
    }

    private JavaTableInfo getJTI()// throws Exception
    {
        String tablen = calTableName(null);

        ArrayList<JavaColumnInfo> norcols = new ArrayList<JavaColumnInfo>();
        JavaColumnInfo pkcol = null;
        ArrayList<JavaForeignKeyInfo> fks = new ArrayList<JavaForeignKeyInfo>();

        // pkcol = new JavaColumnInfo("StartDT",false,
        // XmlVal.XmlValType.vt_int64, -1,
        // false, false,"", false,-1, "",false,false);

        norcols.add(new JavaColumnInfo("TagIdx", true, XmlVal.XmlValType.vt_int32, 10, true, false, "TagIdx_idx", false,
                -1, "", false, false));

        // 记录时间段起始点
        norcols.add(new JavaColumnInfo("DT", false, XmlVal.XmlValType.vt_int64, -1, true, false, "DT_idx",
                false, -1, "", false, false));

        norcols.add(new JavaColumnInfo("JmpVal", false, XmlVal.XmlValType.vt_int16, -1, false, false, "", false, -1, "",
                false, false));


        return new JavaTableInfo(tablen, pkcol, norcols, fks);
    }

    @Override
    protected boolean RT_onTagSegsSaved(TSSSavePK savepk) throws Exception {
        return false;
    }

    public List<RowOb> readLastsGroupByTag() throws Exception {
        String sql = "select max(DT) as MAX_DT,* from " + jti.getTableName() + " group by TagIdx";

        List<RowOb> robis = DBUtil.executeQuerySqlWithXORM(connPool, sql, RowOb.class);
        ArrayList<RowOb> rets = new ArrayList<>();
        rets.addAll(robis);
        return rets;
    }

    // ------------------------------

    // rt run


    public static enum JmpTP {
        off_to_on(0, "Off To On", "fto"),  //
        on_to_off(1, "On To Off", "otf");

        private final int val;
        private final String title;
        private final String mark;

        JmpTP(int v, String tt, String mark) {
            val = v;
            title = tt;
            this.mark = mark;
        }

        public static JmpTP valOfInt(int i) {
            switch (i) {

                case 0:
                    return off_to_on;
                case 1:
                    return on_to_off;

                default:
                    return null;
            }
        }

        public int getVal() {
            return val;
        }

        public String getTitle() {
            return title;
        }

        public String getMark() {
            return this.mark;
        }
    }


    // ------------ read data

    public static class RowOb {
        @XORMProperty(name = "TagIdx")
        public int tagIdx;

        @XORMProperty(name = "DT")
        public long dt;
        /**
         * 0-100
         */
        @XORMProperty(name = "JmpVal")
        public short jmpVal;
    }

    // UI

//		protected String UI_getTempTitle()
//		{
//			return this.getTitle()+" "+getTpTitle();
//		}
//
//		@Override
//		protected String UI_getTempIcon()
//		{
//			return "/_flexedge/res/jmpchg.png" ;
//		}
}
