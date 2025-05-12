// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.nbiot.msg;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

public class WMMsgReport extends WMMsgDT {
    long curMeterVal;
    byte meterUnit;
    byte[] fixStartDT;
    byte fixCollInt;
    int fixNum;
    long[] fixData;

    public WMMsgReport() {
        this.curMeterVal = -1L;
        this.meterUnit = -1;
        this.fixStartDT = null;
        this.fixCollInt = -1;
        this.fixNum = -1;
        this.fixData = null;
    }

    @Override
    protected ArrayList<byte[]> getMsgBody() {
        throw new RuntimeException("no impl");
    }

    public long getCurMeterVal() {
        return this.curMeterVal;
    }

    public byte getMeterUnit() {
        return this.meterUnit;
    }

    public float getMeterUnitVal() {
        switch (this.meterUnit) {
            case 43: {
                return 1.0f;
            }
            case 44: {
                return 0.1f;
            }
            case 45: {
                return 0.01f;
            }
            default: {
                return 0.0f;
            }
        }
    }

    public boolean isTestReport() {
        return this.func[1] == 16;
    }

    @Override
    protected ArrayList<byte[]> parseMsgBody(final InputStream inputs) throws IOException {
        final ArrayList<byte[]> bbs = super.parseMsgBody(inputs);
        if (bbs == null) {
            return null;
        }
        if (inputs.available() < 5) {
            return null;
        }
        byte[] bs = this.readLenTimeout(inputs, 5);
        bbs.add(bs);
        this.curMeterVal = this.bcd2long(bs, 0, 4);
        this.meterUnit = bs[4];
        if (!this.isTestReport()) {
            bs = this.readLenTimeout(inputs, 8);
            bbs.add(bs);
            System.arraycopy(bs, 0, this.fixStartDT = new byte[6], 0, 6);
            this.fixCollInt = bs[6];
            this.fixNum = (bs[7] & 0xFF);
            final int av_len = inputs.available();
            if (av_len < this.fixNum * 4) {
                return null;
            }
            final byte[] fdbs = this.readLenTimeout(inputs, this.fixNum * 4);
            this.fixData = new long[this.fixNum];
            for (int i = 0; i < this.fixNum; ++i) {
                this.fixData[i] = this.bcd2long(fdbs, i * 4, 4);
            }
            bbs.add(fdbs);
        }
        final byte[] z34 = this.readLenTimeout(inputs, 9);
        bbs.add(z34);
        final byte[] z35 = this.readLenTimeout(inputs, 9);
        bbs.add(z35);
        final byte[] z36 = this.readLenTimeout(inputs, 2);
        bbs.add(z36);
        final byte[] z37 = this.readLenTimeout(inputs, 2);
        bbs.add(z37);
        final byte[] z38 = this.readLenTimeout(inputs, 15);
        bbs.add(z38);
        final byte[] z39 = this.readLenTimeout(inputs, 15);
        bbs.add(z39);
        final byte[] z40 = this.readLenTimeout(inputs, 20);
        bbs.add(z40);
        final byte[] z41 = this.readLenTimeout(inputs, 1);
        bbs.add(z41);
        final byte[] z42 = this.readLenTimeout(inputs, 2);
        bbs.add(z42);
        final byte[] z43 = this.readLenTimeout(inputs, 2);
        bbs.add(z43);
        return bbs;
    }

    public WMMsgReceipt createReceipt(final boolean bcontinue) {
        final WMMsgReceipt receipt = new WMMsgReceipt();
        receipt.setMeterAddr(this.getMeterAddr());
        if (this.isTestReport()) {
            receipt.setReceiptTp(2);
        } else {
            receipt.setReceiptTp(0);
        }
        receipt.setContinue(bcontinue);
        receipt.setMsgDT(new Date());
        return receipt;
    }

    @Override
    public String toString() {
        String ret = super.toString();
        ret = ret + " cur_val=" + this.curMeterVal;
        ret = ret + " unit=" + this.getMeterUnitVal();
        return "Report:" + ret;
    }
}
