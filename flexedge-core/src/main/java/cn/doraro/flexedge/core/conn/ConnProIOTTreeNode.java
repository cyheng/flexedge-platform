package cn.doraro.flexedge.core.conn;

import cn.doraro.flexedge.core.ConnProvider;
import cn.doraro.flexedge.core.ConnPt;

/**
 * one IOTTree project in one IOTTree server instance can be a node(IOTTree
 * Node). A node can be connected to another IOTree project's channel.
 * <p>
 * so it can be a sub system integrate into another system.
 * <p>
 * all these is used by this provider
 *
 * @author jason.zhu
 */
public class ConnProIOTTreeNode extends ConnProvider {
    public static final String TP = "flexedge_node";

    @Override
    public String getProviderType() {
        return TP;
    }

    @Override
    public String getProviderTpt() {
        return "IOTTree Node";
    }

    public boolean isSingleProvider() {
        return true;
    }

    @Override
    public Class<? extends ConnPt> supportConnPtClass() {
        return ConnPtIOTTreeNode.class;
    }

    @Override
    protected long connpRunInterval() {
        // TODO Auto-generated method stub
        return 1000;
    }

    public void stop() {
        super.stop();

        disconnAll();
    }

    public void disconnAll() // throws IOException
    {
        for (ConnPt ci : this.listConns()) {
            try {
                ConnPtIOTTreeNode conn = (ConnPtIOTTreeNode) ci;
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void connpRunInLoop() throws Exception {
        for (ConnPt ci : this.listConns()) {
            ConnPtIOTTreeNode citc = (ConnPtIOTTreeNode) ci;
            citc.RT_checkConn();
        }
    }
}
