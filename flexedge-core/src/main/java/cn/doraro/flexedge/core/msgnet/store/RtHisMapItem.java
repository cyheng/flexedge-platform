package cn.doraro.flexedge.core.msgnet.store;

import cn.doraro.flexedge.core.cxt.JSObMap;

public class RtHisMapItem extends JSObMap {
    String name = null;

    String tagId = null;

    public RtHisMapItem() {
    }

    public RtHisMapItem asParam(String name, String tagid) {
        this.name = name;
        this.tagId = tagid;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public String getTagId() {
        return this.tagId;
    }


}
