package cn.doraro.flexedge.core.ws;

import cn.doraro.flexedge.core.station.PlatInsWSServer;

public class WSHelper {
    public static void onSysClose() {
        WSRoot.stopTimer(false);
        WSServer.stopTimer(false);
        PlatInsWSServer.stopTimer(false);
    }
}
