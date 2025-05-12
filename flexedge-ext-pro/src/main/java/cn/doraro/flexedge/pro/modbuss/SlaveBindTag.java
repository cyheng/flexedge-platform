// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.modbuss;

import cn.doraro.flexedge.core.UAPrj;
import cn.doraro.flexedge.core.UATag;
import cn.doraro.flexedge.core.util.xmldata.data_class;
import cn.doraro.flexedge.core.util.xmldata.data_val;

@data_class
public class SlaveBindTag {
    protected SlaveDev belongTo;
    SlaveDevSeg relatedSeg;
    @data_val(param_name = "idx")
    int regIdx;
    @data_val(param_name = "tag")
    String tag;
    private UATag uaTag;
    private boolean uaTagGit;

    public SlaveBindTag() {
        this.relatedSeg = null;
        this.regIdx = -1;
        this.tag = null;
        this.belongTo = null;
        this.uaTag = null;
        this.uaTagGit = false;
    }

    public SlaveBindTag(final String tag, final int regidx) {
        this.relatedSeg = null;
        this.regIdx = -1;
        this.tag = null;
        this.belongTo = null;
        this.uaTag = null;
        this.uaTagGit = false;
        this.tag = tag;
        this.regIdx = regidx;
    }

    public SlaveDev getBelongTo() {
        return this.belongTo;
    }

    public String getTagPath() {
        return this.tag;
    }

    public SlaveBindTag asTagPath(final String bn) {
        this.tag = bn;
        return this;
    }

    public int getRegIdx() {
        return this.regIdx;
    }

    public SlaveDevSeg getRelatedSeg() {
        return this.relatedSeg;
    }

    public UATag getTag() {
        if (this.uaTagGit) {
            return this.uaTag;
        }
        try {
            final UAPrj prj = (UAPrj) this.belongTo.belongTo.getBelongTo().getContainer();
            return this.uaTag = prj.getTagByPath(this.tag);
        } finally {
            this.uaTagGit = true;
        }
    }
}
