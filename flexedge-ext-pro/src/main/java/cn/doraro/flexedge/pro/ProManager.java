package cn.doraro.flexedge.pro;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
public class ProManager {
    static ProManager instance = null;
    private LinkedHashMap<String, IPro> name2pro = new LinkedHashMap();

    public static ProManager getInstance() {
        if (instance != null) {
            return instance;
        } else {
            Class var0 = ProManager.class;
            synchronized(ProManager.class) {
                if (instance != null) {
                    return instance;
                } else {
                    instance = new ProManager();
                    return instance;
                }
            }
        }
    }

    private ProManager() {
    }

    public boolean registerPro(Object p) {
        ProInter pi = new ProInter(p);
        String n = pi.getProName();
        if (n != null && !n.equals("")) {
            this.name2pro.put(n, pi);
            return true;
        } else {
            return false;
        }
    }

    public List<IPro> listPros() {
        ArrayList<IPro> rets = new ArrayList();
        Iterator var3 = this.name2pro.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<String, IPro> n2p = (Map.Entry)var3.next();
            rets.add((IPro)n2p.getValue());
        }

        return rets;
    }

    public IPro getPro(String name) {
        return (IPro)this.name2pro.get(name);
    }

    public IProLic getProLic(String name) {
        IPro p = this.getPro(name);
        if (p == null) {
            return null;
        } else {
            return !(p instanceof IProLic) ? null : (IProLic)p;
        }
    }
}
