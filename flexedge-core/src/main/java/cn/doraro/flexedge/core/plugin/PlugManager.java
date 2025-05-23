package cn.doraro.flexedge.core.plugin;

import cn.doraro.flexedge.core.Config;
import cn.doraro.flexedge.core.UAServer;
import cn.doraro.flexedge.core.msgnet.MNManager;
import cn.doraro.flexedge.core.util.Convert;
import org.json.JSONObject;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author jason.zhu
 */
public class PlugManager {
    private static PlugManager instance = null;
    LinkedHashMap<String, PlugDir> name2plug = new LinkedHashMap<>();
    HashMap<String, LinkedHashMap<String, PlugDir>> lib2plugs = new HashMap<>();
    private HashMap<String, PlugJsApi> name2jsapi = null;
    private transient boolean plugAuthGit = false;
    private transient PlugAuth plugAuth = null;

    private PlugManager() {
        findPlugs();
    }

    public static PlugManager getInstance() {
        if (instance != null)
            return instance;

        synchronized (PlugManager.class) {
            if (instance != null)
                return instance;

            instance = new PlugManager();
            return instance;
        }
    }

    private void findPlugs() {
        String lib_plugins_dir = System.getProperty("flexedge.lib_plugins.dir");
        if (Convert.isNullOrEmpty(lib_plugins_dir))
            throw new RuntimeException("no [flexedge.lib_plugins.dir] env property found");

        File plugdir = new File(lib_plugins_dir);//new File(Config.getDataDirBase()+"/plugins/");
        if (!plugdir.exists())
            return;//new ArrayList<>(0) ;

        File[] dirfs = plugdir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }
        });

        for (File dirf : dirfs) {
            if (dirf.getName().startsWith("_"))
                continue;
            PlugDir pd = PlugDir.parseDir(dirf, null);
            if (pd == null)
                continue;
            name2plug.put(pd.getName(), pd);
        }
        //return plugname2cl ;
    }

    public void onWebappLoaded(UAServer.WebItem wi) {
        PlugDir pd = PlugDir.parseDir(wi.getWebDir(), wi.getAppClassLoader());
        if (pd == null)
            return;
        name2plug.put(pd.getName(), pd);
        System.out.println(" find webapp plug [" + wi.getAppName() + "] @ " + wi.getWebDir().getPath());

        JSONObject msg_net_jo = pd.getMsgNetJO();
        if (msg_net_jo != null) {
            MNManager.registerByWebItem(wi, msg_net_jo);
        }
    }

    public Collection<PlugDir> listPlugs() {
        return this.name2plug.values();
    }

//	public HashMap<String,JSObPk> getJsApiPkAll()
//	{
//		HashMap<String,JSObPk> ret = new HashMap<>() ;
//		for(PlugDir pd:this.name2plug.values())
//		{
//			try
//			{
//				HashMap<String,PlugJsApi> n2o = pd.getOrLoadJsApiObjs() ;
//				if(n2o==null)
//					continue ;
//				for(Map.Entry<String, Object> n2v:n2o.entrySet())
//				{
//					JSObPk obpk = new JSObPk(n2v.getValue()) ;
//					ret.put(n2v.getKey(), obpk);
//				}
//			}
//			catch(Exception e)
//			{
//				e.printStackTrace();
//			}
//		}
//		return ret ;
//	}


//	public HashMap<String,Object> getAuthAll()
//	{
//		HashMap<String,Object> ret = new HashMap<>() ;
//		for(PlugDir pd:this.name2plug.values())
//		{
//			try
//			{
//				HashMap<String,Object> n2o = pd.getOrLoadAuthObjs() ;
//				if(n2o==null)
//					continue ;
//				ret.putAll(n2o);
//			}
//			catch(Exception e)
//			{
//				e.printStackTrace();
//			}
//		}
//		return ret ;
//	}

    public PlugDir getPlug(String name) {
        return this.name2plug.get(name);
    }

    public HashMap<String, PlugJsApi> getJsApiAll() {
        if (name2jsapi != null)
            return name2jsapi;

        HashMap<String, PlugJsApi> ret = new HashMap<>();
        for (PlugDir pd : this.name2plug.values()) {
            try {
                HashMap<String, PlugJsApi> n2o = pd.getOrLoadJsApiObjs();
                if (n2o == null)
                    continue;
                ret.putAll(n2o);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        name2jsapi = ret;
        return ret;
    }

    public Object getAuthObj() {
        Element ele = Config.getConfElement("plug_auth");
        if (ele == null)
            return null;
        String name = ele.getAttribute("name");
        if (Convert.isNullOrEmpty(name))
            return null;

        for (PlugDir pd : this.name2plug.values()) {
            try {
                Object o = pd.loadAuthObj(name);
                if (o != null)
                    return o;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public PlugAuth getPlugAuth()// throws Exception
    {
        if (plugAuthGit)
            return plugAuth;

        try {
            Element ele = Config.getConfElement("plug_auth");
            if (ele == null)
                return null;
            String name = ele.getAttribute("name");
            if (Convert.isNullOrEmpty(name))
                return null;
            String token_cookie = ele.getAttribute("token_cookie_name");
            if (Convert.isNullOrEmpty(token_cookie))
                token_cookie = "token";

            String no_r_p = ele.getAttribute("no_read_right_prompt");
            String no_w_p = ele.getAttribute("no_write_right_prompt");

            Object ob = getAuthObj();
            if (ob == null) {
                //throw new Exception("no plug_auth ["+name+"] found") ;
                System.err.println("no plug_auth [" + name + "] found");
            }
            plugAuth = new PlugAuth(ob, token_cookie);
            plugAuth.asNoRightPrompt(no_r_p, no_w_p);
            plugAuth.initAuth();
            System.out.println("plug_auth [" + name + "] " + ob.getClass().getCanonicalName() + " set ok");
            return plugAuth;
        } finally {
            plugAuthGit = true;
        }
    }


    public LinkedHashMap<String, PlugDir> LIB_getPlugs(String lib_name) {
        LinkedHashMap<String, PlugDir> pds = lib2plugs.get(lib_name);
        if (pds != null)
            return pds;

        String lib_plugins_dir = System.getProperty("flexedge.lib_plugins.dir");
        if (Convert.isNullOrEmpty(lib_plugins_dir))
            throw new RuntimeException("no [flexedge.lib_plugins.dir] env property found");

        File plugdir = new File(lib_plugins_dir + "/_libs/" + lib_name + "/");
        if (!plugdir.exists())
            return null;//new ArrayList<>(0) ;

        pds = new LinkedHashMap<String, PlugDir>();

        File[] dirfs = plugdir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }
        });

        for (File dirf : dirfs) {
            if (dirf.getName().startsWith("_"))
                continue;
            PlugDir pd = PlugDir.parseDir(dirf, null);
            if (pd == null)
                continue;
            pds.put(pd.getName(), pd);
        }
        lib2plugs.put(lib_name, pds);

        return pds;
    }
}
