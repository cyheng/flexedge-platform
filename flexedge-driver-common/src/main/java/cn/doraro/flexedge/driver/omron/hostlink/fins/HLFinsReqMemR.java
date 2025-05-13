

package cn.doraro.flexedge.driver.omron.hostlink.fins;

import cn.doraro.flexedge.core.util.IBSOutput;
import cn.doraro.flexedge.driver.omron.fins.FinsMode;
import cn.doraro.flexedge.driver.omron.hostlink.HLMsgResp;

public class HLFinsReqMemR extends HLFinsReq {
    int beginAddr;
    int beginBit;
    int itemNum;
    FinsMode.AreaCode areaCode;

    public HLFinsReqMemR(final FinsMode mode) {
        super(mode);
        this.areaCode = null;
    }

    public int getBeginAddr() {
        return this.beginAddr;
    }

    public int getItemNum() {
        return this.itemNum;
    }

    @Override
    protected short getMR() {
        return 1;
    }

    @Override
    protected short getSR() {
        return 1;
    }

    public HLFinsReqMemR asReqR(final String mem_area, final boolean b_bit, final int begin_addr, final int begin_bit, final int item_num) {
        if (b_bit) {
            this.areaCode = this.mode.getAreaCodeBit(mem_area);
        } else {
            this.areaCode = this.mode.getAreaCodeWord(mem_area);
        }
        if (this.areaCode == null) {
            throw new IllegalArgumentException("no AreaCode found with mem area=" + mem_area);
        }
        this.beginAddr = begin_addr;
        this.beginBit = begin_bit;
        this.itemNum = item_num;
        return this;
    }

    public FinsMode.AreaCode getAreaCode() {
        return this.areaCode;
    }

    @Override
    protected void packOutCmdParam(final IBSOutput outputs) {
        final byte[] bs = new byte[6];
        bs[0] = (byte) this.areaCode.getCode();
        HLFinsReq.short2bytes((short) this.beginAddr, bs, 1);
        bs[3] = (byte) this.beginBit;
        HLFinsReq.short2bytes((short) this.itemNum, bs, 4);
        outputs.write(bs);
    }

    @Override
    protected HLMsgResp newRespInstance() {
        return new HLFinsRespMemR(this);
    }
}
