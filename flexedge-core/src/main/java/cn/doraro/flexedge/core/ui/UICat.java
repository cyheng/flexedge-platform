package cn.doraro.flexedge.core.ui;

import java.util.List;

/**
 * @author jason.zhu
 */
public abstract class UICat {
    UIManager uimgr = null;

    public UICat(UIManager uim) {
        this.uimgr = uim;
    }

    public abstract String getName();

    public abstract String getTitle();

    public abstract String getDesc();

    public abstract List<UIItem> listUIItems();
}
