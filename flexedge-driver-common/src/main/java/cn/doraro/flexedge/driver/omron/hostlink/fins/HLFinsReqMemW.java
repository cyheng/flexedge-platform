

package cn.doraro.flexedge.driver.omron.hostlink.fins;

import cn.doraro.flexedge.core.util.IBSOutput;
import cn.doraro.flexedge.core.util.xmldata.DataUtil;
import cn.doraro.flexedge.driver.omron.fins.FinsMode;
import cn.doraro.flexedge.driver.omron.hostlink.HLMsgResp;

import java.util.List;

public class HLFinsReqMemW extends HLFinsReq {
    int beginAddr;
    int beginBit;
    int itemNum;
    List<Boolean> bitVals;
    List<Short> wordVals;
    FinsMode.AreaCode areaCode;
    boolean bBit;

    public HLFinsReqMemW(final FinsMode mode) {
        super(mode);
        this.beginBit = 0;
        this.bitVals = null;
        this.wordVals = null;
        this.areaCode = null;
        this.bBit = false;
    }

    @Override
    protected short getMR() {
        return 1;
    }

    @Override
    protected short getSR() {
        return 2;
    }

    public HLFinsReqMemW asReqWBit(final String mem_area, final int begin_addr, final int begin_bit, final int item_num, final List<Boolean> bitvals) {
        this.areaCode = this.mode.getAreaCodeBit(mem_area);
        if (this.areaCode == null) {
            throw new IllegalArgumentException("no AreaCode found with mem area=" + mem_area);
        }
        this.beginAddr = begin_addr;
        this.beginBit = begin_bit;
        this.itemNum = item_num;
        this.bitVals = bitvals;
        this.bBit = true;
        return this;
    }

    public HLFinsReqMemW asReqWWord(final String mem_area, final int begin_addr, final int item_num, final List<Short> wvals) {
        this.areaCode = this.mode.getAreaCodeWord(mem_area);
        if (this.areaCode == null) {
            throw new IllegalArgumentException("no AreaCode found with mem area=" + mem_area);
        }
        this.beginAddr = begin_addr;
        this.beginBit = 0;
        this.itemNum = item_num;
        this.wordVals = wvals;
        this.bBit = false;
        return this;
    }

    public FinsMode.AreaCode getAreaCode() {
        return this.areaCode;
    }

    @Override
    protected void packOutCmdParam(final IBSOutput outputs) {
        byte[] bs = null;
        if (this.bBit) {
            bs = new byte[6 + this.itemNum];
            bs[0] = (byte) this.areaCode.getCode();
            HLFinsReq.short2bytes((short) this.beginAddr, bs, 1);
            bs[3] = (byte) this.beginBit;
            HLFinsReq.short2bytes((short) this.itemNum, bs, 4);
            for (int i = 0; i < this.itemNum; ++i) {
                bs[6 + i] = (byte) (((boolean) this.bitVals.get(i)) ? 1 : 0);
            }
        } else {
            bs = new byte[6 + this.itemNum * 2];
            bs[0] = (byte) this.areaCode.getCode();
            HLFinsReq.short2bytes((short) this.beginAddr, bs, 1);
            bs[3] = (byte) this.beginBit;
            HLFinsReq.short2bytes((short) this.itemNum, bs, 4);
            for (int i = 0; i < this.itemNum; ++i) {
                DataUtil.shortToBytes((short) this.wordVals.get(i), bs, 6 + i * 2);
            }
        }
        outputs.write(bs);
    }

    @Override
    protected HLMsgResp newRespInstance() {
        return new HLFinsRespOnlyEnd(this);
    }
}
