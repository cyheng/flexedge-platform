package cn.doraro.flexedge.core.conn;

import cn.doraro.flexedge.core.ConnProvider;
import cn.doraro.flexedge.core.ConnPt;

public class ConnProHTTP extends ConnProvider {
    public static final String TP = "http";

    @Override
    public String getProviderType() {
        return TP;
    }

    @Override
    public String getProviderTpt() {
        return "HTTP";
    }

    public boolean isSingleProvider() {
        return false;
    }


    @Override
    public Class<? extends ConnPt> supportConnPtClass() {
        return ConnPtHTTP.class;
    }

    @Override
    protected long connpRunInterval() {
        return 100;
    }

    @Override
    protected void connpRunInLoop() throws Exception {
        for (ConnPt ci : this.listConns()) {
            ConnPtHTTP citc = (ConnPtHTTP) ci;
            citc.checkUrl();
        }
    }

}
