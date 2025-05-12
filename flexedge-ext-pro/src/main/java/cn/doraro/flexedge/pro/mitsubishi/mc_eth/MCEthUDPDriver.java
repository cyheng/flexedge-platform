// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.UACh;
import cn.doraro.flexedge.core.ConnPt;
import cn.doraro.flexedge.core.DevDriver;
import cn.doraro.flexedge.core.conn.ConnPtUDPBytesPk;

public class MCEthUDPDriver extends MCEthDriver
{
    ConnPtUDPBytesPk connPt;
    private byte[] recvBuf;
    
    public MCEthUDPDriver() {
        this.connPt = null;
        this.recvBuf = new byte[1024];
    }
    
    public DevDriver copyMe() {
        return new MCEthUDPDriver();
    }
    
    public String getName() {
        return "mc_eth_udp";
    }
    
    public String getTitle() {
        return "Mitsubishi MC Ethernet UDP";
    }
    
    public Class<? extends ConnPt> supportConnPtClass() {
        return (Class<? extends ConnPt>)ConnPtUDPBytesPk.class;
    }
    
    protected void RT_onConnReady(final ConnPt cp, final UACh ch, final UADev dev) throws Exception {
        this.connPt = (ConnPtUDPBytesPk)cp;
    }
    
    protected void RT_onConnInvalid(final ConnPt cp, final UACh ch, final UADev dev) throws Exception {
    }
    
    protected boolean RT_runInLoop(final UACh ch, final UADev dev, final StringBuilder failedr) throws Exception {
        return true;
    }
    
    protected boolean RT_useLoopNoWait() {
        return true;
    }
    
    protected boolean RT_runInLoopNoWait(final UACh ch, final UADev dev, final StringBuilder failedr) throws Exception {
        if (this.connPt == null) {
            return true;
        }
        final DatagramSocket ds = this.connPt.getUDPSock();
        if (ds == null) {
            return true;
        }
        final DatagramPacket pk = new DatagramPacket(this.recvBuf, this.recvBuf.length);
        ds.receive(pk);
        final InetAddress addr = pk.getAddress();
        this.RT_onRecvedBS(pk.getData(), pk.getLength(), addr);
        return true;
    }
    
    private void RT_onRecvedBS(final byte[] bs, final int len, final InetAddress addr) {
    }
}
