package cn.doraro.flexedge.core.msgnet;

import cn.doraro.flexedge.core.UAServer;
import cn.doraro.flexedge.core.msgnet.util.ConfItem;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.ILang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MNCat implements ILang {
    ArrayList<MNNode> nodes = new ArrayList<>();
    ArrayList<MNModule> modules = new ArrayList<>();
    UAServer.WebItem webItem = null;
    HashMap<String, ConfItem> item2conf = new HashMap<>();
    /**
     * name or appname
     */
    private String name;
    private String title = null;

    public MNCat(String name) {
        this.name = name;
    }

    public MNCat(String name, String title) {
        this.name = name;
        this.title = title;
    }

    public MNCat asWebItem(UAServer.WebItem wi) {
        this.webItem = wi;
        return this;
    }

    UAServer.WebItem getWebItem() {
        return this.webItem;
    }


    public String getName() {
        return this.name;
    }

    public String getTitle() {
        String t = Convert.isNotNullEmpty(this.title) ? this.title : this.name;
        return g_def(this.name, t);
    }

    public List<MNNode> getNodes() {
        return nodes;
    }

    public MNNode getNodeByTP(String tp) {
        for (MNNode n : this.nodes) {
            if (tp.equals(n.getTP()))
                return n;
        }
        return null;
    }

    public List<MNModule> getModules() {
        return modules;
    }

    public MNModule getModuleByTP(String tp) {
        for (MNModule m : this.modules) {
            if (tp.equals(m.getTP()))
                return m;
        }
        return null;
    }

    public String getParamUrl(MNBase n) {
        if (this.webItem == null) {
            String tpf = n.getTPFull();
            if (tpf.startsWith("_platform.")) {
                return "./plat/" + n.getTPFull() + ".pm.jsp";
            } else {
                if (n instanceof MNModule)
                    return "./modules/" + n.getTPFull() + ".pm.jsp";
                return "./nodes/" + n.getTPFull() + ".pm.jsp";
            }
        }

        ConfItem ci = item2conf.get(n.getTPFull());
        if (ci == null)
            return null;
        return "/" + webItem.getAppName() + "/" + ci.getPmUIPath();
    }

    public String getDocUrl(MNBase n) {
        if (this.webItem == null)
            return "./nodes/" + n.getTPFull() + "_doc.html";
        ConfItem ci = item2conf.get(n.getTPFull());
        if (ci == null)
            return null;
        return "/" + webItem.getAppName() + "/" + ci.getDocPath();
    }
}
