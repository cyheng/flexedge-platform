<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="
cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.basic.*,
                 java.io.*,
                 java.util.*,
                 java.net.*,
                 java.util.*" %>
<%
    if (!Convert.checkReqEmpty(request, out, "repid", "chid", "op"))
        return;
    String repid = request.getParameter("repid");
    String chid = request.getParameter("chid");
    String op = request.getParameter("op");
    UAManager uam = UAManager.getInstance();
    UAPrj dc = uam.getPrjById(repid);
    if (dc == null) {
        out.print("no rep found with id=" + repid);
        return;
    }

    UACh n = dc.getChById(chid);
    if (n == null) {
        out.print("no node found");
        return;
    }
    StringBuilder failedr = new StringBuilder();
    if ("start".equals(op)) {
        if (!n.RT_start(failedr)) {
            out.print(failedr);
        } else {
            out.print("start ok");
        }
    } else if ("stop".equals(op)) {
        if (!n.RT_stop(failedr)) {
            out.print(failedr);
        } else {
            out.print("stop ok");
        }
    }
%>