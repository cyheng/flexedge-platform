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
<%@ taglib uri="wb_tag" prefix="wbt" %>
<%
    if (!Convert.checkReqEmpty(request, out, "prjid", "cpid"))
        return;
    String repid = request.getParameter("prjid");
    String cpid = request.getParameter("cpid");
    String cptp = ConnProHTTP.TP;//request.getParameter("cptp") ;
    ConnProHTTP cp = (ConnProHTTP) ConnManager.getInstance().getConnProviderById(repid, cpid);
    if (cp == null) {
        out.print("no single provider found with " + cptp);
        return;
    }

    String connid = request.getParameter("connid");
    String cid = connid;
    if (cid == null)
        cid = "";
    ConnPtHTTP cpt = null;
    if (Convert.isNullOrEmpty(connid)) {
        cpt = new ConnPtHTTP();
        connid = cpt.getId();
    } else {
        cpt = (ConnPtHTTP) cp.getConnById(connid);
        if (cpt == null) {
            out.print("no connection found");
            return;
        }
    }

    String req_heads = cpt.getRequestHeads();
    String post_txt = cpt.getPostTxt();
    String name = cpt.getName();
    String title = cpt.getTitle();
    String chked = "";
    if (cpt.isEnable())
        chked = "checked='checked'";
    String desc = cpt.getDesc();
//String opc_appn = cpt.getOpcAppName();
//String opc_epuri  = cpt.getOpcEndPointURI();
    String url = cpt.getUrl();
    String method = cpt.getMethod();
    long int_ms = cpt.getIntervalMS();
    String cp_tp = cp.getProviderType();

    ConnPt.DataTp sor_tp = cpt.getSorTp();
//String init_js = cpt.getInitJS() ;
//String trans_js = cpt.getTransJS();

    String encod = cpt.getEncod();
    if (Convert.isNullOrEmpty(encod))
        encod = "UTF-8";
%>
<html>
<head>
    <title></title>
    <jsp:include page="../head.jsp"></jsp:include>
    <script>
        dlg.resize_to(800, 750);

    </script>
    <style type="text/css">
        .layui-form-item {
            margin-bottom: 5px;
        }

        .layui-input, .layui-select, .layui-textarea {
            height: 28px;
        }

        .layui-form-label {

            padding: 9px 15px;
            line-height: 15px;
        }

        .layui-form-mid {
            padding: 9px 0 !important;
            line-height: 10px;
        }

        .layui-form-switch {

            margin-top: 2px;
        }

    </style>
</head>
<body>
<form class="layui-form" onsubmit="return false;">
    <div class="layui-form-item">
        <label class="layui-form-label">Name:</label>
        <div class="layui-input-inline">
            <input type="text" id="name" name="name" value="<%=name%>" lay-verify="required" autocomplete="off"
                   class="layui-input">
        </div>
        <div class="layui-form-mid">Title:</div>
        <div class="layui-input-inline" style="width: 150px;">
            <input type="text" id="title" name="title" value="<%=title%>" lay-verify="required" autocomplete="off"
                   class="layui-input">
        </div>
        <div class="layui-form-mid">Enable:</div>
        <div class="layui-input-inline" style="width: 150px;">
            <input type="checkbox" id="enable" name="enable" <%=chked%> lay-skin="switch" lay-filter="enable"
                   class="layui-input">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">Url:</label>
        <div class="layui-input-inline" style="width:250px">
            <input type="text" id="url" name="url" value="<%=url %>" class="layui-input">
        </div>
        <label class="layui-form-label">Method</label>
        <div class="layui-input-inline" style="width: 70px;">
            <select id="method" lay-filter="method">
                <option value="GET" <%=("GET".equals(method) ? "selected=selected" : "") %>>GET</option>
                <option value="POST" <%=("POST".equals(method) ? "selected=selected" : "") %> >POST</option>
            </select>
        </div>
        <div class="layui-form-mid">Update Interval</div>
        <div class="layui-input-inline" style="width: 100px;">
            <input type="number" id="int_ms" name="int_ms" value="<%=int_ms%>" class="layui-input">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">Request Heads:</label>
        <div class="layui-input-inline" style="width:250px">
            <textarea id="req_heads" name="req_heads"
                      style="height:50px;width:100%;border-color: #e6e6e6"><%=req_heads %></textarea>
        </div>
        <label class="layui-form-label">Post Txt:</label>
        <div class="layui-input-inline" style="width:300px">
            <textarea id="post_txt" name="post_txt"
                      style="height:50px;width:100%;border-color: #e6e6e6"><%=post_txt %></textarea>
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">Response Probe Bind</label>
        <div class="layui-input-inline" style="width:650px;">
            <iframe id="if_msg" src="cpt_edit_msg.jsp?prjid=<%=repid%>&cpid=<%=cpid%>&connid=<%=cid%>"
                    style="width:100%;height:310px;border:1px solid;border-color:#e6e6e6"></iframe>
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">Description:</label>
        <div class="layui-input-inline" style="width:600px">
            <textarea id="desc" name="desc" style="height:30px;width:100%;border-color: #e6e6e6"><%=desc%></textarea>
        </div>
    </div>
