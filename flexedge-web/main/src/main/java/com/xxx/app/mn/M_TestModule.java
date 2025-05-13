package com.xxx.app.mn;

import cn.doraro.flexedge.core.msgnet.MNModule;
import org.json.JSONObject;

public class M_TestModule extends MNModule {

    @Override
    public String getTP() {
        return null;
    }

    @Override
    public String getTPTitle() {
        return null;
    }

    @Override
    public String getColor() {
        return null;
    }

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public boolean isParamReady(StringBuilder failedr) {
        return false;
    }

    @Override
    public JSONObject getParamJO() {
        return null;
    }

    @Override
    protected void setParamJO(JSONObject jo) {

    }

}
