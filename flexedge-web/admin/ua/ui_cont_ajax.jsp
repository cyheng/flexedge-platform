<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="
	cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.basic.*,
                 java.io.*,
                 java.util.*,
                 java.net.*,
                 java.util.*,
                 org.json.*" %>
<%
    if (!Convert.checkReqEmpty(request, out, "id", "op"))
        return;
    String op = request.getParameter("op");
    String repid = request.getParameter("id");

    UAPrj rep = UAManager.getInstance().getPrjById(repid);
    if (rep == null) {
        out.print("no rep found");
        return;
    }

    if ("load".equals(op)) {
        JSONObject jobj = rep.toOCUnitJSON();//IOCUnit.transOCUnitToJSON(dc) ;
        out.print(jobj.toString(2));
    } else if ("save".equals(op)) {
        if (!Convert.checkReqEmpty(request, out, "txt"))
            return;
        String txt = request.getParameter("txt");
        //System.out.println(txt);
        JSONObject jobj = new JSONObject(txt);
        rep.fromOCUnitJSON(jobj);
        rep.save();
        out.print("ok");
    }
%>