</form>
</body>
<script type="text/javascript">
    var form = null;
    var sor_tp = "<%=sor_tp%>";
    var encod = "<%=encod%>";
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
        $("#url").on("input", function (e) {
            setDirty(true);
        });

        $("#int_ms").on("input", function (e) {
            setDirty(true);
        });
        $("#req_heads").on("input", function (e) {
            setDirty(true);
        });
        $("#post_txt").on("input", function (e) {
            setDirty(true);
        });

        form.on('switch(enable)', function (obj) {
            setDirty(true);
        });
        form.on('select(method)', function (obj) {
            setDirty(true);
        });
        $("#init_js").on("input", function (e) {
            setDirty(true);
        });

        $("#trans_js").on("input", function (e) {
            setDirty(true);
        });
        form.on('select(sor_tp)', function (obj) {
            setDirty(true);
        });
        form.on('select(encod)', function (obj) {
            setDirty(true);
        });

        $("#sor_tp").val(sor_tp);
        $("#encod").val(encod);
        form.render();
    });

    var _tmpid = 0;

    var bdirty = false;
    var cp_id = "<%=cpid%>";
    var cp_tp = "<%=cp_tp%>";
    var conn_id = "<%=connid%>";

    function isDirty() {
        return bdirty;
    }

    function setDirty(b) {
        if (!(b === false))
            b = true;
        bdirty = b;
        dlg.btn_set_enable(1, b);
    }

    function get_url() {
        return $("#url").val();
    }


    function win_close() {
        dlg.close(0);
    }

    function on_find_url_encod(enc) {
        alert(enc);
    }


    function edit_js_trans() {
        edit_js('trans_js', 'Transfer JS', '$topic,$msg', 'trans_sample');
    }

    function edit_js_init() {
        edit_js('init_js', 'Initial JS', '', '');
    }

    function edit_js(taid, tt, funcp, sample_id) {
        event.preventDefault();
        dlg.open("../ua_cxt/cxt_script.jsp?opener_txt_id=" + taid + "&sample_txt_id=" + sample_id + "&func_params=" + funcp,
            {title: tt}, ['Ok', 'Cancel'],
            [
                function (dlgw) {
                    var jstxt = dlgw.get_edited_js();
                    if (jstxt == null)
                        jstxt = '';
                    $("#" + taid).val(jstxt);
                    setDirty();
                    dlg.close();
                },
                function (dlgw) {
                    dlg.close();
                }
            ]);
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
            tt = n;
        }
        var ben = $("#enable").prop("checked");
        var desc = document.getElementById('desc').value;
        if (desc == null)
            desc = '';

        var url = $('#url').val();
        if (url == null || url == '') {
            cb(false, 'Please input Url');
            return;
        }

        var method = $('#method').val();
        if (method == null || method == '') {
            cb(false, 'Please select Method');
            return;
        }

        var int_ms = $('#int_ms').val();
        if (int_ms == null || int_ms == '') {
            cb(false, 'Please input Update interval');
            return;
        }
        int_ms = parseInt(int_ms);
        if (int_ms == NaN || int_ms < 0) {
            cb(false, 'Please input valid Update interval');
            return;
        }

        let req_heads = $("#req_heads").val();
        let post_txt = $("#post_txt").val();

        var msgob = null;
        $("#if_msg")[0].contentWindow.do_submit((bok, ret) => {
            if (!bok) {
                cb(false, ret);
                return;
            }
            msgob = ret;
        })

        var oball = Object.assign({
            id: conn_id,
            name: n,
            title: tt,
            desc: desc,
            enable: ben,
            url: url,
            method: method,
            int_ms: int_ms,
            req_heads: req_heads,
            post_txt: post_txt
        }, msgob);
        cb(true, oball);
    }

    function str2lns(str) {
        var arr = str.split('\n');
        var res = [];
        arr.forEach(function (item) {
            var ln = item.replace(/(^\s*)|(\s*$)/g, "").replace(/\s+/g, " ")
            if (ln == '')
                return;
            res.push(ln);
        })

        return res;
    }
</script>
</html>