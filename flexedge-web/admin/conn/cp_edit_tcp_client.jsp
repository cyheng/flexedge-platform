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
    if (!Convert.checkReqEmpty(request, out, "prjid"))
        return;
    String repid = request.getParameter("prjid");
    String cpid = request.getParameter("cpid");
    ConnProTcpClient cp = null;
    if (Convert.isNullOrEmpty(cpid)) {
        cp = new ConnProTcpClient();
        cpid = cp.getId();
    } else {
        cp = (ConnProTcpClient) ConnManager.getInstance().getConnProviderById(repid, cpid);
        if (cp == null) {
            out.print("no ConnProvider found");
            return;
        }
    }

    String name = cp.getName();
    String title = cp.getTitle();
    String chked = "";
    if (cp.isEnable())
        chked = "checked='checked'";
    String desc = cp.getDesc();
    String cp_tp = cp.getProviderType();
//List<ConnProTcpClient.ClientItem> clients = cp.listConns() ;
%>
<html>
<head>
    <title>tcp client cp editor</title>
    <script src="/_js/jquery-1.12.0.min.js"></script>
    <link rel="stylesheet" type="text/css" href="/_js/layui/css/layui.css"/>
    <script src="/_js/dlg_layer.js"></script>
    <script src="/_js/layui/layui.all.js"></script>
    <script src="/_js/dlg_layer.js"></script>
    <script>
        dlg.resize_to(600, 400);
    </script>
</head>
<body>
<form class="layui-form" onsubmit="return false;">
    <div class="layui-form-item">
        <label class="layui-form-label">Name:</label>
        <div class="layui-input-inline">
            <input type="text" id="name" name="name" value="<%=name%>" required lay-verify="required"
                   placeholder="Pls input name" autocomplete="off" class="layui-input">
        </div>
        <div class="layui-form-mid">Title:</div>
        <div class="layui-input-inline" style="width: 150px;">
            <input type="text" id="title" name="title" value="<%=title%>" required lay-verify="required"
                   placeholder="Pls input name" autocomplete="off" class="layui-input">
        </div>
        <div class="layui-form-mid">Enable:</div>
        <div class="layui-input-inline" style="width: 150px;">
            <input type="checkbox" id="enable" name="enable" <%=chked%> lay-skin="switch" lay-filter="enable"
                   class="layui-input">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">Description:</label>
        <div class="layui-input-block">
            <textarea id="desc" name="desc" required lay-verify="required" placeholder="" class="layui-textarea"
                      rows="2"><%=desc%></textarea>
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">Properties:</label>
        <div class="layui-input-block">

        </div>
    </div>
</form>
</body>
<script type="text/javascript">
    var form = null;
    layui.use('form', function () {
        form = layui.form;

        $("#name").on("input", function (e) {
            setDirty(true);
        });
        $("#title").on("input", function (e) {
            setDirty(true);
        });
        $("#desc").on("input", function (e) {
            setDirty(true);
        });
        form.on('switch(enable)', function (obj) {
            setDirty(true);
        });

        form.render();
    });

    var _tmpid = 0;

    var bdirty = false;
    var cp_id = "<%=cpid%>";
    var cp_tp = "<%=cp_tp%>";

    function isDirty() {
        return bdirty;
    }

    function setDirty(b) {
        if (!(b === false))
            b = true;
        bdirty = b;
        dlg.btn_set_enable(1, b);
    }


    function win_close() {
        dlg.close(0);
    }

    function get_input_val(id, defv, bnum) {
        var n = $('#' + id).val();
        if (n == null || n == '') {
            return defv;
        }
        if (bnum)
            return parseInt(n);
        return n;
    }

    function do_submit(cb) {
        var n = $('#name').val();
        if (n == null || n == '') {
            cb(false, 'Please input name');
            return;
        }
        var tt = $('#title').val();
        if (tt == null || tt == '') {
            cb(false, 'Please input title');
            return;
        }
        var ben = $("#enable").prop("checked");
        var desc = document.getElementById('desc').value;
        if (desc == null)
            desc = '';

        cb(true, {id: cp_id, name: n, title: tt, desc: desc, enable: ben, tp: cp_tp});
        //var dbname=document.getElementById('db_name').value;

        //document.getElementById('form1').submit() ;
    }

</script>
</html>