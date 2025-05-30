<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="
	cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.conn.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.basic.*,
                 java.io.*,
                 java.util.*,
                 java.net.*,
                 java.util.*" %>
<%
    if (!Convert.checkReqEmpty(request, out, "prjid", "cpid", "msgid", "op"))
        return;
    String op = request.getParameter("op");
    String prjid = request.getParameter("prjid");
    String cpid = request.getParameter("cpid");
    String cid = request.getParameter("cid");
    String msgid = request.getParameter("msgid");
    ConnProvider cp = ConnManager.getInstance().getConnProviderById(prjid, cpid);
    if (cp == null) {
        out.print("no provider found ");
        return;
    }
    ConnPt cpt = null;
    if (Convert.isNotNullEmpty(cid)) {
        cpt = cp.getConnById(cid);
        if (cpt == null) {
            out.print("no connection found");
            return;
        }
    }
    String cptitle = cp.getTitle();

    ConnMsg msg = null;
    if (cpt != null)
        msg = cpt.getConnMsgById(msgid);
    else
        msg = cp.getConnMsgById(msgid);

    if (msg == null) {
        out.print("no msg with id " + msgid + " found");
        return;
    }

    switch (op) {
        case "full_json":
            out.print(msg.toFullJsonStr());
            break;
        default:
            out.print("unknown op");
            break;
    }
%>
