// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.eth;

import cn.doraro.flexedge.core.conn.ConnPtTcpClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class S7TcpConn {
    private static final int ISO_TCP_PORT = 102;
    final byte[] PDU;
    byte tsapLocalHI;
    byte tsapLocalLO;
    byte tsapRemoteHI;
    byte tsapRemoteLO;
    int pduLen;
    byte pduType;
    private ConnPtTcpClient cpTcp;
    private S7LinkTp linkTp;
    private long recvTO;
    private transient DataInputStream inputS;
    private transient DataOutputStream outputS;

    public S7TcpConn(final ConnPtTcpClient tcpc) {
        this.linkTp = S7LinkTp.PG;
        this.recvTO = 3000L;
        this.inputS = null;
        this.outputS = null;
        this.PDU = new byte[2048];
        this.pduLen = 0;
        this.pduType = -1;
        this.cpTcp = tcpc;
        this.inputS = new DataInputStream(this.cpTcp.getInputStream());
        this.outputS = new DataOutputStream(this.cpTcp.getOutputStream());
    }

    public S7TcpConn withLinkTp(final S7LinkTp ltp) {
        this.linkTp = ltp;
        return this;
    }

    public S7TcpConn withTSAP(final int loc_tsap, final int remote_tsap) {
        final int loc = loc_tsap & 0xFFFF;
        final int rrr = remote_tsap & 0xFFFF;
        this.tsapLocalHI = (byte) (loc >> 8);
        this.tsapLocalLO = (byte) (loc & 0xFF);
        this.tsapRemoteHI = (byte) (rrr >> 8);
        this.tsapRemoteLO = (byte) (rrr & 0xFF);
        return this;
    }

    public S7TcpConn withRackSlot(final int rack, final int slot) {
        final int rtsap = (this.linkTp.getVal() << 8) + rack * 32 + slot;
        return this.withTSAP(256, rtsap);
    }

    public S7TcpConn withTimeout(final long recv_to) {
        this.recvTO = recv_to;
        return this;
    }

    public ConnPtTcpClient getConnPt() {
        return this.cpTcp;
    }

    void recv(final byte[] buf, final int start, final int size) throws IOException {
        this.checkStreamLenTimeout(size, this.recvTO);
        this.inputS.read(buf, start, size);
    }

    void clearInputStream(final long timeout) {
        try {
            int lastav = this.inputS.available();
            long curt;
            int curav;
            for (long lastt = curt = System.currentTimeMillis(); (curt = System.currentTimeMillis()) - lastt < timeout; lastt = curt, lastav = curav) {
                try {
                    Thread.sleep(1L);
                } catch (final Exception ex) {
                }
                curav = this.inputS.available();
                if (curav != lastav) {
                }
            }
            if (lastav > 0) {
                this.inputS.skip(lastav);
            }
        } catch (final IOException e) {
            if (S7Msg.log.isDebugEnabled()) {
                S7Msg.log.error("", (Throwable) e);
            }
        }
    }

    private void checkStreamLenTimeout(final int len, final long timeout) throws IOException {
        long lastt = System.currentTimeMillis();
        int lastlen = this.inputS.available();
        long curt;
        while ((curt = System.currentTimeMillis()) - lastt < timeout) {
            final int curlen = this.inputS.available();
            if (curlen >= len) {
                return;
            }
            if (curlen > lastlen) {
                lastlen = curlen;
                lastt = curt;
            } else {
                try {
                    Thread.sleep(1L);
                } catch (final Exception ex) {
                }
            }
        }
        throw new IOException("time out");
    }

    void send(final byte[] buf, final int Len) throws IOException {
        this.outputS.write(buf, 0, Len);
        this.outputS.flush();
    }

    void send(final byte[] buf) throws IOException {
        this.send(buf, buf.length);
    }

    public void close() {
        try {
            this.cpTcp.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        this.close();
    }
}
