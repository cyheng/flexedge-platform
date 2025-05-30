<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="
	java.io.*,
                 java.util.*,
                 java.net.*,
                 java.util.*,
                 cn.doraro.flexedge.*,
                 cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.util.xmldata.*" %>
<%
    //通信节点下挂载的设备
    if (!Convert.checkReqEmpty(request, out, "op"))
        return;
    String op = request.getParameter("op");
    String path = request.getParameter("path");

    UANode n = UAUtil.findNodeByPath(path);
    if (n == null) {
        out.print("no node found");
        return;
    }
    if (n instanceof UAHmi)
        n = n.getParentNode();
    if (!(n instanceof UANodeOCTagsCxt)) {
        out.print("not node oc tags");
        return;
    }
    UANodeOCTagsCxt ntags = (UANodeOCTagsCxt) n;
    switch (op) {
        case "w":
            if (!Convert.checkReqEmpty(request, out, "tagid"))
                return;
            String tagid = request.getParameter("tagid");
            UATag tag = (UATag) ntags.findNodeById(tagid);//.getTagById(tagid) ;
            if (tag == null) {
                out.print("no tag found");
                return;
            }
            String strv = request.getParameter("v");
            StringBuilder failedr = new StringBuilder();
            if (tag.RT_writeValStr(strv, failedr))
                out.print("write value ok");
            else
                out.print(failedr.toString());
            break;
        default:
            ntags.CXT_renderJson(out);
            break;
    }
%>