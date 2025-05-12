// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.eth;

import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.xmldata.DataUtil;

import java.io.IOException;

public class S7MsgWrite extends S7Msg {
    S7MemTp areaMemtp;
    int dbNum;
    int pos;
    int bitIdx;
    int writeNum;
    Object writeObj;
    byte[] writeV;

    public S7MsgWrite() {
        this.bitIdx = -1;
        this.writeObj = null;
    }

    public static void writeArea(final S7TcpConn conn, final S7MemTp area_memtp, final int db_num, int pos, final int readnum, final byte[] bs) throws IOException, S7Exception {
        int ele_byte_n = 1;
        if (area_memtp == S7MemTp.C || area_memtp == S7MemTp.T) {
            ele_byte_n = 2;
        }
        int offset = 0;
        final int ele_max = (conn.pduLen - 35) / ele_byte_n;
        int ele_num;
        for (int ele_tot = readnum; ele_tot > 0; ele_tot -= ele_num, pos += ele_num * ele_byte_n) {
            ele_num = ele_tot;
            if (ele_num > ele_max) {
                ele_num = ele_max;
            }
            final int data_size = ele_num * ele_byte_n;
            final int iso_size = 35 + data_size;
            System.arraycopy(S7MsgWrite.RW35, 0, conn.PDU, 0, 35);
            S7Util.setUInt16(conn.PDU, 2, iso_size);
            int len = data_size + 4;
            S7Util.setUInt16(conn.PDU, 15, len);
            conn.PDU[17] = 5;
            conn.PDU[18] = 1;
            conn.PDU[27] = (byte) area_memtp.getVal();
            if (area_memtp == S7MemTp.DB) {
                S7Util.setUInt16(conn.PDU, 25, db_num);
            } else if (area_memtp == S7MemTp.V) {
                S7Util.setUInt16(conn.PDU, 25, 1);
            }
            int addr;
            if (area_memtp == S7MemTp.C || area_memtp == S7MemTp.T) {
                addr = pos;
                len = data_size;
                if (area_memtp == S7MemTp.C) {
                    conn.PDU[22] = 28;
                } else {
                    conn.PDU[22] = 29;
                }
            } else {
                addr = pos * 8;
                len = data_size * 8;
            }
            S7Util.setUInt16(conn.PDU, 23, ele_num);
            conn.PDU[30] = (byte) (addr & 0xFF);
            addr >>= 8;
            conn.PDU[29] = (byte) (addr & 0xFF);
            addr >>= 8;
            conn.PDU[28] = (byte) (addr & 0xFF);
            S7Util.setUInt16(conn.PDU, 33, len);
            System.arraycopy(bs, offset, conn.PDU, 35, data_size);
            if (S7MsgWrite.log.isTraceEnabled()) {
                final String tmps = Convert.byteArray2HexStr(conn.PDU, 0, iso_size, " ");
                S7MsgWrite.log.trace("S7Msg write ->" + tmps);
            }
            conn.send(conn.PDU, iso_size);
            len = S7Msg.recvIsoPacket(conn);
            if (S7MsgWrite.log.isTraceEnabled() && len > 0 && len < 1000) {
                final String tmps = Convert.byteArray2HexStr(conn.PDU, 0, len, " ");
                S7MsgWrite.log.trace("S7Msg recv <-" + tmps);
            }
            if (len != 22) {
                throw new S7Exception("invalid pdu");
            }
            if (S7Util.getUInt16(conn.PDU, 17) != 0 || conn.PDU[21] != -1) {
                throw new S7Exception("write date err");
            }
            offset += data_size;
        }
    }

