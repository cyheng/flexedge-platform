package cn.doraro.flexedge.core.basic.ce;

import org.json.JSONObject;

import java.util.List;

public interface IExchgModule {
    public String getExchgModuleName();

    public String getExchgModuleTitle();

    public List<ExchgObj> listExchgObjs();

    public boolean formExchgJO(JSONObject jo);
}
