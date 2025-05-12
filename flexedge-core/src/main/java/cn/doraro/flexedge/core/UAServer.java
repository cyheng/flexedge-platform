package cn.doraro.flexedge.core;

import cn.doraro.flexedge.core.plugin.PlugManager;
import cn.doraro.flexedge.core.service.ServiceManager;
import cn.doraro.flexedge.core.sim.SimManager;
import cn.doraro.flexedge.core.station.PlatInsWSServer;
import cn.doraro.flexedge.core.station.StationLocal;
import cn.doraro.flexedge.core.ws.WSHelper;

import java.io.File;
import java.util.List;

public class UAServer {
    public static void onServerStarted(List<WebItem> webitems) {
        for (WebItem wi : webitems) {
            PlugManager.getInstance().onWebappLoaded(wi);
        }

        System.out.println("**starting service manager");
        ServiceManager.getInstance().start();
        ;
        // System.out.println(" all web comp loaded,fire event");
        // runFileMon();
        System.out.println("**starting ua manager");
        UAManager.getInstance().start();

        System.out.println("**starting simulator manager");
        SimManager.getInstance().start();

        System.out.println("**starting connection provider");
        ConnProvider.getAllConnProviders();

        StationLocal sl = StationLocal.getInstance();
        if (sl != null) {
            System.out.println("**starting station local [" + sl.getStationId() + "]");
            sl.RT_start();
        }
    }

    public static void beforeServerStop() {
        StationLocal sl = StationLocal.getInstance();
        if (sl != null) {
            System.out.println("**stopping station local [" + sl.getStationId() + "]");
            sl.RT_stop();
        }

        System.out.println("**stopping pstation instance");
        PlatInsWSServer.stopBeforeSysExit();

        System.out.println("**stopping connection provider");
        for (ConnProvider cp : ConnProvider.getAllConnProviders()) {
            cp.stop();
        }

        System.out.println("*stopping simulator manager");
        SimManager.getInstance().stop();

        System.out.println("**stopping ua manager");
        UAManager.getInstance().stop();

        System.out.println("**stopping service manager");
        ServiceManager.getInstance().stop();

        WSHelper.onSysClose();
    }

    public static class WebItem {
        String appN;

        ClassLoader cl;

        File webDir;

        public WebItem(String appn, ClassLoader cl, File web_dir) {
            this.appN = appn;
            this.cl = cl;
            this.webDir = web_dir;
        }

        public String getAppName() {
            return appN;
        }

        public ClassLoader getAppClassLoader() {
            return cl;
        }

        public File getWebDir() {
            return webDir;
        }
    }
}
