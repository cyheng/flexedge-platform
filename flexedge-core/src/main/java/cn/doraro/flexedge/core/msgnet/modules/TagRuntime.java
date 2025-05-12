package cn.doraro.flexedge.core.msgnet.modules;

import cn.doraro.flexedge.core.msgnet.MNModule;
import org.json.JSONObject;

public class TagRuntime extends MNModule {

    @Override
    public String getTP() {
        return "tag_rt";
    }

    @Override
    public String getTPTitle() {
        return g("tag_rt");
    }

    @Override
    public String getColor() {
        return "#a1cbde";
    }

    @Override
    public String getIcon() {
        return "\\uf02c";
    }

    @Override
    public boolean isParamReady(StringBuilder failedr) {
        return true;
    }

    @Override
    public JSONObject getParamJO() {
        return null;
    }

    @Override
    protected void setParamJO(JSONObject jo) {

    }

}
