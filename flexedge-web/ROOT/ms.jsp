<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="org.json.*,
                 cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.res.*,
                 cn.doraro.flexedge.web.oper.*,
                 cn.doraro.flexedge.core.comp.*,
                 java.io.*,
                 java.util.*,
                 java.net.*,
                 java.util.*" %>
<%
    JSONObject jo = new JSONObject();
    Date dt = new Date();
    jo.put("ms", dt.getTime());
    jo.put("dt", Convert.toFullYMDHMS(dt));
%><%=jo%>