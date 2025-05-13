<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="
	java.io.*,
                 java.util.*,
                 java.net.*,
                 java.util.*,
                 org.json.*,
                 cn.doraro.flexedge.*,
                 cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.cxt.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.util.xmldata.*" %>
<%
    if (!Convert.checkReqEmpty(request, out, "cn", "op"))
        return;
    String op = request.getParameter("op");
    String classn = request.getParameter("cn");
    Class<?> cc = Class.forName(classn);

    Object sub_ob = cc.newInstance();

    if (sub_ob instanceof IJsProp) {
        ((IJsProp) sub_ob).constructSubForCxtHelper();
    }
    switch (op) {
        case "sub":
            out.write("[");
            JSObMap subv = (JSObMap) sub_ob;
            List<JsSub> jps = subv.JS_get_subs();//.JS_props();//.JS_get_props_cxt();
            boolean bfirst = true;
            for (JsSub jp : jps) {
                if (bfirst)
                    bfirst = false;
                else
                    out.write(',');
                jp.writeTree(null, out);
            }
            out.write("]");
            return;
    }

%>