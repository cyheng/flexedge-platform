package cn.doraro.flexedge.core;

import cn.doraro.flexedge.core.util.CompressUUID;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.ZipUtil;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import cn.doraro.flexedge.core.util.xmldata.DataTranserXml;
import cn.doraro.flexedge.core.util.xmldata.XmlData;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;

/**
 * manager Driver,Device Cat and DevDef
 *
 * @author jason.zhu
 */
public class DevManager // implements IResCxt
{
    protected static ILogger log = LoggerManager.getLogger(DevManager.class);

    private static DevManager instance = null;
    private ArrayList<DevDriver> drivers = new ArrayList<>();
    private ArrayList<DevDrvCat> driverCats = new ArrayList<>();
    private ArrayList<DevLib> libs = null;

    private DevManager() {
        try {
            loadDrivers();

            //loadCats();
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public static DevManager getInstance() {
        if (instance != null)
            return instance;
        synchronized (DevManager.class) {
            if (instance != null)
                return instance;
            instance = new DevManager();
            return instance;
        }
    }

    static File getDevDrvBase() {
        String fp = Config.getDataDirBase() + "/dev_drv/";
        return new File(fp);
    }

    static File getDevLibBase() {
        String fp = Config.getDataDirBase() + "/dev_lib/";
        return new File(fp);
    }

    private void loadDrivers() throws Exception {
        File f = new File(getDevDrvBase(), "drivers.json");
        if (!f.exists())
            return;
        String txt = Convert.readFileTxt(f, "UTF-8");
        JSONArray jarr = new JSONArray(txt);
        int len = jarr.length();
        for (int i = 0; i < len; i++) {
            //load cat
            JSONObject catjo = jarr.getJSONObject(i);
            DevDrvCat ddc = new DevDrvCat();
            ddc.name = catjo.getString("cat_name");
            ddc.title = catjo.getString("cat_title");
            this.driverCats.add(ddc);

            JSONArray d_jarr = catjo.getJSONArray("drivers");
            int dlen = d_jarr.length();
            for (int j = 0; j < dlen; j++) {
                JSONObject jo = d_jarr.getJSONObject(j);
                String ln = jo.optString("class");
                if (Convert.isNullOrEmpty(ln) || ln.startsWith("#"))
                    continue;
                try {
                    Class<?> c = Class.forName(ln);
                    DevDriver dd = (DevDriver) c.getConstructor().newInstance();
                    if (dd == null)
                        continue;
                    List<DevDriver> multi_dds = dd.supportMultiDrivers();
                    if (multi_dds == null || multi_dds.size() <= 0) {
                        drivers.add(dd);
                        ddc.drivers.add(dd);
                    } else {
                        drivers.addAll(multi_dds);
                        ddc.drivers.addAll(multi_dds);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.warn("load driver [" + ln + "] failed!");
                }
            }
        }
    }


    private File getDevLibDir(String libid) {
        return new File(getDevLibBase(), "lib_" + libid + "/");
    }

    private DevLib loadLib(String libid) throws Exception {
        File libdir = getDevLibDir(libid);
        if (!libdir.exists())
            return null;
        File catf = new File(libdir, "lib.xml");
        if (!catf.exists())
            return null;
        DevLib r = new DevLib();
        if (catf.length() > 0) {
            XmlData tmpxd = XmlData.readFromFile(catf);
            DataTranserXml.injectXmDataToObj(r, tmpxd);
        }
        r.id = libid;
        return r;
    }

    void saveLib(DevLib dc) throws Exception {
        File libdir = getDevLibDir(dc.getId());
        if (!libdir.exists())
            libdir.mkdirs();
        XmlData xd = DataTranserXml.extractXmlDataFromObj(dc);
        // XmlData xd = rep.toUAXmlData();
        XmlData.writeToFile(xd, new File(libdir, "lib.xml"));
    }

    DevLib reloadLib(String libid) throws Exception {
        DevLib c = this.loadLib(libid);
        if (c == null)
            return null;

        List<DevLib> libs = getDevLibs();

        for (int i = 0, n = libs.size(); i < n; i++) {
            DevLib cat = libs.get(i);
            if (cat.getId().equals(libid)) {
                libs.set(i, c);
                return c;
            }
        }
        libs.add(c);
        return c;
    }

    private ArrayList<DevLib> loadLibs() {
        ArrayList<DevLib> rets = new ArrayList<>();
        File lbf = getDevLibBase();
        if (!lbf.exists())
            return rets;

        File[] fs = lbf.listFiles(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (!f.isDirectory())
                    return false;
                String n = f.getName();
                return n.startsWith("lib_");
            }
        });

        for (File tmpf : fs) {
            String libid = tmpf.getName().substring(4);
            try {
                DevLib dc = loadLib(libid);
                if (dc == null) {
                    log.warn("load DevLib failed [" + libid + "]");
                    continue;
                }
                rets.add(dc);
            } catch (Exception e) {
                log.warn("load DevLib error [" + libid + "]");
                e.printStackTrace();
            }
        }
        return rets;
    }

    public List<DevLib> getDevLibs() {
        if (libs != null)
            return libs;

        synchronized (this) {
            if (libs != null)
                return libs;

            libs = loadLibs();
            Collections.sort(libs);
        }
        return libs;
    }

    public DevLib getDevLibById(String libid) {
        for (DevLib lib : getDevLibs()) {
            if (lib.getId().equals(libid))
                return lib;
        }
        return null;
    }

    public DevDef getDevDefById(String libid, String defid) {
        DevLib lib = getDevLibById(libid);
        if (lib == null)
            return null;
        return lib.getDevDefById(defid);
    }


    public DevLib addDevLib(String title) throws Exception {
        //StringBuilder failedr = new StringBuilder();
        DevLib lib = new DevLib(title);
        this.saveLib(lib);
        this.getDevLibs().add(lib);
        return lib;
    }

    public void delDevLib(String libid) {
        DevLib dc = this.getDevLibById(libid);
        if (dc == null)
            return;
        File dir = getDevLibDir(libid);
        if (!dir.exists())
            return;
        Convert.deleteDir(dir);
        this.getDevLibs().remove(dc);
    }

    public DevCat getDevCatById(String libid, String catid) {
        DevLib lib = this.getDevLibById(libid);
        if (lib == null)
            return null;
        return lib.getDevCatById(catid);
    }

    public DevDef getDevDefById(String libid, String catid, String devid) {
        DevCat dc = getDevCatById(libid, catid);
        if (dc == null)
            return null;
        return dc.getDevDefById(devid);
    }

    public UANode findNodeByPath(String path) {
        if (Convert.isNullOrTrimEmpty(path))
            return null;
        LinkedList<String> ss = Convert.splitStrWithLinkedList(path, "/\\.");
        String n = ss.removeFirst();
        List<String> devps = Convert.splitStrWith(n, "-");
        if (devps.size() != 3)
            return null;
        DevLib dl = this.getDevLibById(devps.get(0));
        // DevDriver drv = this.getDriver() ;
        if (dl == null)
            return null;
        DevCat cat = dl.getDevCatByName(devps.get(1));
        if (cat == null)
            return null;
        DevDef dd = cat.getDevDefByName(devps.get(2));
        if (dd == null)
            return null;

        return dd.getDescendantNodeByPath(ss);
    }

//	public UANode findNodeById(String id)
//	{
//		for(DevDriver drv:this.getDrivers())
//		{
//			DevDef dd = drv.getDevDefById(id) ;
//			if(dd==null)
//				continue ;
//			UANode n = dd.findNodeById(id) ;
//			if(n!=null)
//				return n ;
//		}
//		return null;
//	}
//	
//	public DevDef getDevDefById(String id)
//	{
//		for(DevDriver drv:this.getDrivers())
//		{
//			DevDef dd = drv.getDevDefById(id) ;
//			if(dd!=null)
//				return dd ;
//		}
//		return null;
//	}
//	
//	public DevCat getDevCatById(String catid)
//	{
//		for(DevDriver drv:this.getDrivers())
//		{
//			DevCat dc = drv.getDevCatById(catid) ;
//			if(dc!=null)
//				return dc ;
//		}
//		return null ;
//	}


    public File exportDevLibTo(String libid, File fout) throws IOException {
        DevLib lib = this.getDevLibById(libid);
        if (lib == null)
            return null;


        File dir = lib.getLibDir();
        List<File> fs = Arrays.asList(dir);
        HashMap<String, String> metam = new HashMap<>();
        metam.put("tp", "devlib");
        metam.put("libid", libid);
        metam.put("libtitle", lib.getTitle());

        String metatxt = Convert.transMapToPropStr(metam);

        ZipUtil.zipFileOut(metatxt, fs, fout);
        return fout;
    }

    public File exportDevLibToTmp(String libid) throws IOException {
        String fn = "lib_" + libid + ".zip";
        File fout = new File(Config.getDataDirBase() + "/tmp/" + fn);
        exportDevLibTo(libid, fout);
        return fout;
    }


    public File backupDevCatToZip(String libid) throws IOException {
        String fn = "lib_" + libid + "_" + System.currentTimeMillis() + ".zip";
        File fout = new File(Config.getDataBackupDir(), "dev_lib/" + fn);
        exportDevLibTo(libid, fout);
        return fout;
    }

    public HashMap<String, String> parseDevLibZipFileMeta(File zipf) throws Exception {
        //ArrayList<IdName> rets =new ArrayList<>() ;

        String metatxt = ZipUtil.readZipMeta(zipf);
        if (metatxt == null || metatxt.equals(""))
            return null;
        HashMap<String, String> pms = Convert.transPropStrToMap(metatxt);
        if (!"devlib".equals(pms.get("tp")))
            return null;
        String libid = pms.get("libid");
        if (Convert.isNullOrEmpty(libid))
            return null;
        return pms;
    }


    public boolean importDevLibZipFile(File zipf, String libid, String title) throws Exception {
        HashMap<String, String> pms = parseDevLibZipFileMeta(zipf);
        if (pms == null)
            return false;

        String oldlibid = pms.get("libid");
        if (Convert.isNullOrEmpty(oldlibid))
            return false;

        if (Convert.isNullOrEmpty(libid))
            libid = CompressUUID.createNewId();

//		if( !catid.contentEquals(pms.get("catid")) 
//			||  !catname.contentEquals(pms.get("catname"))
//			||  !drvname.contentEquals(pms.get("drvname")))
//			return false;


        String libdirname = "lib_" + oldlibid + "/";
        String libdirname1 = "lib_" + oldlibid + "\\";
        int prefixlen = libdirname.length();
        List<String> ens = ZipUtil.readZipEntrys(zipf);
        HashMap<String, String> outens = new HashMap<>();
        for (String en : ens) {
            if (en.startsWith(libdirname) || en.startsWith(libdirname1)) {
                String taren = "lib_" + libid + "/" + en.substring(prefixlen);
                outens.put(en, taren);
            }
        }

        File libbf = getDevLibBase();
        File oldcatdir = new File(libbf, libdirname);
        if (oldlibid.equals(libid) && oldcatdir.exists()) {//back up
            backupDevCatToZip(libid);
            Convert.deleteDir(oldcatdir);
        }

        ZipUtil.readZipOut(zipf, outens, libbf);

        //
        DevLib lib = reloadLib(libid);
        if (lib == null)
            return false;

        if (Convert.isNotNullEmpty(title) && !title.equals(lib.getTitle())) {
            lib.asTitle(title);
            lib.save();
        }
        return true;
    }

    public List<DevDrvCat> getDriverCats() {
        return this.driverCats;
    }

    public List<DevDriver> getDrivers() {
        return drivers;
    }

    public DevDriver getDriver(String name) {
        for (DevDriver dd : drivers) {
            if (name.contentEquals(dd.getName()))
                return dd;
        }
        return null;
    }

//	public DevCat getDevCat(String drvname,String catname)
//	{
//		DevDriver dd = this.getDriver(drvname);
//		if(dd==null)
//			return null ;
//		return dd.getDevCatByName(catname) ;
//	}
//	
//	public DevCat getDevCatWithCatId(String drvname,String catid)
//	{
//		DevDriver dd = this.getDriver(drvname);
//		if(dd==null)
//			return null ;
//		return dd.getDevCatById(catid) ;
//	}

    DevDriver createDriverIns(String name) {
        DevDriver dd = getDriver(name);
        if (dd == null)
            return null;
        dd = dd.copyMe();
        return dd;
    }


    public List<DevDriver> listDriversNotNeedConn() {
        List<DevDriver> drvs = getDrivers();
        ArrayList<DevDriver> rets = new ArrayList<>();
        for (DevDriver drv : drvs)
            rets.add(drv);
        return rets;
    }


}
