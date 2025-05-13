package cn.doraro.flexedge.web.oper;

import cn.doraro.flexedge.core.station.PlatInsWSServer;
import cn.doraro.flexedge.core.ws.WebSocketConfig;

import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/_ws/station/{stationid}", configurator = WebSocketConfig.class)
public class WSStationRT extends PlatInsWSServer {
    //private static final String PAU = "_pau_" ;
    static {
//System.out.println("WSStationRT fined..............") ;
    }


}

