

package cn.doraro.flexedge.driver.common.modbus;

import kotlin.NotImplementedError;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class ModbusCmd {
    public static final short MODBUS_FC_READ_COILS = 1;
    public static final short MODBUS_FC_READ_DISCRETE_INPUT = 2;
    public static final short MODBUS_FC_READ_HOLD_REG = 3;
    public static final short MODBUS_FC_READ_INPUT_REG = 4;
    public static final short MODBUS_FC_WRITE_SINGLE_COIL = 5;
    public static final short MODBUS_FC_WRITE_SINGLE_REG = 6;
    public static final short MODBUS_FC_WRITE_MULTI_COIL = 15;
    public static final short MODBUS_FC_WRITE_MULTI_REG = 16;
    public static final int ERR_RECV_TIMEOUT = -1;
    public static final int ERR_RECV_END_TIMEOUT = -2;
    public static final int ERR_CRC = -3;
    public static final int ERR_RET = -4;
    public static final long SCAN_INTERVER_DEFAULT = 100L;
    static final int MAX_SCAN_MULTI = 50;
    static final int RECV_TIMEOUT_MIN = 20;
    static final int RECV_TIMEOUT_DEFAULT = 1000;
    static final int RECV_END_TIMEOUT_DEFAULT = 20;
    static int[] auchCRCHi;
    static int[] auchCRCLo;

    static {
        ModbusCmd.auchCRCHi = new int[]{0, 193, 129, 64, 1, 192, 128, 65, 1, 192, 128, 65, 0, 193, 129, 64, 1, 192, 128, 65, 0, 193, 129, 64, 0, 193, 129, 64, 1, 192, 128, 65, 1, 192, 128, 65, 0, 193, 129, 64, 0, 193, 129, 64, 1, 192, 128, 65, 0, 193, 129, 64, 1, 192, 128, 65, 1, 192, 128, 65, 0, 193, 129, 64, 1, 192, 128, 65, 0, 193, 129, 64, 0, 193, 129, 64, 1, 192, 128, 65, 0, 193, 129, 64, 1, 192, 128, 65, 1, 192, 128, 65, 0, 193, 129, 64, 0, 193, 129, 64, 1, 192, 128, 65, 1, 192, 128, 65, 0, 193, 129, 64, 1, 192, 128, 65, 0, 193, 129, 64, 0, 193, 129, 64, 1, 192, 128, 65, 1, 192, 128, 65, 0, 193, 129, 64, 0, 193, 129, 64, 1, 192, 128, 65, 0, 193, 129, 64, 1, 192, 128, 65, 1, 192, 128, 65, 0, 193, 129, 64, 0, 193, 129, 64, 1, 192, 128, 65, 1, 192, 128, 65, 0, 193, 129, 64, 1, 192, 128, 65, 0, 193, 129, 64, 0, 193, 129, 64, 1, 192, 128, 65, 0, 193, 129, 64, 1, 192, 128, 65, 1, 192, 128, 65, 0, 193, 129, 64, 1, 192, 128, 65, 0, 193, 129, 64, 0, 193, 129, 64, 1, 192, 128, 65, 1, 192, 128, 65, 0, 193, 129, 64, 0, 193, 129, 64, 1, 192, 128, 65, 0, 193, 129, 64, 1, 192, 128, 65, 1, 192, 128, 65, 0, 193, 129, 64};
        ModbusCmd.auchCRCLo = new int[]{0, 192, 193, 1, 195, 3, 2, 194, 198, 6, 7, 199, 5, 197, 196, 4, 204, 12, 13, 205, 15, 207, 206, 14, 10, 202, 203, 11, 201, 9, 8, 200, 216, 24, 25, 217, 27, 219, 218, 26, 30, 222, 223, 31, 221, 29, 28, 220, 20, 212, 213, 21, 215, 23, 22, 214, 210, 18, 19, 211, 17, 209, 208, 16, 240, 48, 49, 241, 51, 243, 242, 50, 54, 246, 247, 55, 245, 53, 52, 244, 60, 252, 253, 61, 255, 63, 62, 254, 250, 58, 59, 251, 57, 249, 248, 56, 40, 232, 233, 41, 235, 43, 42, 234, 238, 46, 47, 239, 45, 237, 236, 44, 228, 36, 37, 229, 39, 231, 230, 38, 34, 226, 227, 35, 225, 33, 32, 224, 160, 96, 97, 161, 99, 163, 162, 98, 102, 166, 167, 103, 165, 101, 100, 164, 108, 172, 173, 109, 175, 111, 110, 174, 170, 106, 107, 171, 105, 169, 168, 104, 120, 184, 185, 121, 187, 123, 122, 186, 190, 126, 127, 191, 125, 189, 188, 124, 180, 116, 117, 181, 119, 183, 182, 118, 114, 178, 179, 115, 177, 113, 112, 176, 80, 144, 145, 81, 147, 83, 82, 146, 150, 86, 87, 151, 85, 149, 148, 84, 156, 92, 93, 157, 95, 159, 158, 94, 90, 154, 155, 91, 153, 89, 88, 152, 136, 72, 73, 137, 75, 139, 138, 74, 78, 142, 143, 79, 141, 77, 76, 140, 68, 132, 133, 69, 135, 71, 70, 134, 130, 66, 67, 131, 65, 129, 128, 64};
    }

    protected long scanIntervalMS;
    protected long maxRecvTOMS;
    protected long recvTimeout;
    protected boolean bFixTO;
    protected long recvEndTimeout;
    protected long reqInterMS;
    protected short slaveAddr;
    protected int tryTimes;
    protected Protocol protocal;
    protected byte[] mbap4Tcp;
    protected transient byte[] mbuss_adu;
    protected transient int mbuss_rnum;
    protected transient int lastTcpCC;
    ModbusRunner belongToRunner;
    transient Object relatedObj;
    transient long runOnceT;
    transient int errCount;
    int urx_end;
    int urx_count_last;
    int b_in_rx;
    long tt_c;
    private int scanErrIntervalMulti;
    private boolean bFixEndTO;
    private transient long lastRunT;

    protected ModbusCmd() {
        this.scanIntervalMS = 100L;
        this.maxRecvTOMS = 60000L;
        this.scanErrIntervalMulti = 0;
        this.recvTimeout = 1000L;
        this.bFixTO = true;
        this.recvEndTimeout = 20L;
        this.reqInterMS = 0L;
        this.bFixEndTO = true;
        this.slaveAddr = 0;
        this.tryTimes = 0;
        this.protocal = Protocol.rtu;
        this.mbap4Tcp = null;
        this.mbuss_adu = new byte[300];
        this.mbuss_rnum = 0;
        this.lastRunT = 0L;
        this.belongToRunner = null;
        this.relatedObj = null;
        this.runOnceT = -1L;
        this.errCount = 0;
        this.lastTcpCC = 0;
        this.urx_end = 1;
        this.urx_count_last = 0;
        this.b_in_rx = 0;
        this.tt_c = 0L;
    }

    public ModbusCmd(final long scan_inter_ms, final int dev_addr) {
        this.scanIntervalMS = 100L;
        this.maxRecvTOMS = 60000L;
        this.scanErrIntervalMulti = 0;
        this.recvTimeout = 1000L;
        this.bFixTO = true;
        this.recvEndTimeout = 20L;
        this.reqInterMS = 0L;
        this.bFixEndTO = true;
        this.slaveAddr = 0;
        this.tryTimes = 0;
        this.protocal = Protocol.rtu;
        this.mbap4Tcp = null;
        this.mbuss_adu = new byte[300];
        this.mbuss_rnum = 0;
        this.lastRunT = 0L;
        this.belongToRunner = null;
        this.relatedObj = null;
        this.runOnceT = -1L;
        this.errCount = 0;
        this.lastTcpCC = 0;
        this.urx_end = 1;
        this.urx_count_last = 0;
        this.b_in_rx = 0;
        this.tt_c = 0L;
        this.slaveAddr = (short) (dev_addr & 0xFF);
        if (scan_inter_ms > 0L) {
            this.scanIntervalMS = scan_inter_ms;
        }
    }

    public static int modbus_crc16_check(final byte[] pmsg, final int msglen) {
        int hi = 255;
        int lo = 255;
        for (int i = 0; i < msglen; ++i) {
            final int idx = hi ^ (pmsg[i] & 0xFF);
            hi = (lo ^ ModbusCmd.auchCRCHi[idx]);
            lo = ModbusCmd.auchCRCLo[idx];
        }
        return hi << 8 | lo;
    }

    public static int modbus_crc16_check_seg(final byte[] phead, final int hlen, final byte[] pmsg, final int msglen, final byte[] ptail, final int tlen) {
        int hi = 255;
        int lo = 255;
        if (phead != null) {
            for (int i = 0; i < hlen; ++i) {
                final int idx = hi ^ (phead[i] & 0xFF);
                hi = (lo ^ ModbusCmd.auchCRCHi[idx]);
                lo = ModbusCmd.auchCRCLo[idx];
            }
        }
        if (pmsg != null) {
            for (int i = 0; i < msglen; ++i) {
                final int idx = hi ^ (pmsg[i] & 0xFF);
                hi = (lo ^ ModbusCmd.auchCRCHi[idx]);
                lo = ModbusCmd.auchCRCLo[idx];
            }
        }
        if (ptail != null) {
            for (int i = 0; i < tlen; ++i) {
                final int idx = hi ^ (ptail[i] & 0xFF);
                hi = (lo ^ ModbusCmd.auchCRCHi[idx]);
                lo = ModbusCmd.auchCRCLo[idx];
            }
        }
        return hi << 8 | lo;
    }

    public static ModbusCmd parseRequest(final byte[] req, final int[] pl) {
        if (req == null || req.length <= 1) {
            return null;
        }
        final short fc = (short) (req[1] & 0xFF);
        switch (fc) {
            case 1:
            case 2: {
                return ModbusCmdReadBits.createReqMC(req, pl);
            }
            case 3:
            case 4: {
                return ModbusCmdReadWords.createReqMC(req, pl);
            }
            case 5: {
                return ModbusCmdWriteBit.createReqMC(req, pl);
            }
            case 6: {
                return ModbusCmdWriteWord.createReqMC(req, pl);
            }
            default: {
                return null;
            }
        }
    }

    public static int addCRC(final byte[] data, final int dlen) {
        final int cr = modbus_crc16_check(data, dlen);
        data[dlen] = (byte) (cr >> 8 & 0xFF);
        data[dlen + 1] = (byte) (cr & 0xFF);
        return dlen + 2;
    }

    public static byte[] createRespError(final ModbusCmd mc, final short addr, final short req_fc) {
        switch (mc.getProtocol()) {
            case tcp: {
                return createRespErrorTCP(mc.mbap4Tcp, addr, req_fc);
            }
            case ascii: {
                throw new NotImplementedError();
            }
            default: {
                return createRespErrorRTU(addr, req_fc);
            }
        }
    }

    protected static byte[] createRespErrorTCP(final byte[] mbap, final short addr, final short req_fc) {
        final byte[] bs = {mbap[0], mbap[1], mbap[2], mbap[3], 0, 3, (byte) addr, (byte) (req_fc + 128), 4};
        return bs;
    }

    protected static byte[] createRespErrorRTU(final short addr, final short req_fc) {
        final byte[] bs = {(byte) addr, (byte) (req_fc + 128), 4, 0, 0};
        final int crc = modbus_crc16_check(bs, 3);
        bs[3] = (byte) (crc >> 8 & 0xFF);
        bs[4] = (byte) (crc & 0xFF);
        return bs;
    }

    public ModbusRunner getComRunner() {
        return this.belongToRunner;
    }

    public Protocol getProtocol() {
        return this.protocal;
    }

    public void setProtocol(final Protocol p) {
        if (p == Protocol.ascii) {
            throw new IllegalArgumentException("not support ascii protocol");
        }
        this.protocal = p;
    }

    public abstract short getFC();

    public Object getRelatedObj() {
        return this.relatedObj;
    }

    public void setRelatedObj(final Object o) {
        this.relatedObj = o;
    }

    public short getDevAddr() {
        return this.slaveAddr;
    }

    public long getScanIntervalMS() {
        return this.scanIntervalMS + 100 * this.scanErrIntervalMulti;
    }

    public void setScanIntervalMS(final long sms) {
        this.scanIntervalMS = sms;
    }

    public long getRecvTimeout() {
        return this.recvTimeout;
    }

    public void setRecvTimeout(final long rto) {
        if (rto <= 0L) {
            this.recvTimeout = 1000L;
            this.bFixTO = false;
        } else {
            this.recvTimeout = rto;
            this.bFixTO = true;
        }
    }

    public long getRecvEndTimeout() {
        return this.recvEndTimeout;
    }

    public void setRecvEndTimeout(final long rto) {
        if (rto <= 0L) {
            this.recvEndTimeout = 20L;
            this.bFixEndTO = false;
        } else {
            this.recvEndTimeout = rto;
            this.bFixEndTO = true;
        }
    }

    public int getTryTimes() {
        return this.tryTimes;
    }

    public void setTryTimes(final int tt) {
        this.tryTimes = tt;
    }

    public void setMaxRecvTO(final long ms) {
        if (ms > 0L) {
            this.maxRecvTOMS = ms;
        }
    }

    public boolean isReadCmd() {
        return false;
    }

    public Object[] getReadVals() {
        return null;
    }

    public boolean tickCanRun() {
        final long ct = System.currentTimeMillis();
        if (ct - this.lastRunT > this.getScanIntervalMS()) {
            this.lastRunT = ct;
            return true;
        }
        return false;
    }

    public long getRunOnceTime() {
        return this.runOnceT;
    }

    public void setRunOnceTime(final long t) {
        this.runOnceT = t;
    }

    public int getErrCount() {
        return this.errCount;
    }

    private void increaseErrCount() {
        if (this.errCount >= Integer.MAX_VALUE) {
            return;
        }
        ++this.errCount;
    }

    public void doCmd(final OutputStream outs, final InputStream ins) throws Exception {
        try {
            this.doCmdInner(outs, ins);
            final Object[] ovs = this.getReadVals();
            if (ovs != null) {
                this.errCount = 0;
            } else {
                this.increaseErrCount();
            }
        } catch (final Exception e) {
            this.increaseErrCount();
            throw e;
        }
    }

    private void doCmdInner(final OutputStream outs, final InputStream ins) throws Exception {
        int r = 0;
        switch (this.protocal) {
            case rtu: {
                r = this.reqRespRTU(outs, ins);
                break;
            }
            case tcp: {
                r = this.reqRespTCP(outs, ins);
                break;
            }
            case ascii: {
                r = this.reqRespASCII(outs, ins);
                break;
            }
        }
        if (!this.isReadCmd()) {
            return;
        }
        if (r < 0) {
            switch (r) {
                case -1: {
                    if (this.bFixTO) {
                        break;
                    }
                    if (this.recvTimeout > 100L) {
                        this.recvTimeout += 100L;
                    } else if (this.recvTimeout > 10L) {
                        this.recvTimeout += 10L;
                    } else {
                        ++this.recvTimeout;
                    }
                    if (this.recvTimeout < this.maxRecvTOMS) {
                        break;
                    }
                    this.recvTimeout = this.maxRecvTOMS;
                    ++this.scanErrIntervalMulti;
                    if (this.scanErrIntervalMulti > 50) {
                        this.scanErrIntervalMulti = 50;
                        break;
                    }
                    break;
                }
                case -2: {
                    if (!this.bFixEndTO) {
                        ++this.recvEndTimeout;
                        break;
                    }
                    break;
                }
            }
        }
        if (r > 0 && this.scanErrIntervalMulti > 0) {
            this.scanErrIntervalMulti = 0;
            if (!this.bFixTO) {
                this.recvTimeout = 1000L;
            }
        }
    }

    public abstract int calRespLenRTU();

    protected abstract int reqRespRTU(final OutputStream p0, final InputStream p1) throws Exception;

    protected abstract int reqRespTCP(final OutputStream p0, final InputStream p1) throws Exception;

    protected int reqRespASCII(final OutputStream ous, final InputStream ins) throws Exception {
        return 0;
    }

    protected void clearInputStream(final InputStream inputs) throws IOException {
        final byte[] tmpbs = new byte[100];
        int avn = 0;
        while ((avn = inputs.available()) > 0) {
            inputs.read(tmpbs);
        }
        this.mbuss_rnum = 0;
    }

    protected int chkCurRecvedLen(final InputStream inputs) throws IOException {
        final int avn = inputs.available();
        if (avn > 0) {
            final int n = inputs.read(this.mbuss_adu, this.mbuss_rnum, avn);
            this.mbuss_rnum += n;
        }
        return this.mbuss_rnum;
    }

    protected void com_stream_recv_start(final InputStream inputs) throws IOException {
        this.urx_end = 0;
        this.urx_count_last = 0;
        this.b_in_rx = 1;
        this.tt_c = System.currentTimeMillis();
    }

    protected boolean com_stream_in_recving() {
        return this.b_in_rx > 0;
    }

    protected void com_stream_end() {
        this.b_in_rx = 0;
        this.urx_end = 1;
    }

    protected int com_stream_recv_chk_len_timeout(final InputStream inputs) throws IOException {
        int rc = 0;
        if (this.urx_end != 0) {
            return 0;
        }
        rc = this.chkCurRecvedLen(inputs);
        if (rc == 0) {
            if (System.currentTimeMillis() - this.tt_c > this.recvTimeout) {
                this.com_stream_end();
            }
            try {
                Thread.sleep(1L);
            } catch (final Exception ex) {
            }
            return 0;
        }
        if (rc > this.urx_count_last) {
            this.urx_count_last = rc;
            this.tt_c = System.currentTimeMillis();
            return rc;
        }
        try {
            Thread.sleep(1L);
        } catch (final Exception ex2) {
        }
        if (System.currentTimeMillis() - this.tt_c > this.recvEndTimeout) {
            this.com_stream_end();
            return rc;
        }
        return rc;
    }

    @Override
    public String toString() {
        return "{dev:" + this.slaveAddr + ",recv_to:" + this.recvTimeout + ",recv_end_to:" + this.recvEndTimeout + "}";
    }

    public enum Protocol {
        rtu(0),
        tcp(1),
        ascii(2);

        private final int val;

        private Protocol(final int v) {
            this.val = v;
        }

        public int getIntValue() {
            return this.val;
        }
    }
}
