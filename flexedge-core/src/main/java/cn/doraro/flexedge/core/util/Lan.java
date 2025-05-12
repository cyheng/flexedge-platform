package cn.doraro.flexedge.core.util;

import cn.doraro.flexedge.core.Config;
import cn.doraro.flexedge.core.dict.DataClass;
import cn.doraro.flexedge.core.dict.DataNode;
import cn.doraro.flexedge.core.dict.DictManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Lan {
    static HashMap<String, Object> pk2lang = new HashMap<>();

    static HashMap<String, Object> pk2prop_lang = new HashMap<>();
    private static String sys_ln = null;
    private static ThreadLocal<String> th_lang = new ThreadLocal<>();

    static {
        try {
            sys_ln = loadSysLan();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    HashMap<String, String> name2val = new HashMap<>();
    DataClass dc = null;

    private Lan(DataClass dc) {
        this.dc = dc;
    }

    public static Lan getLangInPk(Class<?> c) {
        return getOrLoadLangInPk(c, pk2lang, "lang.xml");
    }

    public static Lan getPropLangInPk(Class<?> c) {
        return getOrLoadLangInPk(c, pk2prop_lang, "prop_lang.xml");
    }

    private static Lan getOrLoadLangInPk(Class<?> c, HashMap<String, Object> pk2obj, String filename) {
        String pkn = c.getPackage().getName();
        Object ln = pk2obj.get(pkn);
        if (ln != null) {
            if (ln instanceof String)
                return null;
            else
                return (Lan) ln;
        }

        synchronized (c) {
            ln = pk2obj.get(pkn);
            if (ln != null) {
                if (ln instanceof String)
                    return null;
                else
                    return (Lan) ln;
            }

            Lan lg = null;
            try {
                lg = loadLang(c, filename);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (lg == null)
                pk2obj.put(pkn, "");
            else
                pk2obj.put(pkn, lg);
            return lg;
        }
    }

    private static Lan loadLang(Class<?> c, String filename) throws Exception {
        String pkn = c.getPackage().getName();

        String pkresln = "/" + pkn.replaceAll("\\.", "/") + "/" + filename;

        DataClass dc = DictManager.loadDataClassByResPath(c, pkresln);
        if (dc == null) {
            //pk2lang.put(pkn, "");
            return null;
        }
        return new Lan(dc);
    }

    /**
     * may be used in jsp page
     *
     * @param lang
     */
    public static void setLangInThread(String lang) {
        if (Convert.isNotNullEmpty(lang))
            th_lang.set(lang);
        else
            th_lang.remove();
    }

    public static String getSysLang() {
        return sys_ln;
    }

    /**
     * used for install config
     *
     * @param ln
     * @throws IOException
     */
    public static void setSysLang(String ln) throws IOException {
        sys_ln = ln;

        if (sys_ln == null)
            sys_ln = "";

        saveSysLan(sys_ln);
    }

    private static void saveSysLan(String ln) throws IOException {
        String fp = Config.getDataDirBase() + "/auth/__sys_lang.txt";
        File f = new File(fp);
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        Convert.writeFileTxt(f, ln, "utf-8");
    }

    private static String loadSysLan() throws IOException {
        String fp = Config.getDataDirBase() + "/auth/__sys_lang.txt";
        File f = new File(fp);
        if (!f.exists()) {
            return null;
        }
        String ss = Convert.readFileTxt(f, "utf-8");
        if (ss == null || (ss = ss.trim()).equals(""))
            return null;
        return ss;
    }

    public static String getUsingLang() {
        String ln = th_lang.get();
        if (ln == null) {
            ln = sys_ln;
        }

        if (ln == null) {
            ln = Config.getAppLang();
        }

        if (Convert.isNullOrEmpty(ln))
            ln = "en";
        return ln;
    }

    public String g(String name) {
        DataNode dn = dc.getNodeByName(name);
        if (dn == null)
            return "[x]" + name + "[x]";

        return dn.getNameByLang(getUsingLang());
    }

    public String g_def(String name, String defv) {
        DataNode dn = dc.getNodeByName(name);
        if (dn == null)
            return defv;

        return dn.getNameByLang(getUsingLang());
    }

    public DataNode gn(String name) {
        return dc.getNodeByName(name);
    }

    public String g(String name, String pn) {
        DataNode dn = dc.getNodeByName(name);
        if (dn == null)
            return "[p]" + name + "[p]";

        String ppn = pn + "_" + getUsingLang();
        return dn.getAttr(ppn);
    }

    public String g(String name, String pn, String def) {
        DataNode dn = dc.getNodeByName(name);
        if (dn == null)
            return def;

        String ppn = pn + "_" + getUsingLang();
        String ret = dn.getAttr(ppn);
        if (ret == null)
            return def;
        return ret;
    }
}
