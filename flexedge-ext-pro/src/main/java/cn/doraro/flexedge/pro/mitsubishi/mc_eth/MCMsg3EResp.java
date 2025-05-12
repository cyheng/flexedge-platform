// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

import java.io.IOException;
import java.io.InputStream;

public class MCMsg3EResp extends MCMsg3E
{
    private int respLenAscii;
    private int respLenBin;
    private int endCode;
    private RespErr respErr;
    private static int RESP_ERR_LEN_ASCII;
    private static int RESP_ERR_LEN_BIN;
    
    public MCMsg3EResp() {
        this.respLenAscii = -1;
        this.respLenBin = -1;
        this.endCode = -1;
        this.respErr = null;
    }
    
    public MCMsg3EResp asRespLen(final int resp_len_bin, final int resp_len_ascii) {
        this.respLenAscii = resp_len_ascii;
        this.respLenBin = resp_len_bin;
        return this;
    }
    
    public MCMsg3EResp asRespLen(final MCMsg3EReq req) {
        return this.asRespLen(req.calRespLenBin(), req.calRespLenAscii());
    }
    
    public int getEndCode() {
        return this.endCode;
    }
    
    public boolean isRespErr() {
        return this.endCode != 0;
    }
    
    public RespErr getRespErr() {
        return this.respErr;
    }
    
    @Override
    protected boolean readFromAscii(final InputStream inputs, final long timeout, final StringBuilder failedr) throws IOException {
        if (!super.readFromAscii(inputs, timeout, failedr)) {
            return false;
        }
        byte[] bs = new byte[4];
        MCMsg.readBytesTimeout(inputs, timeout, bs, 0, 4);
        final int resp_len = MCMsg.fromAsciiHexBytes(bs, 0, 4);
        if (resp_len != this.respLenAscii && resp_len != MCMsg3EResp.RESP_ERR_LEN_ASCII) {
            failedr.append("resp len is not fit req or err");
            return false;
        }
        bs = new byte[resp_len];
        MCMsg.readBytesTimeout(inputs, timeout, bs, 0, resp_len);
        this.endCode = MCMsg.fromAsciiHexBytes(bs, 0, 4);
        if (this.endCode == 0) {
            return this.parseRespDataAscii(bs);
        }
        this.parseErrAscii(bs);
        failedr.append("end code err=" + Integer.toHexString(this.endCode));
        return false;
    }
    
    @Override
    protected boolean readFromBin(final InputStream inputs, final long timeout, final StringBuilder failedr) throws IOException {
        if (!super.readFromBin(inputs, timeout, failedr)) {
            return false;
        }
        byte[] bs = new byte[2];
        MCMsg.readBytesTimeout(inputs, timeout, bs, 0, 2);
        int resp_len = bs[1] & 0xFF;
        resp_len <<= 9;
        resp_len += (bs[0] & 0xFF);
        if (resp_len != this.respLenBin && resp_len != MCMsg3EResp.RESP_ERR_LEN_BIN) {
            failedr.append("resp len is not fit req or err");
            return false;
        }
        bs = new byte[resp_len];
        MCMsg.readBytesTimeout(inputs, timeout, bs, 0, resp_len);
        this.endCode = (bs[1] & 0xFF);
        this.endCode <<= 8;
        this.endCode += (bs[0] & 0xFF);
        if (this.endCode == 0) {
            return this.parseRespDataBin(bs);
        }
        this.parseErrBin(bs);
        failedr.append("end code err=" + Integer.toHexString(this.endCode));
        return false;
    }
    
    protected final void parseErrAscii(final byte[] bs) {
    }
    
    protected final void parseErrBin(final byte[] bs) {
    }
    
    protected boolean parseRespDataBin(final byte[] bs) {
        return true;
    }
    
    protected boolean parseRespDataAscii(final byte[] bs) {
        return true;
    }
    
    static {
        MCMsg3EResp.RESP_ERR_LEN_ASCII = 22;
        MCMsg3EResp.RESP_ERR_LEN_BIN = 8;
    }
    
    public static class RespErr
    {
        int netCode;
        int plcCode;
        int moduleIO_No;
        int stationNo;
        int cmd;
        int cmdSub;
    }
}
