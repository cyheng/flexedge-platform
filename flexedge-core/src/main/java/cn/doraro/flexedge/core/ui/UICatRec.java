package cn.doraro.flexedge.core.ui;

import cn.doraro.flexedge.core.store.record.RecTagParam;

import java.util.List;

public class UICatRec extends UICat {
    public UICatRec(UIManager uim) {
        super(uim);
    }

    @Override
    public String getName() {
        return "_rec";
    }

    @Override
    public String getTitle() {
        return "Tag Recorder";
    }

    @Override
    public String getDesc() {
        return null;
    }

    @Override
    public List<UIItem> listUIItems() {
        for (RecTagParam rtp : uimgr.recmgr.getRecTagParams().values()) {

        }
        return null;
    }


}
