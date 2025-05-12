// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.server;

import cn.doraro.flexedge.core.Config;
import cn.doraro.flexedge.core.UAServer;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.IServerBootComp;
import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Server {
    static ClassLoader dynCL;
    static ServerTomcat serverTomcat;
    static ArrayList<IServerBootComp> serverComps;
    static Runnable consoleRunner;

    static {
        Server.dynCL = null;
        Server.serverTomcat = null;
        Server.serverComps = new ArrayList<IServerBootComp>();
        Server.consoleRunner = new Runnable() {
            @Override
            public void run() {
                try {
                    final ServerCtrlHandler sch = new ServerCtrlHandler(System.in);
                    sch.handle();
                    Server.stopServer();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private static void printBanner() {
        try {
            Exception t = null;
            try {
                final InputStream inputs = Server.class.getResourceAsStream("banner.txt");
                try {
                    final BufferedReader br = new BufferedReader(new InputStreamReader(inputs));
                    String ln;
                    do {
                        ln = br.readLine();
                        if (ln == null) {
                            System.out.print("   version " + Config.getVersion());
                            System.out.println("\r\nfile base=" + Config.getConfigFileBase());
                            System.out.print("\r\n\r\n");
                            break;
                        }
                        System.out.print("\r\n" + ln);
                    } while (ln != null);
                } finally {
                    if (inputs != null) {
                        inputs.close();
                    }
                }
            } catch (Exception var12) {
                if (t == null) {
                    t = var12;
                } else if (t != var12) {
                    t.addSuppressed(var12);
                }

                throw t;
            }
        } catch (final Exception ex) {
        }
    }

    static void startServer(final boolean bservice) throws Exception {
        printBanner();
        if (Config.isDebug()) {
            System.out.println("user.dir=" + System.getProperties().getProperty("user.dir"));
        }
        Config.loadConf();
        final ClassLoader tbs_loader = Thread.currentThread().getContextClassLoader();
        final Element sysele = Config.getConfElement("system");
        if (sysele != null) {
            final Element[] scs = Convert.getSubChildElement(sysele, "server_comp");
            if (scs != null) {
                Element[] array;
                for (int length = (array = scs).length, i = 0; i < length; ++i) {
                    final Element sc = array[i];
                    final String serv_comp_cn = sc.getAttribute("class");
                    if (!Convert.isNullOrEmpty(serv_comp_cn)) {
                        try {
                            System.out.println("find server comp:" + serv_comp_cn);
                            ServerBootCompMgr.registerServerBoolComp(serv_comp_cn);
                        } catch (final Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        Server.serverTomcat = new ServerTomcat();
        final List<UAServer.WebItem> wis = Server.serverTomcat.startTomcat(tbs_loader);
        String[] allServerBootNames;
        for (int length2 = (allServerBootNames = ServerBootCompMgr.getInstance().getAllServerBootNames()).length, j = 0; j < length2; ++j) {
            final String n = allServerBootNames[j];
            ServerBootCompMgr.getInstance().startBootComp(n);
        }
        UAServer.onServerStarted((List) wis);
        if (bservice) {
            new Thread(Server::runFileMon, "").start();
        } else {
            Server.consoleRunner.run();
        }
    }

    static void stopServer() {
        UAServer.beforeServerStop();
        try {
            ServerBootCompMgr.getInstance().stopAllBootComp();
            Thread.sleep(5000L);
        } catch (final Exception ex) {
        }
        if (Server.serverTomcat != null) {
            try {
                Server.serverTomcat.stopComp();
            } catch (final Exception ex2) {
            }
        }
    }

    private static void runFileMon() {
        try {
            final File wf = new File("./iottree_running.flag");
            wf.createNewFile();
            final File stopwf = new File("./iottree_stopped.flag");
            if (stopwf.exists()) {
                stopwf.delete();
            }
            while (wf.exists()) {
                try {
                    Thread.sleep(3000L);
                } catch (final Exception ex) {
                }
            }
            stopServer();
            stopwf.createNewFile();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(final String[] args) throws Exception {
        boolean bservice = false;
        boolean blinux_nohup = false;
        if (args.length > 0) {
            final String s;
            switch (s = args[0]) {
                case "linux_nohup": {
                    blinux_nohup = true;
                    bservice = true;
                    break;
                }
                case "service": {
                    bservice = true;
                    break;
                }
                default:
                    break;
            }
        }
        startServer(bservice);
        if (!blinux_nohup) {
            return;
        }
        while (true) {
            Thread.sleep(60000L);
        }
    }
}
