package cn.doraro.flexedge.core.dict;

import cn.doraro.flexedge.core.Config;
import cn.doraro.flexedge.core.UAManager;
import cn.doraro.flexedge.core.util.Convert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DictManager {

    static FilenameFilter DD_FF = new FilenameFilter() {

        public boolean accept(File dir, String name) {
            String n = name.toLowerCase();
            return n.startsWith("dd_") && n.endsWith(".xml");
        }
    };
    private static Object locker = new Object();
    private static DictManager dictMgr = null;
    HashMap<String, DataClass> id2class = new HashMap<>();
    HashMap<String, DataClass> name2class = new HashMap<>();
    HashMap<String, ModuleDictMap> module2dict = new HashMap<>();
    LinkedHashMap<String, PrjDataClass> prjid2class = new LinkedHashMap<>();

    private DictManager() throws Exception,
            IOException {
        String fp = Config.getDataDirBase() + "dict/";


        for (File tmpf : listDDFiles(fp)) {
            try (FileInputStream fis = new FileInputStream(tmpf);) {
                DataClass dc = loadDataClass(fis);
                if (dc == null)
                    continue;

                id2class.put(dc.getClassId(), dc);
                name2class.put(dc.getClassName(), dc);
            }
        }

    }

    public static DictManager getInstance() {
        if (dictMgr != null)
            return dictMgr;

        synchronized (locker) {
            if (dictMgr != null)
                return dictMgr;

            try {
                dictMgr = new DictManager();
                return dictMgr;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static DataClass loadLangDataClassByClass(Class<?> c)
            throws Exception {
        String cnn = c.getCanonicalName();
        cnn = "/" + cnn.replace('.', '/') + ".lang.xml";
        DataClass dc = loadDataClassByResPath(c, cnn);
        if (dc != null) {
            String ulang = System.getProperty("user.language");
            if ("zh".equals(ulang) || "cn".equals(ulang))
                ulang = "cn";
            else
                ulang = "en";
            dc.setDefaultLang(ulang);
        }

        return dc;
    }

    /**
     * path like  /com/dw/xxx.xml
     *
     * @param resp
     * @return
     * @throws Exception
     */
    public static DataClass loadDataClassByResPath(Class<?> c, String resp)
            throws Exception {
        URL u = c.getResource(resp);
        if (u == null)
            return null;

        DocumentBuilderFactory docBuilderFactory = null;
        DocumentBuilder docBuilder = null;
        Document doc = null;

        // parse XML XDATA File
        docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setValidating(false);
        docBuilder = docBuilderFactory.newDocumentBuilder();

        doc = docBuilder.parse(u.toString());
        Element rootele = doc.getDocumentElement();

        return DataClass.loadFromEle(rootele);
    }

    public static DataClass loadDataClass(InputStream inputs)
            throws Exception {
        DocumentBuilderFactory docBuilderFactory = null;
        DocumentBuilder docBuilder = null;
        Document doc = null;

        // parse XML XDATA File
        docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setValidating(false);
        docBuilder = docBuilderFactory.newDocumentBuilder();

        doc = docBuilder.parse(inputs);

        Element rootele = doc.getDocumentElement();
        return DataClass.loadFromEle(rootele);
    }

    public static void writeDataClass(DataClass dc, OutputStream outputs) throws Exception {
        OutputStreamWriter osw = new OutputStreamWriter(outputs, "UTF-8");
        dc.writeToXml(osw);
        osw.flush();
    }

    private File[] listDDFiles(String dir) {
        File f = new File(dir);
        if (!f.exists())
            return new File[0];

        if (!f.isDirectory())
            return new File[0];

        File[] fs = f.listFiles(DD_FF);
        if (fs == null)
            return new File[0];
        return fs;
    }

    public PrjDataClass getPrjDataClassByPrjId(String prjid) {
        PrjDataClass pdc = prjid2class.get(prjid);
        if (pdc != null)
            return pdc;

        synchronized (prjid2class) {
            pdc = prjid2class.get(prjid);
            if (pdc != null)
                return pdc;

            File prjdir = UAManager.getPrjFileSubDir(prjid);
            if (!prjdir.exists())
                return null;

            pdc = new PrjDataClass(prjid);
            pdc.loadAll();
            prjid2class.put(pdc.getPrjId(), pdc);
            return pdc;
        }
    }

    public DataClass[] getAllDataClasses() {
        DataClass[] rets = new DataClass[id2class.size()];
        id2class.values().toArray(rets);
        return rets;
    }

    public DataClass getDataClass(int cid) {
        return id2class.get(cid);
    }

    public DataClass getDataClass(String classn) {
        return name2class.get(classn);
    }

    public ArrayList<String> getAllHasDictModules() {
        ArrayList<String> rets = new ArrayList<String>();
        for (Map.Entry<String, ModuleDictMap> n2mdm : module2dict.entrySet()) {
            if (n2mdm.getValue().id2class.size() <= 0)
                continue;

            rets.add(n2mdm.getKey());
        }
        return rets;
    }

    /**
     * get all class in module
     *
     * @param modulen
     * @return
     */
    public DataClass[] getAllDataClasses(String modulen) {
        ModuleDictMap mdm = module2dict.get(modulen);
        if (mdm == null)
            return null;

        DataClass[] rets = new DataClass[mdm.id2class.size()];
        mdm.id2class.values().toArray(rets);
        return rets;
    }

    public DataClass getDataClass(String modulen, int cid) {
        ModuleDictMap mdm = module2dict.get(modulen);
        if (mdm == null)
            return null;

        return mdm.id2class.get(cid);
    }

    public DataClass getDataClass(String modulen, String classn) {
        ModuleDictMap mdm = module2dict.get(modulen);
        if (mdm == null)
            return null;

        return mdm.name2class.get(classn);
    }

    /**
     * @param req
     * @param cid
     * @return
     */
    public DataClass getModuleDataClass(HttpServletRequest req, int cid) {
        String mn = Convert.getModuleNameByHttpReq(req);
        if (mn == null)
            return null;
        return getDataClass(mn, cid);
    }

    public DataClass getModuleDataClass(HttpServletRequest req, String classn) {
        String mn = Convert.getModuleNameByHttpReq(req);
        if (mn == null)
            return null;
        return getDataClass(mn, classn);
    }

    static class ModuleDictMap {
        HashMap<String, DataClass> id2class = new HashMap<>();

        HashMap<String, DataClass> name2class = new HashMap<>();
    }

}