    public static void writeAreaBit(final S7TcpConn conn, final S7MemTp area_memtp, final int db_num, int pos, final int bitidx, final int wnum, final boolean v) throws IOException, S7Exception {
        int ele_byte_n = 1;
        if (area_memtp == S7MemTp.C || area_memtp == S7MemTp.T) {
            ele_byte_n = 2;
        }
        int offset = 0;
        final int ele_max = (conn.pduLen - 35) / ele_byte_n;
        int ele_num;
        for (int ele_tot = wnum; ele_tot > 0; ele_tot -= ele_num, pos += ele_num * ele_byte_n) {
            ele_num = ele_tot;
            if (ele_num > ele_max) {
                ele_num = ele_max;
            }
            final int data_size = ele_num * ele_byte_n;
            final int iso_size = 35 + data_size;
            System.arraycopy(S7MsgWrite.RW35, 0, conn.PDU, 0, 35);
            S7Util.setUInt16(conn.PDU, 2, iso_size);
            conn.PDU[11] = 0;
            conn.PDU[12] = 8;
            int len = data_size + 4;
            S7Util.setUInt16(conn.PDU, 15, len);
            conn.PDU[17] = 5;
            conn.PDU[27] = (byte) area_memtp.getVal();
            if (area_memtp == S7MemTp.DB) {
                S7Util.setUInt16(conn.PDU, 25, db_num);
            } else if (area_memtp == S7MemTp.V) {
                S7Util.setUInt16(conn.PDU, 25, 1);
            }
            conn.PDU[22] = 1;
            int addr;
            if (area_memtp == S7MemTp.C || area_memtp == S7MemTp.T) {
                addr = pos;
                len = data_size;
                if (area_memtp == S7MemTp.C) {
                    conn.PDU[22] = 28;
                } else {
                    conn.PDU[22] = 29;
                }
            } else {
                addr = pos * 8 + bitidx;
                len = data_size;
            }
            S7Util.setUInt16(conn.PDU, 23, ele_num);
            conn.PDU[30] = (byte) (addr & 0xFF);
            addr >>= 8;
            conn.PDU[29] = (byte) (addr & 0xFF);
            addr >>= 8;
            conn.PDU[28] = (byte) (addr & 0xFF);
            conn.PDU[32] = 3;
            S7Util.setUInt16(conn.PDU, 33, len);
            conn.PDU[35] = (byte) (v ? 1 : 0);
            if (S7MsgWrite.log.isTraceEnabled()) {
                final String tmps = Convert.byteArray2HexStr(conn.PDU, 0, iso_size, " ");
                S7MsgWrite.log.trace("S7Msg write bit ->" + tmps);
            }
            conn.send(conn.PDU, iso_size);
            len = S7Msg.recvIsoPacket(conn);
            if (S7MsgWrite.log.isTraceEnabled() && len > 0 && len < 1000) {
                final String tmps = Convert.byteArray2HexStr(conn.PDU, 0, len, " ");
                S7MsgWrite.log.trace("S7Msg recv <-" + tmps);
            }
            if (len != 22) {
                throw new S7Exception("invalid pdu");
            }
            if (S7Util.getUInt16(conn.PDU, 17) != 0 || conn.PDU[21] != -1) {
                throw new S7Exception("write date err");
            }
            offset += data_size;
        }
    }

    public S7MsgWrite withParam(final S7MemTp area_memtp, final int db_num, final int pos, final int bitidx, final int writenum) {
        this.areaMemtp = area_memtp;
        this.dbNum = db_num;
        this.pos = pos;
        this.bitIdx = bitidx;
        this.writeNum = writenum;
        return this;
    }

    public S7MsgWrite withParam(final S7MemTp area_memtp, final int db_num, final S7Addr addr, final Object v) {
        this.areaMemtp = area_memtp;
        this.dbNum = db_num;
        this.pos = addr.getOffsetBytes();
        this.bitIdx = addr.getInBits();
        this.writeNum = addr.getBytesNum();
        this.writeObj = v;
        this.writeV = this.transVToBytes(addr, v);
        return this;
    }

    private byte[] transVToBytes(final S7Addr addr, final Object v) {
        final S7ValTp s7vtp = addr.getMemValTp();
        if (s7vtp == null) {
            return null;
        }
        final UAVal.ValTP vtp = addr.getValTP();
        final int bn = s7vtp.getByteNum();
        if (vtp.isNumberVT()) {
            final Number num = (Number) v;
            if (vtp.isNumberFloat()) {
                return DataUtil.floatToBytes(num.floatValue());
            }
            switch (bn) {
                case 1: {
                    return new byte[]{num.byteValue()};
                }
                case 2: {
                    final int intv = num.intValue();
                    return new byte[]{(byte) (intv >> 8 & 0xFF), (byte) (intv & 0xFF)};
                }
                case 4: {
                    final int intv = num.intValue();
                    return new byte[]{(byte) (intv >> 24 & 0xFF), (byte) (intv >> 16 & 0xFF), (byte) (intv >> 8 & 0xFF), (byte) (intv & 0xFF)};
                }
            }
        }
        return null;
    }

    @Override
    public void processByConn(final S7TcpConn conn) throws S7Exception, IOException {
        if (this.bitIdx >= 0) {
            boolean rv;
            if (this.writeObj instanceof Boolean) {
                rv = (boolean) this.writeObj;
            } else {
                if (!(this.writeObj instanceof Number)) {
                    return;
                }
                rv = (((Number) this.writeObj).intValue() > 0);
            }
            writeAreaBit(conn, this.areaMemtp, this.dbNum, this.pos, this.bitIdx, this.writeNum, rv);
        } else {
            writeArea(conn, this.areaMemtp, this.dbNum, this.pos, this.writeNum, this.writeV);
        }
    }
}
