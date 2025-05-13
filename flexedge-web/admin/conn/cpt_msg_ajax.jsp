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
    if (!Convert.checkReqEmpty(request, out, "prjid", "cpid", "cid", "op"))
        return;
    String op = request.getParameter("op");
    String prjid = request.getParameter("prjid");
    String cpid = request.getParameter("cpid");
    String cid = request.getParameter("cid");
    ConnProvider cp = ConnManager.getInstance().getConnProviderById(prjid, cpid);
    if (cp == null) {
        out.print("no provider found ");
        return;
    }
    ConnPtMSGNor cpt = (ConnPtMSGNor) cp.getConnById(cid);
    if (cpt == null) {
        out.print("no connection found");
        return;
    }

    String cptitle = cp.getTitle();

    switch (op) {
        case "read_tmp_to_buf":
            File f = cpt.readMsgToTmpBuf();
            if (f == null) {
                out.print("read msg to tmp buf failed");
                return;
            }
            Date dt = new Date(f.lastModified());
            out.print("{dt:\"" + Convert.toFullYMDHMS(dt) + "\",bfp:\"connpt_msg/" + f.getName() + "\"}");
            break;
        default:
            out.print("unknown op");
            break;
    }
%>
