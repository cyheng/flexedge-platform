package cn.doraro.flexedge.core.router;

import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.ILang;
import cn.doraro.flexedge.core.util.Lan;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class RouterOuterAdpCat implements ILang {
    public static final RouterOuterAdpCat MQ = new RouterOuterAdpCat("mq");
    public static final RouterOuterAdpCat DB = new RouterOuterAdpCat("db");
    public static final RouterOuterAdpCat OFFICE = new RouterOuterAdpCat("office");
    public static final RouterOuterAdpCat ERP_MES = new RouterOuterAdpCat("erp_mes");
    public static final RouterOuterAdpCat MACHINE_DEV = new RouterOuterAdpCat("machine_dev");
    static Lan lan = Lan.getLangInPk(RouterOuterAdpCat.class);
    static ArrayList<RouterOuterAdpCat> catsAll = new ArrayList<>();
    private static LinkedHashMap<String, RouterOuterAdp> TP2OUTER = new LinkedHashMap<>();
    private static LinkedHashMap<String, AdpDef> TP2DEF = new LinkedHashMap<>();

    static {
        catsAll.add(MQ);
        catsAll.add(DB);
        catsAll.add(OFFICE);
        catsAll.add(ERP_MES);
        catsAll.add(MACHINE_DEV);
    }

    static {
        registerOuterAdpDef("mqtt", "cn.doraro.flexedge.core.router.roa.ROAMqtt", MQ);
        registerOuterAdpDef("kafka", "cn.doraro.flexedge.ext.roa.ROAKafka", MQ);

        registerOuterAdpDef("db_mysql", "", DB); //cn.doraro.flexedge.core.router.roa.ROAJdbcMySql
        registerOuterAdpDef("db_oracle", "", DB); //cn.doraro.flexedge.core.router.roa.ROAJdbcOracle
        registerOuterAdpDef("db_sqlserver", "", DB); //cn.doraro.flexedge.core.router.roa.ROAJdbcSQLServer
        registerOuterAdpDef("db_influxdb", "", DB);

        registerOuterAdpDef("email", "", OFFICE);
        registerOuterAdpDef("xls", "", OFFICE);
        registerOuterAdpDef("csv", "", OFFICE);

        registerOuterAdpDef("sap", "", ERP_MES);

        registerOuterAdpDef("cnc", "", MACHINE_DEV);
        registerOuterAdpDef("barcode", "", MACHINE_DEV);
        registerOuterAdpDef("opc_ua", "", MACHINE_DEV);
    }

    String name;
    ArrayList<AdpDef> adps = new ArrayList<>();

    public RouterOuterAdpCat(String name) {
        this.name = name;
    }

    public static List<RouterOuterAdpCat> listCatsAll() {
        return catsAll;
    }

    public static void registerOuterAdp(RouterOuterAdp outer) {
        TP2OUTER.put(outer.getTp(), outer);
    }

    public static RouterOuterAdp getAdpByTP(String tp) {
        RouterOuterAdp ret = TP2OUTER.get(tp);
        if (ret != null)
            return ret;
        AdpDef ad = TP2DEF.get(tp);
        if (ad == null)
            return null;
        if (Convert.isNullOrEmpty(ad.className))
            return null;

        try {
            Class<?> c = Class.forName(ad.className);
            Constructor<?> cs = c.getConstructor(RouterManager.class);
            RouterOuterAdp ro = (RouterOuterAdp) cs.newInstance((RouterManager) null);
            TP2OUTER.put(tp, ro); //registerOuterAdp(ro) ;
            return ro;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static AdpDef registerOuterAdpDef(String tp, String class_name, RouterOuterAdpCat cat) {
        AdpDef ad = new AdpDef(tp, class_name);
        TP2DEF.put(tp, ad);
        cat.adps.add(ad);
        return ad;
    }

    public static List<RouterOuterAdp> listOuterAdpTPS() {
        ArrayList<RouterOuterAdp> rets = new ArrayList<>();
        rets.addAll(TP2OUTER.values());
        return rets;
    }

    public String getName() {
        return this.name;
    }

    public String getTitle() {
        return g("adp_cat_" + name);
    }

    public List<AdpDef> getAdpDefs() {
        return this.adps;
    }

    public static class AdpDef {
        String className;

        String adpTp;

        public AdpDef(String tp, String classn) {
            this.className = classn;
            this.adpTp = tp;
        }

        public String getAdpTP() {
            return this.adpTp;
        }

        public String getAdpTPTitle() {
            return lan.g("adp_" + this.adpTp);
        }

        public boolean isOk() {
            return Convert.isNotNullEmpty(this.className);
        }
    }
}
