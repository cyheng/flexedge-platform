// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common.modbus.slave;

import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

public class ServiceWrapper implements WrapperListener {
    private MSlaveManager pm;

    private ServiceWrapper() {
        this.pm = MSlaveManager.getInstance();
    }

    public static void main(final String[] args) {
        System.out.println("Initializing...");
        WrapperManager.start((WrapperListener) new ServiceWrapper(), args);
    }

    public Integer start(final String[] args) {
        try {
            this.pm.start();
            return null;
        } catch (final Throwable t) {
            t.printStackTrace();
            return -1;
        }
    }

    public int stop(final int extcode) {
        try {
            this.pm.stop();
        } catch (final Exception ee) {
            ee.printStackTrace();
        }
        System.exit(extcode);
        return extcode;
    }

    public void controlEvent(final int arg) {
    }
}
