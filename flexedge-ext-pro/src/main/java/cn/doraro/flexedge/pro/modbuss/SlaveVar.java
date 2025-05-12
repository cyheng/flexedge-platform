// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.modbuss;

import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.util.xmldata.data_class;
import cn.doraro.flexedge.core.util.xmldata.data_val;

@data_class
public class SlaveVar {
    public static UAVal.ValTP[] VAL_TPS;

    static {
        SlaveVar.VAL_TPS = new UAVal.ValTP[]{UAVal.ValTP.vt_int16, UAVal.ValTP.vt_int32, UAVal.ValTP.vt_int64, UAVal.ValTP.vt_uint16, UAVal.ValTP.vt_uint32, UAVal.ValTP.vt_uint64, UAVal.ValTP.vt_float, UAVal.ValTP.vt_double};
    }

    protected SlaveDev belongTo;
    SlaveDevSeg relatedSeg;
    @data_val
    int col;
    @data_val(param_name = "idx")
    int regIdx;
    @data_val
    String name;
    @data_val(param_name = "bind_name")
    String bindName;
    UAVal.ValTP valTP;

    public SlaveVar() {
        this.relatedSeg = null;
        this.col = 0;
        this.regIdx = -1;
        this.name = null;
        this.bindName = null;
        this.valTP = UAVal.ValTP.vt_uint16;
        this.belongTo = null;
    }

    public SlaveVar(final String name, final int regidx) {
        this.relatedSeg = null;
        this.col = 0;
        this.regIdx = -1;
        this.name = null;
        this.bindName = null;
        this.valTP = UAVal.ValTP.vt_uint16;
        this.belongTo = null;
        this.name = name;
        this.regIdx = regidx;
    }

    @data_val(param_name = "valtp")
    private String get_ValTP() {
        return this.valTP.getStr();
    }

    @data_val(param_name = "valtp")
    private void set_ValTP(final String vtp) {
        this.valTP = UAVal.getValTp(vtp);
        if (this.valTP == null) {
            this.valTP = UAVal.ValTP.vt_uint16;
        }
    }

    public SlaveDev getBelongTo() {
        return this.belongTo;
    }

    public int getColumn() {
        return this.col;
    }

    public String getName() {
        return this.name;
    }

    public SlaveVar asName(final String n) {
        this.name = n;
        return this;
    }

    public String getBindName() {
        return this.bindName;
    }

    public SlaveVar asBindName(final String bn) {
        this.bindName = bn;
        return this;
    }

    public int getRegIdx() {
        return this.regIdx;
    }

    public SlaveDevSeg getRelatedSeg() {
        return this.relatedSeg;
    }

    public UAVal.ValTP getValueTp() {
        if (this.relatedSeg.isBoolData()) {
            return UAVal.ValTP.vt_bool;
        }
        return this.valTP;
    }
}
