

package cn.doraro.flexedge.driver.common.modbus.slave;

import cn.doraro.flexedge.core.Config;
import cn.doraro.flexedge.core.util.Convert;
import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MSlaveManager {
    static Object locker;
    static MSlaveManager slaveMgr;

    static {
        MSlaveManager.locker = new Object();
        MSlaveManager.slaveMgr = null;
    }

    ArrayList<MSlave> slaves;
    Thread monThread;
    Runnable monRunner;

    private MSlaveManager() {
        this.slaves = new ArrayList<MSlave>();
        this.monThread = null;
        this.monRunner = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    for (final MSlave ms : MSlaveManager.this.slaves) {
                        List<MSlaveDataProvider> bdps = ms.getBitDataProviders();
                        if (bdps != null) {
                            for (final MSlaveDataProvider dp : bdps) {
                                dp.pulseAcquireData();
                            }
                        }
                        bdps = ms.getWordDataProviders();
                        if (bdps != null) {
                            for (final MSlaveDataProvider dp : bdps) {
                                dp.pulseAcquireData();
                            }
                        }
                    }
                    try {
                        Thread.sleep(50L);
                    } catch (final Exception ex) {
                    }
                }
            }
        };
        final Element ele = Config.getConfElement("modbus");
        if (ele == null) {
            return;
        }
        for (final Element sele : Convert.getSubChildElement(ele, "slave")) {
            MSlave ms = null;
            String t = sele.getAttribute("type");
            if (Convert.isNullOrEmpty(t)) {
                t = "tcp_server";
            }
            if ("tcp_server".equals(t)) {
                ms = new MSlaveTcpServer();
            } else if (!"tcp_client".equals(t)) {
                if ("com".equalsIgnoreCase(t)) {
                }
            }
            if (ms != null) {
                ms.init(sele);
                this.slaves.add(ms);
            }
        }
    }

    public static MSlaveManager getInstance() {
        if (MSlaveManager.slaveMgr != null) {
            return MSlaveManager.slaveMgr;
        }
        synchronized (MSlaveManager.locker) {
            if (MSlaveManager.slaveMgr != null) {
                return MSlaveManager.slaveMgr;
            }
            return MSlaveManager.slaveMgr = new MSlaveManager();
        }
    }

    public static void main(final String[] args) throws Exception {
        final MSlaveManager pm = new MSlaveManager();
        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        final String sDevId = null;
        System.out.print(">");
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            try {
                final StringTokenizer st = new StringTokenizer(inputLine, " ", false);
                final String[] cmds = new String[st.countTokens()];
                for (int i = 0; i < cmds.length; ++i) {
                    cmds[i] = st.nextToken();
                }
                if (cmds.length <= 0) {
                    continue;
                }
                if ("start".equals(cmds[0])) {
                    pm.start();
                } else if ("stop".equals(cmds[0])) {
                    pm.stop();
                } else if ("ls".equals(cmds[0])) {
                    for (final MSlave pp : pm.slaves) {
                        System.out.println(pp);
                    }
                } else {
                    if ("exit".equals(cmds[0])) {
                        break;
                    }
                    continue;
                }
            } catch (final Exception _e) {
                _e.printStackTrace();
            } finally {
                if (sDevId != null) {
                    System.out.print(sDevId);
                }
                System.out.print(">");
            }
        }
        System.exit(0);
    }

    public void start() {
        for (final MSlave ms : this.slaves) {
            ms.start();
        }
        (this.monThread = new Thread(this.monRunner, "mslave_mon")).start();
    }

    public void stop() {
        for (final MSlave ms : this.slaves) {
            ms.stop();
        }
        final Thread st = this.monThread;
        if (st != null) {
            st.interrupt();
            this.monThread = null;
        }
    }
}
