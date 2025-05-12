// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.fins;

public abstract class FinsCmdReq extends FinsCmd {
    public FinsCmdReq(final FinsMode fins_mode) {
        super(fins_mode);
    }

    @Override
    protected short getMRC() {
        return 1;
    }

    @Override
    protected short getSRC() {
        return 1;
    }

    @Override
    protected final short getICF() {
        if (this.isNeedResp()) {
            return 128;
        }
        return 129;
    }

    protected boolean isNeedResp() {
        return true;
    }
}
