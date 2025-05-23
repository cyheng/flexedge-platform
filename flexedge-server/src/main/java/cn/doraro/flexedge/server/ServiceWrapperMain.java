

package cn.doraro.flexedge.server;

import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

public class ServiceWrapperMain implements WrapperListener {
    private ServiceWrapperMain() {
    }

    public static void main(final String[] args) {
        System.out.println("Initializing iottree...");
        WrapperManager.start((WrapperListener) new ServiceWrapperMain(), args);
    }

    public Integer start(final String[] arg0) {
        try {
            Server.startServer(true);
            return null;
        } catch (final Throwable t) {
            t.printStackTrace();
            return -1;
        }
    }

    public int stop(final int extcode) {
        try {
            Server.stopServer();
        } catch (final Exception ee) {
            ee.printStackTrace();
        }
        return extcode;
    }

    public void controlEvent(final int arg0) {
    }
}
