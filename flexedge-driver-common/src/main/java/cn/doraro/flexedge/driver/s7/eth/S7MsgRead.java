// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.eth;

import java.io.IOException;

public class S7MsgRead extends S7Msg {
    private static final int Size_RD = 31;
    S7MemTp areaMemtp;
    int dbNum;
    int pos;
    int readNum;
    byte[] readRes;
    private boolean readOk;

    public S7MsgRead() {
        this.readOk = false;
    }

    private static int calBytes(final S7MemTp area_memtp, final int readnum) {
        int ele_byte_n = 1;
        if (area_memtp == S7MemTp.C || area_memtp == S7MemTp.T) {
            ele_byte_n = 2;
        }
        return readnum * ele_byte_n;
    }

    static void readArea(final S7TcpConn conn, final S7MemTp area_memtp, final int db_num, int pos, final int readnum, final byte[] bs) throws S7Exception, IOException {
        int ele_byte_n = 1;
        if (area_memtp == S7MemTp.C || area_memtp == S7MemTp.T) {
            ele_byte_n = 2;
        }
        final int ele_max = (conn.pduLen - 18) / ele_byte_n;
        int ele_tot = readnum;
        int offset = 0;
        while (ele_tot > 0) {
            int ele_num = ele_tot;
            if (ele_num > ele_max) {
                ele_num = ele_max;
            }
            final int req_bytes = ele_num * ele_byte_n;
            System.arraycopy(S7MsgRead.RW35, 0, conn.PDU, 0, 31);
            conn.PDU[27] = (byte) area_memtp.getVal();
            if (area_memtp == S7MemTp.DB) {
                S7Util.setUInt16(conn.PDU, 25, db_num);
            } else if (area_memtp == S7MemTp.V) {
                S7Util.setUInt16(conn.PDU, 25, 1);
            }
            int addr;
            if (area_memtp == S7MemTp.C || area_memtp == S7MemTp.T) {
                addr = pos;
                if (area_memtp == S7MemTp.C) {
                    conn.PDU[22] = 28;
                } else {
                    conn.PDU[22] = 29;
                }
            } else {
                addr = pos << 3;
            }
            S7Util.setUInt16(conn.PDU, 23, ele_num);
            conn.PDU[30] = (byte) (addr & 0xFF);
            addr >>= 8;
            conn.PDU[29] = (byte) (addr & 0xFF);
            addr >>= 8;
            conn.PDU[28] = (byte) (addr & 0xFF);
            conn.send(conn.PDU, 31);
            final int len = S7Msg.recvIsoPacket(conn);
            if (len < 25) {
                throw new S7Exception("invalid pdu");
            }
            if (len - 25 != req_bytes || conn.PDU[21] != -1) {
                throw new S7Exception("read err");
            }
            System.arraycopy(conn.PDU, 25, bs, offset, req_bytes);
            offset += req_bytes;
            ele_tot -= ele_num;
            pos += ele_num * ele_byte_n;
        }
    }

    public S7MsgRead withParam(final S7MemTp area_memtp, final int db_num, final int pos, final int readnum) {
        this.areaMemtp = area_memtp;
        this.dbNum = db_num;
        this.pos = pos;
        this.readNum = readnum;
        return this;
    }

    public int getPos() {
        return this.pos;
    }

    public byte[] getReadRes() {
        return this.readRes;
    }

    public boolean isReadOk() {
        return this.readOk;
    }

    @Override
    public void processByConn(final S7TcpConn conn) throws S7Exception, IOException {
        final int n = calBytes(this.areaMemtp, this.readNum);
        final byte[] bs = new byte[n];
        readArea(conn, this.areaMemtp, this.dbNum, this.pos, this.readNum, bs);
        this.readRes = bs;
        this.readOk = true;
    }
}
