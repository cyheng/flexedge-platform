package cn.doraro.flexedge.core.basic.ce;

import org.json.JSONObject;

import java.util.List;

/**
 * provide ExchgModule in self configuration
 *
 * @author jason.zhu
 */
public abstract class ExchgModuleAdp {
    public abstract String getExchgModuleName();

    public abstract String getExchgModuleTitle();

    public final ExchgModule SOR_provideExchgModule() {
        List<ExchgObj> objs = SOR_provideExchgObjs();
        return new ExchgModule(this.getExchgModuleName(), this.getExchgModuleTitle(), objs);
    }

    public abstract List<ExchgObj> SOR_provideExchgObjs();

    public abstract ExchgObj createExchgObjByTP(String tp);

    public final ExchgModule USR_fromJO(JSONObject jo) {
        return null;
    }
}
