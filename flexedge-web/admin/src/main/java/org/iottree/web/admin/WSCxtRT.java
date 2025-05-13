package cn.doraro.flexedge.web.admin;

import cn.doraro.flexedge.core.UAManager;
import cn.doraro.flexedge.core.UANode;
import cn.doraro.flexedge.core.UANodeOCTagsCxt;
import cn.doraro.flexedge.core.UAPrj;
import cn.doraro.flexedge.core.util.web.LoginUtil;
import cn.doraro.flexedge.core.ws.WSRoot;
import cn.doraro.flexedge.core.ws.WebSocketConfig;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/_ws/cxt_rt/{prjname}/{nodeid}", configurator = WebSocketConfig.class)
public class WSCxtRT extends WSRoot {
    static {
        //System.out.println(" cxt ws class is loading ................>>>>>>>>>>>>>>>>>>>>>>") ;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam(value = "prjname") String prjname,
                       @PathParam(value = "nodeid") String nodeid, EndpointConfig config) throws Exception //
    {
        HttpSession hs = WebSocketConfig.getHttpSession(config);
        if (!LoginUtil.checkAdminLogin(hs)) {
            session.close();
            return;
        }
        UAPrj rep = UAManager.getInstance().getPrjByName(prjname);
        if (rep == null) {
            session.close();
            return;
        }
        UANodeOCTagsCxt nodecxt = null;

        UANode n = rep.findNodeById(nodeid);
        if (n != null && (n instanceof UANodeOCTagsCxt)) {
            nodecxt = (UANodeOCTagsCxt) n;
        }

//		ConnAuth auth = getAuthFromSession(connid, session);
//		if (auth == null)
//		{
//			try
//			{
//				session.close();
//			}
//			catch (IOException e)
//			{
//				e.printStackTrace();
//			}
//		}

        SessionItem si = new SessionItem(session, rep, nodecxt, config);
        addSessionItem(si);

        startTimer();
    }

    // 关闭连接时调用
    @OnClose
    public void onClose(Session session, @PathParam(value = "connid") String connid) {
        removeSessionItem(session);
        //getAgentServer().onSessionUnset(session.getId());
        if (getSessionNum() <= 0)
            stopTimer(false);
    }


    @OnMessage
    public void onMessage(Session session, byte[] msg) throws Exception {
    }

    @OnMessage
    public void onMessageTxt(Session session, String msg) throws Exception {
    }

    // 错误时调用
    @OnError
    public void onError(Session session, Throwable t, @PathParam(value = "connid") String connid) {
        //getAgentServer().onSessionErr(connid, t);
        removeSessionItem(session);
        if (getSessionNum() <= 0)
            stopTimer(false);
    }
}
