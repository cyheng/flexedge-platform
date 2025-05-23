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
    if (!Convert.checkReqEmpty(request, out, "repid", "unit_id"))
        return;
    String repid = request.getParameter("repid");
    String chid = request.getParameter("unit_id");

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
%>
<div>run=<%=n.getDriver().RT_isRunning() %>
    <input type="button" value="start" onclick="btn_start_ch()"/>
    <input type="button" value="stop" onclick="btn_stop_ch()"/>
</div>
<script>
    function btn_start_ch() {
        send_ajax('/admin/ua/ch_action_ajax.jsp', {
            repid: "<%=repid%>",
            chid: "<%=chid%>",
            op: "start"
        }, function (bsucc, ret) {
            dlg.msg(ret);
        });
    }

    function btn_stop_ch() {
        send_ajax('/admin/ua/ch_action_ajax.jsp', {
            repid: "<%=repid%>",
            chid: "<%=chid%>",
            op: "stop"
        }, function (bsucc, ret) {
            dlg.msg(ret);
        });
    }
</script>