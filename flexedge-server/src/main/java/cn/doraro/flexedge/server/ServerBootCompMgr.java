// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.server;

import cn.doraro.flexedge.core.util.IServerBootComp;

import java.util.Hashtable;
import java.util.StringTokenizer;

public class ServerBootCompMgr {
    static ServerBootCompMgr bcMgr;
    static Object lockobj;
    static Hashtable<String, IServerBootComp> name2bc;
    static Hashtable<String, Thread> name2thread;

    static {
        ServerBootCompMgr.bcMgr = null;
        ServerBootCompMgr.lockobj = new Object();
        ServerBootCompMgr.name2bc = new Hashtable<String, IServerBootComp>();
        ServerBootCompMgr.name2thread = new Hashtable<String, Thread>();
    }

    private ServerBootCompMgr() {
        final String sbc = System.getProperty("server.boot.comp");
        if (sbc != null && !sbc.equals("")) {
            final StringTokenizer tmpst = new StringTokenizer(sbc, ",");
            while (tmpst.hasMoreTokens()) {
                String cn = tmpst.nextToken();
                if ((cn = cn.trim()).equals("")) {
                    continue;
                }
                try {
                    final Class<?> c = Class.forName(cn);
                    if (c == null) {
                        continue;
                    }
                    final IServerBootComp tmpbc = (IServerBootComp) c.newInstance();
                    ServerBootCompMgr.name2bc.put(tmpbc.getBootCompName(), tmpbc);
                } catch (final Exception e) {
                    System.out.println("Load Server Boot Comp Error:");
                    e.printStackTrace();
                }
            }
        }
    }

    public static IServerBootComp registerServerBoolComp(final String classname) {
        try {
            final Class<?> c = Class.forName(classname);
            if (c == null) {
                return null;
            }
            final IServerBootComp tmpbc = (IServerBootComp) c.newInstance();
            ServerBootCompMgr.name2bc.put(tmpbc.getBootCompName(), tmpbc);
            return tmpbc;
        } catch (final Exception e) {
            System.out.println("Load Server Boot Comp Error:");
            e.printStackTrace();
            return null;
        }
    }

    public static ServerBootCompMgr getInstance() {
        if (ServerBootCompMgr.bcMgr != null) {
            return ServerBootCompMgr.bcMgr;
        }
        synchronized (ServerBootCompMgr.lockobj) {
            if (ServerBootCompMgr.bcMgr != null) {
                final ServerBootCompMgr bcMgr = ServerBootCompMgr.bcMgr;
                monitorexit(ServerBootCompMgr.lockobj);
                return bcMgr;
            }
            ServerBootCompMgr.bcMgr = new ServerBootCompMgr();
            final ServerBootCompMgr bcMgr2 = ServerBootCompMgr.bcMgr;
            monitorexit(ServerBootCompMgr.lockobj);
            return bcMgr2;
        }
    }

    public String[] getAllServerBootNames() {
        final String[] rets = new String[ServerBootCompMgr.name2bc.size()];
        ServerBootCompMgr.name2bc.keySet().toArray(rets);
        return rets;
    }

    public void stopAllBootComp() {
        for (final IServerBootComp bc : ServerBootCompMgr.name2bc.values()) {
            try {
                bc.stopComp();
            } catch (final Exception ex) {
            }
        }
    }

    public IServerBootComp startBootComp(final String n) throws Exception {
        final IServerBootComp bc = ServerBootCompMgr.name2bc.get(n);
        if (bc == null) {
            return null;
        }
        Thread t = ServerBootCompMgr.name2thread.get(n);
        if (t != null) {
            return bc;
        }
        final ServerCompCtrlStarter sccs = new ServerCompCtrlStarter(bc);
        t = new Thread(sccs);
        t.start();
        return bc;
    }

    public IServerBootComp getBootComp(final String n) {
        return ServerBootCompMgr.name2bc.get(n);
    }

    public boolean isBootCompRunning(final String n) throws Exception {
        final IServerBootComp bc = ServerBootCompMgr.name2bc.get(n);
        return bc != null && bc.isRunning();
    }

    public void stopBootComp(final String n) throws Exception {
        final IServerBootComp bc = ServerBootCompMgr.name2bc.get(n);
        if (bc == null) {
            return;
        }
        bc.stopComp();
    }

    class ServerCompCtrlStarter implements Runnable {
        IServerBootComp bootc;

        public ServerCompCtrlStarter(final IServerBootComp sbc) {
            this.bootc = null;
            this.bootc = sbc;
        }

        @Override
        public void run() {
            try {
                this.bootc.startComp();
            } catch (final Exception e) {
                e.printStackTrace();
                return;
            } finally {
                ServerBootCompMgr.name2thread.remove(this.bootc.getBootCompName());
            }
            ServerBootCompMgr.name2thread.remove(this.bootc.getBootCompName());
        }
    }
}
