package cn.doraro.flexedge.core.util.web;

import cn.doraro.flexedge.core.dict.DataClass;
import cn.doraro.flexedge.core.dict.DataNode;
import cn.doraro.flexedge.core.dict.DictManager;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.Lan;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author zzj
 */
public class Lang {
    static Object locker = new Object();

    private static HashMap<Class<?>, Lang> jspClass2Lang = new HashMap<>();


    private static HashMap<String, Class<?>> reqUri2JspClass = new HashMap<>();
    String defaultLang = "en";
    DataClass langDC = null;

    Lang(File langf, String default_lan)
            throws Exception {
        try (FileInputStream fis = new FileInputStream(langf)) {
            langDC = DictManager.loadDataClass(fis);
        }
        if (default_lan != null)
            defaultLang = default_lan;
    }

    public static Lang getPageLang(Servlet jspp, HttpServletRequest req) {
        Class<?> jspc = jspp.getClass();
        Lang wpl = (Lang) jspClass2Lang.get(jspc);
        if (wpl != null) {
            return wpl;
        }

        synchronized (locker) {
            wpl = (Lang) jspClass2Lang.get(jspc);
            if (wpl != null)
                return wpl;

            try {
                Class tmpc = reqUri2JspClass.get(req.getRequestURI());
                if (tmpc != null) {
                    System.out.println("remove page lang by old="
                            + req.getRequestURI());
                    jspClass2Lang.remove(tmpc);
                }

                String realpath = jspp.getServletConfig().getServletContext()
                        .getRealPath(req.getServletPath());
                File f = new File(realpath);
                File langf = new File(f.getAbsolutePath() + ".lang");

                if (!langf.exists())
                    return null;

                wpl = new Lang(langf, null);

                //System.out.println("add new page lang=" + req.getRequestURI());
                reqUri2JspClass.put(req.getRequestURI(), jspc);
                jspClass2Lang.put(jspc, wpl);

                return wpl;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public String getLangValue(String key) {
        if (langDC == null)
            return "[X]" + key + "[X]";
        DataNode dn = langDC.getNodeByName(key);
        if (dn == null)
            return "[X]" + key + "[X]";
        String ln = Lan.getUsingLang();
        String tmps = dn.getNameByLang(ln);//(defaultLang);
        if (tmps != null)
            return tmps;

        return "[X]" + key + "[X]";
    }

//	WebPageLang(byte[] langf, String default_lan)
//			throws Exception
//	{
//		langDC = DictManager.loadDataClass(null,langf);
//		if (default_lan != null)
//			defaultLang = default_lan;
//	}

    String getLangValueNoPPT(String key) {
        if (langDC == null)
            return null;
        DataNode dn = langDC.getNodeByName(key);
        if (dn == null)
            return null;
        String ln = Lan.getUsingLang();
        String tmps = dn.getNameByLang(ln);//(defaultLang);
        if (tmps != null)
            return tmps;

        return null;
    }

    public String getLangValue(List<String> keys) {
        if (langDC == null)
            return "[X]" + Convert.combineStrWith(keys, ",") + "[X]";

        String ln = Lan.getUsingLang();
        String gap = "en".equals(ln) ? " " : "";
        StringBuilder sb = new StringBuilder();
        boolean bfirst = true;
        for (String key : keys) {
            if (bfirst) bfirst = false;
            else sb.append(gap);

            DataNode dn = langDC.getNodeByName(key);
            if (dn == null) {
                sb.append("[X]" + key + "[X]");
                continue;
            }

            String tmps = dn.getNameByLang(ln);//(defaultLang);
            if (tmps != null) {
                sb.append(tmps);
                continue;
            }

            sb.append("[X]" + key + "[X]");

        }

        return sb.toString();
    }

    public DataNode getLangDataNode(String key) {
        if (langDC == null)
            return null;

        return langDC.getNodeByName(key);
    }

    public DataClass getLangDataClass() {
        return langDC;
    }

    public List<DataNode> listLangDataNodesByPrefix(String prefix_n) {
        if (langDC == null)
            return new ArrayList<DataNode>();

        ArrayList<DataNode> rets = new ArrayList<DataNode>();
        List<DataNode> dns = langDC.getRootNodes();
        if (dns == null || dns.size() <= 0)
            return rets;
        for (DataNode dn : dns) {
            if (dn.getName().startsWith(prefix_n))
                rets.add(dn);
        }
        return rets;
    }

    public String getLangValue(String key, String lang) {
        if (langDC == null)
            return "[X]" + key + "[X]";

        if (lang == null) {// ʹ��ҳ�涨�������
            lang = defaultLang;
        }

        DataNode dn = langDC.getNodeByName(key);
        if (dn == null)
            return "[X]" + key + "[X]";
        String tmps = dn.getNameByLang(lang);
        if (tmps != null)
            return tmps;
        return "[X]" + key + "[X]";
    }

    static class LangFilenameFilter implements FilenameFilter {
        private String jspFN = null;

        public LangFilenameFilter(String jspfn) {
            jspFN = jspfn.toLowerCase();
        }

        public boolean accept(File dir, String name) {
            String tmpn = name.toLowerCase();
            if (!tmpn.startsWith(jspFN))
                return false;

            return tmpn.endsWith(".lang");
        }
    }
}
