package cn.doraro.flexedge.core.util.xmldata;

import org.json.JSONObject;

public interface IJSONObj {
    public JSONObject toJSONObj();

    public boolean fromJSONObj(JSONObject job);
}
