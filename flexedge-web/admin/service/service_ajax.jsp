<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="
	cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.service.*,
                 cn.doraro.flexedge.core.basic.*,
                 java.io.*,
                 java.util.*,
                 java.net.*,
                 java.util.*" %>
<%

    if (!Convert.checkReqEmpty(request, out, "op"))
        return;
    String op = request.getParameter("op");

    switch (op) {
        case "start":
        case "stop":
            if (!Convert.checkReqEmpty(request, out, "n"))
                return;
            String n = request.getParameter("n");
            AbstractService as = ServiceManager.getInstance().getService(n);
            if (as == null) {
                out.print("no service found");
                return;
            }
            if ("start".equals(op))
                as.startService();
            else
                as.stopService();
            out.print("ok");
            break;
        case "setup":
            if (!Convert.checkReqEmpty(request, out, "n"))
                return;
            n = request.getParameter("n");
            as = ServiceManager.getInstance().getService(n);
            if (as == null) {
                out.print("no service found");
                return;
            }
            HashMap<String, String> pms = Convert.parseFromRequest(request, null);
            as.setService(pms);
            out.print("ok");
            break;
        case "list":
            break;
    }
%>