package cn.doraro.flexedge.core;

/**
 * all msg is send to channel with this driver
 * <p>
 * driver will set data under channel
 *
 * @author jason.zhu
 */
public abstract class DevDriverMsgOnly extends DevDriver {
    public abstract void RT_onConnMsgIn(byte[] msgbs);

    @Override
    public Class<? extends ConnPt> supportConnPtClass() {
        return cn.doraro.flexedge.core.conn.ConnPtMSGMultiTcp.class;
    }

}
