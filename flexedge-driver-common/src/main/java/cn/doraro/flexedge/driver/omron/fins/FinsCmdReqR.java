

package cn.doraro.flexedge.driver.omron.fins;

import cn.doraro.flexedge.core.util.IBSOutput;

public class FinsCmdReqR extends FinsCmdReq {
    short memAreaCode;
    int beginAddr;
    int itemNum;

    public FinsCmdReqR(final FinsMode fins_mode) {
        super(fins_mode);
    }

    public FinsCmdReqR asReqR(final String mem_area, final int begin_addr, final int item_num) {
        final FinsMode.AreaCode ac = this.mode.getAreaCodeBit(mem_area);
        if (ac == null) {
            throw new IllegalArgumentException("no AreaCode found with mem area=" + mem_area);
        }
        this.memAreaCode = ac.getCode();
        this.beginAddr = begin_addr;
        this.itemNum = item_num;
        return this;
    }

    @Override
    protected void writeParam(final IBSOutput outputs) {
        final byte[] bs = new byte[6];
        bs[0] = (byte) this.memAreaCode;
        FinsCmd.int2byte3(this.beginAddr, bs, 1);
        FinsCmd.short2bytes((short) this.itemNum, bs, 4);
        outputs.write(bs);
    }

    @Override
    protected int getParamBytesNum() {
        return 6;
    }
}
