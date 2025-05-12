// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common.modbus.sim;

import cn.doraro.flexedge.core.cxt.JsProp;
import cn.doraro.flexedge.core.sim.SimTag;
import cn.doraro.flexedge.core.util.xmldata.data_class;
import cn.doraro.flexedge.core.util.xmldata.data_val;

import java.util.List;

@data_class
public class SlaveTag extends SimTag {
    SlaveDevSeg relatedSeg;
    @data_val
    int regIdx;

    public SlaveTag() {
        this.relatedSeg = null;
        this.regIdx = -1;
    }

    public SlaveTag(final String name, final int regidx) {
        super(name);
        this.relatedSeg = null;
        this.regIdx = -1;
        this.regIdx = regidx;
    }

    public int getRegIdx() {
        return this.regIdx;
    }

    public SlaveDevSeg getRelatedSeg() {
        return this.relatedSeg;
    }

    public Class<?> getValueTp() {
        if (this.relatedSeg.isBoolData()) {
            return Boolean.class;
        }
        return Short.class;
    }

    public Object getValue() {
        return this.relatedSeg.getSlaveData(this.regIdx);
    }

    public void setValue(final Object val) {
        this.relatedSeg.setSlaveDataStr(this.regIdx, val.toString());
    }

    public Object JS_get(final String key) {
        final Object r = super.JS_get(key);
        if (r != null) {
            return r;
        }
        switch (key) {
            case "_regidx": {
                return this.regIdx;
            }
            default: {
                return null;
            }
        }
    }

    public List<JsProp> JS_props() {
        final List<JsProp> rets = super.JS_props();
        rets.add(new JsProp("_regidx", (Object) null, (Class) Integer.class, false, "RegIdx", ""));
        return rets;
    }
}
