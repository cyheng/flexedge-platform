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
<%@ taglib uri="wb_tag" prefix="wbt" %>
<%
    String taggpath = request.getParameter("tagg_path");
    String ppath = request.getParameter("ppath");
    boolean bedit = false;
    UANode pnode = null;
    UATagG tagg = null;
    if (Convert.isNotNullEmpty(taggpath)) {//edit
        bedit = true;
        tagg = (UATagG) UAUtil.findNodeByPath(taggpath);
        if (tagg == null) {
            out.print("no tagg found");
            return;
        }
        pnode = tagg.getParentNode();
    } else {
        pnode = UAUtil.findNodeByPath(ppath);
        if (pnode == null) {
            out.print("no parent node found");
            return;
        }
        tagg = new UATagG();
    }
    String name = tagg.getName();
    String title = tagg.getTitle();
    String desc = tagg.getDesc();

    String tt = bedit ? "edit,tagg" : "add,tagg";
%>
<html>
<head>
    <title>Tag Group Editor</title>
    <script src="/_js/jquery-1.12.0.min.js"></script>
    <link rel="stylesheet" type="text/css" href="/_js/layui/css/layui.css"/>
    <script src="/_js/dlg_layer.js"></script>
    <script src="/_js/layui/layui.all.js"></script>
    <script src="/_js/dlg_layer.js"></script>
    <script src="/_js/oc/oc.js"></script>
    <link type="text/css" href="/_js/oc/oc.css" rel="stylesheet"/>
    <script>
        dlg.resize_to(600, 400);
    </script>
</head>
<body>
<form class="layui-form" action="">
    <div class="layui-form-item">
        <label class="layui-form-label"><wbt:g>name</wbt:g>:</label>
        <div class="layui-input-block">
            <input type="text" id="name" name="name" value="<%=name %>" lay-verify="required" autocomplete="off"
                   class="layui-input">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label"><wbt:g>title</wbt:g>:</label>
        <div class="layui-input-block">
            <input type="text" id="title" name="title" value="<%=title %>" lay-verify="required" autocomplete="off"
                   class="layui-input">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label"><wbt:g>desc</wbt:g>:</label>
        <div class="layui-input-block">
            <input type="text" id="desc" name="desc" required lay-verify="required" placeholder="" autocomplete="off"
                   class="layui-input">
        </div>
    </div>
</form>
</body>
<script type="text/javascript">
    dlg.set_dlg_title("<wbt:g><%=tt%></wbt:g>");

    var name = "<%=name%>";

    layui.use('form', function () {
        var form = layui.form;
        if (!name)
            $("#name").focus();
        else
            $("#title").focus();
    });

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
            cb(false, '请输入名称');
            return;
        }
        var tt = $('#title').val();
        if (tt == null || tt == '') {
            //cb(false,'请输入标题') ;
            //return ;
            tt = n;
        }
        var desc = document.getElementById('desc').value;
        if (desc == null)
            desc = '';
        cb(true, {name: n, title: tt, desc: desc});
        //var dbname=document.getElementById('db_name').value;

        //document.getElementById('form1').submit() ;
    }

</script>
</html>