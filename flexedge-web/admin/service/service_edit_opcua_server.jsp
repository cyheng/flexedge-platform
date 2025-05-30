<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="
	cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.service.*,
                 cn.doraro.flexedge.core.basic.*,
                 cn.doraro.flexedge.core.util.web.*,
                 cn.doraro.flexedge.driver.opc.opcua.server.*,
                 java.io.*,
                 java.util.*,
                 java.net.*,
                 java.util.*" %>
<%@ taglib uri="wb_tag" prefix="wbt" %>
<%
    OpcUAServer ser = (OpcUAServer) ServiceManager.getInstance().getService(OpcUAServer.NAME);
    HashMap<String, String> pms = ser.getConfPMS();
    boolean enable = ser.isEnable();//ser.isMqttEn();
    //boolean tcp_en = false;//ser.isTcpEn();
    String port = "4840";// ser.getMqttPortStr();

    String chked_en = "";
    if (enable)
        chked_en = "checked=checked";
    //if(tcp_en)
    //	chked_tcp_en = "checked=checked";

    String user = "";// ser.getAuthUser() ;
    String psw = "";// ser.getAuthPsw() ;
    String users = "";// ser.getAuthUsers();

%>
<html>
<head>
    <title>editor</title>
    <script src="/_js/jquery-1.12.0.min.js"></script>
    <script type="text/javascript" src="/_js/ajax.js"></script>
    <script src="/_js/layui/layui.all.js"></script>
    <script src="/_js/dlg_layer.js"></script>
    <link rel="stylesheet" type="text/css" href="/_js/layui/css/layui.css"/>
    <script>
        dlg.resize_to(600, 400);
    </script>
    <style>
    </style>
</head>
<body>
<form class="layui-form" action="">
    <div class="layui-form-item">
        <label class="layui-form-label"><wbt:g>enable</wbt:g>:</label>
        <div class="layui-input-inline" style="width: 150px;">
            <input type="checkbox" id="enable" name="enable" <%=chked_en%> lay-skin="switch" lay-filter="enable"
                   class="layui-input">
        </div>
        <div class="layui-form-mid"><wbt:g>port</wbt:g>:</div>
        <div class="layui-input-inline" style="width: 70px;">
            <input type="text" id="port" name="port" value="<%=port%>" class="layui-input">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label"><wbt:g>user</wbt:g>:</label>
        <div class="layui-input-inline" style="width: 150px;">
            <input type="text" id="user" name="user" class="layui-input" value="<%=user%>">
        </div>
        <div class="layui-form-mid"><wbt:g>psw</wbt:g>:</div>
        <div class="layui-input-inline" style="width: 70px;">
            <input type="text" id="psw" name="psw" value="<%=psw%>" class="layui-input">
        </div>
    </div>
    <div class="layui-form-item layui-form-text">
        <label class="layui-form-label"><wbt:g>more,users</wbt:g></label>
        <div class="layui-input-block">
            <textarea name="users" id="users" class="layui-textarea"><%=users %></textarea>
        </div>
    </div>
</form>
</body>
<script type="text/javascript">

    layui.use('form', function () {
        var form = layui.form;
        form.render();
    });

    function win_close() {
        dlg.close(0);
    }

    function do_submit(cb) {
        var enable = $("#enable").prop("checked");

        var auth_user = $('#user').val();
        var auth_psw = $('#psw').val();
        var auth_users = $('#users').val();
        cb(true, {
            enable: enable,
            auth_user: auth_user, auth_psw: auth_psw, auth_users: auth_users
        });
    }

</script>
</html>