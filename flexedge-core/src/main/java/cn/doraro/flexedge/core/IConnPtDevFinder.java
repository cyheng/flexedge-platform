package cn.doraro.flexedge.core;

import java.util.LinkedHashMap;

public interface IConnPtDevFinder {
    public LinkedHashMap<String, ConnDev> getFoundConnDevs();

    public default boolean addConnDevToCh(UACh ch, ConnDev cd, StringBuilder failedr) throws Exception {
        UADev dev = ch.getDevByName(cd.getName());
        if (dev != null) {
            failedr.append("device is existed");
            return false;
        }

        UADev ndev = ch.addDev(cd.getName(), cd.getTitle(), "", null, null, null);

        for (ConnDev.Data d : cd.getDatas()) {
            String vtstr = d.getValTp();
            UAVal.ValTP vt = UAVal.getValTp(vtstr);
            if (vt == null)
                continue;
            //UATag nt = ndev.addTagWithGroupByPath(d.getPath(),vt,false) ;
        }
        ndev.save();
        return true;
    }
}
