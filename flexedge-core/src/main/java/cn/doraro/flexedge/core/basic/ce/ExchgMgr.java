package cn.doraro.flexedge.core.basic.ce;

import cn.doraro.flexedge.core.store.StoreExchgModuleAdp;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ExchgMgr {
    static ArrayList<ExchgModuleAdp> ADPS = new ArrayList<>();

    static {
        ADPS.add(new StoreExchgModuleAdp());
    }

    public static List<ExchgModuleAdp> listModuleAdps() {
        return ADPS;
    }

    public static JSONObject SOR_toExchgJO() {
        JSONObject jo = new JSONObject();
        JSONArray module_jarr = new JSONArray();
        jo.put("modules", module_jarr);

        for (ExchgModuleAdp adp : ADPS) {
            ExchgModule em = adp.SOR_provideExchgModule();
            if (em == null)
                continue;
            JSONObject mjo = em.toExchgJO();
            module_jarr.put(mjo);
        }
        return jo;
    }
}
