

package cn.doraro.flexedge.driver.s7.ppi;

public abstract class PPIMsgReq extends PPIMsg {
    short da;
    short sa;
    PPIMemTp memTp;

    public PPIMsgReq() {
        this.sa = 0;
    }

    public abstract short getFC();

    public PPIMsgReq withSorAddr(final short sa) {
        this.sa = sa;
        return this;
    }

    public PPIMsgReq withDestAddr(final short da) {
        this.da = da;
        return this;
    }

    public abstract int getRetOffsetBytes();
}
