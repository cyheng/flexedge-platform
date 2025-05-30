<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="
	cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.dict.*,
                 cn.doraro.flexedge.core.store.*,
                 cn.doraro.flexedge.core.basic.*,
                 cn.doraro.flexedge.core.util.web.*,
                 cn.doraro.flexedge.core.store.*,
                 java.io.*,
                 java.util.*,
                 java.net.*,
                 java.util.*" %>
<%@ taglib uri="wb_tag" prefix="w" %>
<%
    String id = request.getParameter("id");

    if (id == null)
        id = "";
    String name = "";
    String title = "";
    String chked = "checked";
    String desc = "";
    String drv_name = "";
    String db_host = "";
//def

    String db_port_str = "";
    String db_name = "";
    String db_user = "";
    String db_psw = "";
    if (Convert.isNotNullEmpty(id)) {
        SourceJDBC st = (SourceJDBC) StoreManager.getSourceById(id);//.getSourceById(storeid) ;
        if (st == null) {
            out.print("no store found");
            return;
        }
        name = st.getName();
        title = st.getTitle();
        if (!st.isEnable())
            chked = "";
        drv_name = st.getDrvName();
        db_host = st.getDBHost();
        if (st.getDBPort() > 0)
            db_port_str = "" + st.getDBPort();
        db_name = st.getDBName();
        db_user = st.getDBUser();
        db_psw = st.getDBPsw();
        desc = st.getDesc();
    }
%>
<html>
<head>
    <title>jdbc editor</title>
    <script src="/_js/jquery-1.12.0.min.js"></script>
    <script type="text/javascript" src="/_js/ajax.js"></script>
    <script src="/_js/layui/layui.all.js"></script>
    <script src="/_js/dlg_layer.js"></script>
    <link rel="stylesheet" type="text/css" href="/_js/layui/css/layui.css"/>
    <script>
        dlg.resize_to(700, 600);
    </script>
    <style>
    </style>
</head>
<body>
<form class="layui-form" action="">
    <div class="layui-form-item">
        <label class="layui-form-label"><w:g>name</w:g>:</label>
        <div class="layui-input-inline" style="width: 150px;">
            <input type="text" id="name" name="name" value="<%=name%>" autocomplete="off"
                   class="layui-input" <%=Convert.isNotNullEmpty(name)?"readonly":"" %>>
        </div>
        <div class="layui-form-mid"><w:g>title</w:g>:</div>
        <div class="layui-input-inline" style="width: 150px;">
            <input type="text" id="title" name="title" value="<%=title%>" autocomplete="off" class="layui-input">
        </div>
        <div class="layui-form-mid"><w:g>enable</w:g>:</div>
        <div class="layui-input-inline" style="width: 150px;">
            <input type="checkbox" id="enable" name="enable" <%=chked%> lay-skin="switch" lay-filter="enable"
                   class="layui-input">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label"><w:g>driver</w:g>:</label>
        <div class="layui-input-inline" style="width: 150px;">
            <select id="drv_name" lay-filter="drv_name">
                <option value="">--</option>
                <%
                    for (SourceJDBC.Drv drv : SourceJDBC.listJDBCDrivers()) {
                        String defpstr = drv.getDefaultPortStr();
                %>
                <option value="<%=drv.getName() %>" jdbc_port_def="<%=defpstr %>"><%=drv.getTitle() %>
                </option>
                <%
                    }
                %>
            </select>
        </div>
        <div class="layui-form-mid host_port">DB <w:g>host</w:g>:</div>
        <div class="layui-input-inline host_port">
            <input type="text" id="db_host" name="db_host" value="<%=db_host%>" autocomplete="off" class="layui-input">
        </div>
        <div class="layui-form-mid host_port"><w:g>port</w:g>:</div>
        <div class="layui-input-inline host_port" style="width: 100px;">
            <input type="number" id="db_port" name="db_port" value="<%=db_port_str%>" autocomplete="off"
                   class="layui-input">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">DB <w:g>name</w:g>:</label>


        <div class="layui-input-inline" style="width: 150px;">
            <input type="text" id="db_name" name="db_name" value="<%=db_name%>" autocomplete="off" class="layui-input">
        </div>
        <div class="layui-form-mid user_psw"><w:g>user</w:g>:</div>
        <div class="layui-input-inline user_psw" style="width: 130px;">
            <input type="text" id="db_user" name="db_user" value="<%=db_user%>" autocomplete="off" class="layui-input">
        </div>
        <div class="layui-form-mid user_psw"><w:g>psw</w:g>:</div>
        <div class="layui-input-inline user_psw" style="width: 130px;">
            <input type="password" id="db_psw" name="db_psw" value="<%=db_psw%>" autocomplete="off" class="layui-input">
        </div>
    </div>

    <div class="layui-form-item">
        <label class="layui-form-label"><w:g>desc</w:g>:</label>
        <div class="layui-input-block" style="width: 450px;">
            <textarea id="desc" name="desc" required lay-verify="required" placeholder="" class="layui-textarea"
                      rows="2"><%=desc%></textarea>
        </div>
    </div>
</form>
</body>
<script type="text/javascript">
    var id = "<%=id%>";

    layui.use('form', function () {
        var form = layui.form;
        form.on("select(drv_name)", function (obj) {
            //let dbport = $("#db_port").val() ;
            if (!id) {
                let pdef = $("#drv_name").find("option:selected").attr("jdbc_port_def");
                $("#db_port").val(pdef);
                form.render();
            }

            update_ui();
        });

        $("#drv_name").val("<%=drv_name%>");
        form.render();
    });

    function update_ui() {
        let drv_name = $("#drv_name").val();
        if (drv_name == 'sqlite') {
            $(".user_psw").css("display", "none");
            $(".host_port").css("display", "none");

        } else {
            $(".user_psw").css("display", "");
            $(".host_port").css("display", "");
        }
    }

    update_ui();

    function win_close() {
        dlg.close(0);
    }

    function do_submit(cb) {
        var n = $('#name').val();
        if (n == null || n == '') {
            cb(false, '<w:g>pls,input,name</w:g>');
            return;
        }
        var tt = $('#title').val();
        if (tt == null || tt == '') {
            tt = n;
        }

        var desc = "";//document.getElementById('desc').value;
        if (desc == null)
            desc = '';

        var ben = $("#enable").prop("checked");

        var drv_name = $('#drv_name').val();
        if (!drv_name) {
            cb(false, "<w:g>pls,select,driver</w:g>");
            return;
        }

        let db_host = "";
        let db_port = 0;
        let db_user = "";
        let db_psw = "";

        if (drv_name != 'sqlite') {
            db_host = $('#db_host').val();
            if (!db_host) {
                cb(false, "<w:g>pls,input,host</w:g>");
                return;
            }
            db_port = parseInt($('#db_port').val());
            if (db_port == NaN) {
                cb(false, "port > 0 ");
                return;
            }
            db_user = $('#db_user').val();
            if (!db_user) {
                cb(false, "<w:g>pls,input,user</w:g>");
                return;
            }
            db_psw = $('#db_psw').val();
        }

        var db_name = $('#db_name').val();
        if (!db_name) {
            cb(false, "<w:g>pls,input</w:g> DB <w:g>name</w:g>");
            return;
        }

        cb(true, {
            id: id, _tp: "jdbc", name: n, title: tt, enable: ben, desc: desc, drv_name: drv_name,
            db_host: db_host, db_port: db_port, db_name: db_name, db_user: db_user, db_psw: db_psw
        });
        //var dbname=document.getElementById('db_name').value;

        //document.getElementById('form1').submit() ;
    }

</script>
</html>