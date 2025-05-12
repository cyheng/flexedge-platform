// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.eth;

import java.io.IOException;

public class S7MsgISOCR extends S7Msg
{
    private static byte[] ISO_CR;
    private static final byte[] S7_PN;
    
    private int recvPduLength(final S7TcpConn conn) throws IOException, S7Exception {
        S7Util.setUInt16(S7MsgISOCR.S7_PN, 23, 480);
        conn.send(S7MsgISOCR.S7_PN);
        final int len = S7Msg.recvIsoPacket(conn);
        if (len != 27 || conn.PDU[17] != 0 || conn.PDU[18] != 0) {
            throw new S7Exception("get conn.PDU lenght failedr");
        }
        conn.pduLen = S7Util.getUInt16(conn.PDU, 25);
        if (conn.pduLen <= 0) {
            throw new S7Exception("get conn.PDU lenght failedr");
        }
        return conn.pduLen;
    }
    
    @Override
    public void processByConn(final S7TcpConn conn) throws S7Exception, IOException {
        S7MsgISOCR.ISO_CR[16] = conn.tsapLocalHI;
        S7MsgISOCR.ISO_CR[17] = conn.tsapLocalLO;
        S7MsgISOCR.ISO_CR[20] = conn.tsapRemoteHI;
        S7MsgISOCR.ISO_CR[21] = conn.tsapRemoteLO;
        conn.send(S7MsgISOCR.ISO_CR);
        final int sz = S7Msg.recvIsoPacket(conn);
        if (sz != 22) {
            throw new S7Exception("Invalid conn.PDU");
        }
        if (conn.pduType != -48) {
            throw new S7Exception("ISO Connected failed");
        }
        this.recvPduLength(conn);
    }
    
    static {
        S7MsgISOCR.ISO_CR = new byte[] { 3, 0, 0, 22, 17, -32, 0, 0, 0, 1, 0, -64, 1, 10, -63, 2, 1, 0, -62, 2, 1, 2 };
        S7_PN = new byte[] { 3, 0, 0, 25, 2, -16, -128, 50, 1, 0, 0, 4, 0, 0, 8, 0, 0, -16, 0, 0, 1, 0, 1, 0, 30 };
    }
}
