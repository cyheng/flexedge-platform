<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ page
        import="java.util.*,
                java.io.*,
                java.util.*,
                cn.doraro.flexedge.core.*,
                cn.doraro.flexedge.core.util.*,
                cn.doraro.flexedge.core.res.*,
                cn.doraro.flexedge.core.comp.*,
                java.net.*" %>
<%@ taglib uri="wb_tag" prefix="wbt" %>
<%
    if (!Convert.checkReqEmpty(request, out, "tabid", "libid", "catid", "id"))
        return;
    //String op = request.getParameter("op");
    String libid = request.getParameter("libid");
    String tabid = request.getParameter("tabid");
    String catid = request.getParameter("catid");
    String id = request.getParameter("id");
    CompManager cm = CompManager.getInstance();
    CompLib lib = cm.getCompLibById(libid);
    CompCat cc = lib.getCatById(catid);
    if (cc == null) {
        out.print("cat not found");
        return;
    }
    CompItem ci = cc.getItemById(id);
    if (ci == null) {
        out.print("no item found");
        return;
    }
    String res_lib_id = lib.getResLibId();//.getResNodeUID();
%><!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>HMI Component Editor</title>
    <jsp:include page="../head.jsp">
        <jsp:param value="true" name="oc"/>
    </jsp:include>

    <style>
        body {
            margin: 0px;
            padding: 0px;
            font-size: 12px;
            text-align: center;
            -moz-user-select: none;
            -webkit-user-select: none;
        }

        .top {
            position: fixed;

            left: 0;
            top: 0;
            bottom: 0;
            z-index: 999;
            height: 45px;
            width: 100%;
            text-align: left;
            margin: 0px;
            padding: 0px;
            overflow: hidden
        }


        .left {
            position: fixed;
            float: left;
            left: 0;
            top: 45px;
            bottom: 0;
            z-index: 999;
            width: 45px;
            overflow-x: hidden
        }


        .left_pan {
            position: fixed;
            float: left;
            left: 45px;
            top: 45px;
            bottom: 0;
            z-index: 999;
            width: 145px;
            overflow-x: hidden
        }

        .right {
            position: fixed;
            float: right;
            right: 0;
            top: 45px;
            bottom: 0;
            z-index: 999;
            width: 250px;
            height: 100%;
            overflow-x: hidden
        }

        .mid {
            position: absolute;
            left: 45px;
            right: 250px;
            top: 45px;
            bottom: 0;
            z-index: 998;
            width: auto;
            overflow: hidden;
            box-sizing: border-box
        }

        .top_btn {
            color: #ffffff;
            margin-top: 5px;
            margin-left: 20px;
            cursor: pointer;
        }

        .top i:hover {
            color: #fdd000;
        }

        .lr_btn {
            margin-top: 10px;
            color: #009999;
            cursor: pointer;
        }

        .lr_btn_div {
            margin-top: 0px;
            color: #858585;
            background-color: #eeeeee;
            cursor: pointer;
        }

        .lr_btn_btm {
            margin-bottom: 20px;
            position: absolute;
            left: 5px;
            bottom: 20px;
            color: #858585;

            cursor: pointer;
        }

        .left i:hover {
            color: #fdd000;
        }

        .lr_btn i:hover {
            color: #fdd000;
        }

        .right i:hover {
            color: #ffffff;
        }

        .props_panel_edit {
            position0: absolute;
            left: 0px;
            right: 0px;
            top: 18px;
            bottom0: 50px;
            height: 80%
            z-index: 998;

            overflow-y: auto;
            vertical-align: top;
            box-sizing: border-box
        }

        .props_panel_pos {
            position: absolute;
            bottom: 50px;

            z-index: 998;
            box-sizing: border-box
        }

        .top_menu_close {
            font-family: Tahoma;
            border: solid 2px #ccc;
            padding: 0px 5px;
            text-align: center;
            font-size: 12px;
            color: blue;
            position: absolute;
            top: 2px;
            line-height: 14px;
            height: 14px;
            width: 26px;
            border-radius: 14px;
            -moz-border-radius: 14px;
            background-color: white;
        }

        .top_menu_left {
            position: absolute;
            z-index: 50000;
            width: 25;
            height: 25;
            TOP: 100px;
            right: 0px;
            text-align: center;
            font-size: 12px;
            font-weight: bold;
            background-color: #4770a1;
            color: #eeeeee;
            line-height: 35px;
            border: 2px solid;
            border-radius: 5px;
        / / box-shadow: 5 px 5 px 2 px #888888;
        }

        .top_win_left {
            border: solid 3px gray;
            background-color: silver;
            top: 0;
            left: 30;
            height: 230;
            width: 830;
            padding: 1px;
            line-height: 21px;
            border-radius: 15px;
            -moz-border-radius: 15px;
            box-shadow: 0 5px 27px rgba(0, 0, 0, 0.3);
            -webkit-box-shadow: 0 5px 27px rgba(0, 0, 0, 0.3);
            -moz-box-shadow: 0 5px 27px rgba(0, 0, 0, 0.3);
            _position: absolute;
            _display: block;
            z-index: 10000;
        }

        .left_panel_win {
            position: absolute;
            display: none;
            z-index: 1000;
            left: 45px;
            background-color: #eeeeee;
            top: 0px;
            height: 100%;
        }

        .left_panel_bar {
            height: 30px;
        }


        .layui-tab {
            margin: 0px;
            padding: 0px;
            text-align: left !important;
            height: 35px;
        }


        .layui-tab-content {
            padding: 0px;
        }

        .layui-tab-title .layui-this {
            background-color: #aaaaaa;
            top: 0px;
            margin: 0px;
            padding-top: 0px;
            padding-bottom: 0px;

        }

        .edit_toolbar {
            height: 50px;
            background-color: grey;
        }

        .edit_toolbar button {
            width: 40px;
            height: 40px;
            margin-top: 5px;
            float: left;
            margin-left: 5px;
        }


    </style>
</head>
<script type="text/javascript">


</script>
<body class="layout-body">
<div class="top " style="background-color: #007ad4;color:#ffffff;">
    <div style="float: left;position:relative;left:0px;margin-left:5px;top:2px;font: 30px solid;font-weight:600;font-size:16px;color:#d6ccd4">
        <img src="../inc/logo1.png" width="40px" height="40px"/>IOTTree HMI <wbt:g>comp,editor</wbt:g></div>
    <div style="float: left;position:relative;left:100px;margin-left:5px;top:2px;font: 25px solid">
        <%=lib.getTitle() %>-<%=cc.getTitle()%>-<%=ci.getTitle()%>
    </div>
    <div style="float: right;margin-right:10px;margin-top:10px;font: 20px solid;color:#ffffff">

        <button class="layui-btn layui-btn-warm" onclick="up_to_prj()"><wbt:g>up_to_prjs</wbt:g></button>
        &nbsp;
        <i class="fa-brands fa-squarespace fa-lg top_btn" onclick="open_res()" title="<wbt:g>resources</wbt:g>"></i>
        <i class="fa fa-floppy-disk fa-lg top_btn" onclick="tab_save()" title="<wbt:g>save,this,comp</wbt:g>"></i>
        <i id="lr_btn_fitwin" class="fa fa-crosshairs fa-lg top_btn" onclick="draw_fit()" title="show fit"></i>
    </div>
</div>

<div class="left " style="background-color: #aaaaaa">
    <%--
        <div id="leftcat_basic_di" onclick="leftcat_sel('basic_di','Basic')"><i class="fa fa-circle-o fa-3x lr_btn" ></i><br>Basic</div>
         --%>
    <div id="leftcat_basic_icon" onclick="leftcat_sel('basic_icon','Basic Icons')"><i
            class="fa-regular fa-image fa-3x lr_btn"></i><br>Icon
    </div>
    <%--
                <div id="leftcat_pic" onclick="leftcat_sel('pic','Pictures Lib',500)"><i class="fa fa-cubes fa-3x lr_btn"></i><br>Pic Lib</div>
                 --%>
</div>
<div id="left_panel" class="left_panel_win" pop_width="300px">
    <div class="left_panel_bar">
        <span id="left_panel_title" style="font-size: 20px;">Basic Shape</span>
        <div onclick="leftcat_close()" class="top_menu_close" style="position:absolute;top:1px;right:10px;top:2px;">X
        </div>
    </div>
    <iframe id="left_pan_iframe" src=""
            style="width:100%;height:99%;overflow:hidden;margin: 0px;border:0px;padding: 0px"></iframe>
</div>
<div class="mid">
    <div id="main_panel" style="border: 0px solid #000; width: 100%; height: 100%; background-color: #1e1e1e"
         ondrop0="drop(event)" ondragover0="allowDrop(event)">
        <div id="win_act_store" style="position: absolute; display: none; background-color: #cccccc;z-index:1">
            <div class="layui-btn-group">
                <button type="button" class="layui-btn layui-btn-primary layui-btn-sm" title="新增数据库"
                        onclick="store_add_db()">
                    <i class="layui-icon">&#xe654;</i>
                </button>
                <button type="button" class="layui-btn layui-btn-primary layui-btn-sm">
                    <i class="layui-icon">&#xe642;</i>
                </button>
                <button type="button" class="layui-btn layui-btn-primary layui-btn-sm">
                    <i class="layui-icon">&#xe640;</i>
                </button>
            </div>
        </div>

        <div id="win_act_conn" style="position: absolute; display: none; background-color: #cccccc;z-index:1">
            <div class="layui-btn-group" style="width:40px">
                <button type="button" class="layui-btn layui-btn-primary layui-btn-sm" title="add new connector"
                        onclick="conn_add()">
                    <i class="layui-icon">&#xe654;</i>
                </button>
                <button type="button" class="layui-btn layui-btn-primary layui-btn-sm">
                    <i class="layui-icon">&#xe642;</i>
                </button>
                <button type="button" class="layui-btn layui-btn-primary layui-btn-sm">
                    <i class="layui-icon">&#xe640;</i>
                </button>
            </div>
        </div>


    </div>
    <div class="right " style0="background-color: #eeeeee;display:flex;flex-direction: column;">

        <div id="p_info" style="position:absolute;bottom:0px;width:100%;background-color: #cccccc; height: 30px"
             class="props_panel_pos">&nbsp;
        </div>
        <div style="position0: absolute; width: 100%; height:90%; border:1 solid;border-color: red">
            <div class="layui-tab">
                <ul class="layui-tab-title">
                    <li class="layui-this"><wbt:g>props</wbt:g></li>
                    <li><wbt:g>evts</wbt:g></li>
                </ul>
                <div class="layui-tab-content">
                    <div class="layui-tab-item layui-show">
                        <div id='edit_props' style="width:100%;height:600px;overflow: auto;"></div>
                    </div>
                    <div class="layui-tab-item">
                        <div id='edit_events' style="width:100%;height:600px;overflow: auto;"></div>
                    </div>

                </div>
            </div>
            <%--
                            <div id='edit_panel' style="border: 1 solid; font: 15; position: absolute; top: 20px; width: 100%; bottom:0px; background-color: window; z-index: 60000;overFlow: auto;">
                                 <div id='edit_props' ></div>
                                 <div id='edit_events' ></div>
                            </div>
                             --%>
        </div>

    </div>


    <div id="toolbar_basic" class="toolbox" style="left:50px">

        <div class="content" style="height:200px">
            <div style="height:50px;background-color: grey" class="edit_toolbar">
                <button id="oper_save" title="save"><i class="fa fa-floppy-disk fa-2x"></i></button>
                <span id="edit_toolbar" class="edit_toolbar"></span>
            </div>
            <iframe src="hmi_left_basic_di.jsp" height="150px" width="100%"></iframe>
        </div>
    </div>

    <div id="inter_editor" class="toolbox" style="left:50px;top:385px;">
        <div class="title"><h3><wbt:g>interface</wbt:g></h3></div>
        <div class="content" style="height:180px">
            <div id='inter_prop_panel'
                 style="border: 1; font: 15; flex: 1;width: 100%;  background-color: #f9f9f9; z-index: 60000; ;float:left">
                <div style="position0:relative;top:0px;height:20px;background-color: grey;width:100%;float:left">
                    <wbt:g>props</wbt:g>
                    <button onclick="inter_prop_edit()">+<wbt:g>add</wbt:g></button>
                    <button onclick="inter_prop_test()"><wbt:g>test</wbt:g></button>
                    <button onclick="inter_prop_paste()"><wbt:g>paste</wbt:g></button>
                    <button onclick="inter_prop_map()"><wbt:g>do_map</wbt:g></button>
                </div>
            </div>
            <div id="inter_prop_list"
                 style="position0:absolute;top:20px;height:160px;width:100%;background-color: #f9f9f9;overflow: auto;">

            </div>
        </div>
        <div id='inter_event_panel'
             style="border: 1; font: 15; flex: 1;width: 100%;  background-color: window; z-index: 60000; ">
            <div style="position0:absolute;top:0px;height:20px;background-color: grey;width:100%">
                <wbt:g>evts</wbt:g>
                <button onclick="inter_event_edit()">+<wbt:g>add</wbt:g></button>
            </div>
            <div id="inter_event_list" style="position0:absolute;top:20px;height:120px;width:100%">

            </div>

        </div>

    </div>
</div>


</div>


<script>

    toolbox_init("#inter_editor");
    toolbox_init("#toolbar_basic");

    var libid = "<%=libid%>";
    var tab_id = "<%=tabid%>";
    var catid = "<%=catid%>";
    var itemid = "<%=id%>"
    var editname = "<%=ci.getEditorName()%>";
    var res_lib_id = "<%=res_lib_id%>";
    var res_id = "<%=id%>";

    var ctrl_items = [];

    layui.use('element', function () {
        var element = layui.element;

        //…
    });

    $('#oper_save').click(function () {
        tab_save();
    });


    var panel = null;
    var editor = null;

    var loadLayer = null;
    var intedit = null;

    var hmiModel = null;
    var hmiView = null;

    function open_res() {
        dlg.open("../util/di_editplug_prop_imgres.jsp?res_lib_id=" + res_lib_id + "&res_id=" + res_id,
            {title: "Edit Resourse", w: '500px', h: '400px'},
            ['Close'],
            [
                function (dlgw) {
                    dlg.close();
                }
            ]);

    }

    function on_panel_mousemv(p, d) {
        $("#p_info").html("[" + p.x + "," + p.y + "] - (" + Math.round(d.x * 10) / 10 + "," + Math.round(d.y * 10) / 10 + ")");
    }

    function init_iottpanel() {
        hmiModel = new oc.hmi.HMICompModel({
            comp_url: "comp_ajax.jsp?op=comp_txt&libid=" + libid + "&catid=" + catid + "&id=" + itemid,
        });

        panel = new oc.hmi.HMICompPanel(itemid, res_lib_id, res_id, "main_panel", {
            on_mouse_mv: on_panel_mousemv,
            on_model_chg: on_model_chg
        });
        panel.setInEdit(true);
        editor = new oc.DrawEditor("edit_props", "edit_events", "edit_toolbar", panel, {
            plug_cb: editor_plugcb,
            on_prompt_msg: on_editor_prompt
        });
        hmiView = new oc.hmi.HMICompView(hmiModel, panel, editor, {
            copy_paste_url: "../util/copy_paste_ajax.jsp",
            loaded_cb: () => {
                inter_refresh();
                setTimeout("draw_fit()", 1000);
            }
        });

        loadLayer = hmiView.getLayer();
        intedit = hmiView.getInteract();
        hmiView.init();
    }


    function on_editor_prompt(m) {
        dlg.msg(m);
    }

    var editor_plugcb_pm = {layer: loadLayer, editor: editname, editor_id: itemid, catid: catid, compid: itemid};

    function editor_plugcb(jq_ele, tp, di, pn_defname, name, val) {
        editor_plugcb_pm = {
            layer: loadLayer,
            editor: editname,
            editor_id: itemid,
            catid: catid,
            compid: itemid,
            di: di,
            name: name,
            val: val
        };

        if (tp.indexOf("event_") == 0) {
            dlg.open("../util/di_editplug_" + tp + ".jsp?sjs=false&compid=" + itemid,
                {title: "Edit Event", w: '500px', h: '400px'},
                ['Ok', 'Cancel', 'Help'],
                [
                    function (dlgw) {
                        var ret = dlgw.editplug_get();
                        var cjs = ret.clientjs;
                        var sjs = ret.serverjs;
                        if (cjs == null)
                            cjs = "";
                        if (sjs == null)
                            sjs = "";
                        di.setEventBinder(name, cjs, sjs);
                        editor.refreshEventEditor();
                        dlg.close();
                    },
                    function (dlgw) {
                        dlg.close();
                    },
                    function (dlgw) {
                        doc_help("")
                    }
                ]);
        } else if (tp == "prop_bind") {
            dlg.open("./hmi_editor_comp_prop_interface.jsp?res_lib_id=" + res_lib_id + "&res_id=" + itemid,
                {title: "To Interface Map", w: '500px', h: '400px'},
                ['Ok', 'Cancel'],
                [
                    function (dlgw) {

                        dlg.close();
                    },
                    function (dlgw) {
                        dlg.close();
                    }
                ]);
        } else {
            dlg.open("../util/di_editplug_" + tp + ".jsp?res_lib_id=" + res_lib_id + "&res_id=" + itemid,
                {title: "Edit Properties", w: '500px', h: '400px'},
                ['Ok', 'Cancel'],
                [
                    function (dlgw) {
                        if (tp == "prop_bind") {
                            var ret = dlgw.editplug_get();
                            if (ret.unbind) {
                                di.setPropBinder(name, null, false);
                            } else {
                                di.setPropBinder(name, ret.jstxt, ret.bexp);
                            }

                            editor.refreshPropBindEditor();
                        } else {
                            var ret = dlgw.editplug_get();
                            var v = ret.v;
                            jq_ele.val(v);
                            editor.applyUI2SelectedItem();
                        }

                        dlg.close();
                    },
                    function (dlgw) {
                        dlg.close();
                    }
                ]);
        }

    }

    function inter_prop_test() {
        dlg.open("comp_inter_prop_tester.jsp?libid=" + libid + "&catid=" + catid + "&id=" + itemid,
            {title: "Component Tester", w: '500px', h: '400px'},
            ['Close'],
            [
                function (dlgw) {
                    dlg.close();
                }
            ]);
    }

    function inter_refresh() {
        var compinter = loadLayer.getCompInter();
        var tmps = "";
        let ips = compinter.getInterProps();
        for (let i = 0; i < ips.length; i++) {
            let ci = ips[i];
            if (ci.isInnerItemMap())
                tmps += "<div>" + ci.t + "[" + ci.n + "] <a href='javascript:inter_prop_del(\"" + ci.n + "\")'>del</a></div>";
            else {
                tmps += "<div>" + ci.t + "[" + ci.n + "] <a href='javascript:inter_prop_edit(\"" + ci.n + "\")'>edit</a> <a href='javascript:inter_prop_del(\"" + ci.n + "\")'>del</a> <a href='javascript:inter_prop_copy(\"" + ci.n + "\")'>copy</a>";
                if (i > 0)
                    tmps += " <a href='javascript:updown_prop_edit(\"" + ci.n + "\",true)'><i class='fa-solid fa-arrow-up'></i></a>";
                if (i < ips.length - 1)
                    tmps += " <a href='javascript:updown_prop_edit(\"" + ci.n + "\",false)'><i class='fa-solid fa-arrow-down'></i></a>";
                tmps += "</div>";
            }

        }
        $("#inter_prop_list").html(tmps);

        tmps = "";
        for (var ci of compinter.getInterEvents()) {
            tmps += "<div>" + ci.t + "[" + ci.n + "] <a href='javascript:inter_event_edit(\"" + ci.n + "\")'>edit</a> <a href='javascript:inter_event_del(\"" + ci.n + "\")'>del</a></div>";
        }
        $("#inter_event_list").html(tmps);
    }

    function inter_prop_del(n) {
        loadLayer.getCompInter().setInterProp(n, null);
        inter_refresh();
    }

    function inter_prop_copy(n) {
        var ci = loadLayer.getCompInter().getInterPropByName(n);
        if (ci == null)
            return;
        send_ajax("../util/copy_paste_ajax.jsp", {
            op: "common_copy",
            n: "_hmicomp_inter_prop",
            t: JSON.stringify(ci)
        }, (bsucc, ret) => {
            if (!bsucc || ret != 'succ') {
                dlg.msg(ret);
                return;
            }
            dlg.msg("copy ok");
        });
    }

    function inter_prop_paste() {
        send_ajax("../util/copy_paste_ajax.jsp", {op: "common_paste", n: "_hmicomp_inter_prop"}, (bsucc, ret) => {
            if (!bsucc || ret.indexOf('succ=') != 0) {
                dlg.msg(ret);
                return;
            }
            var txt = ret.substring(5);
            var ci = null;
            eval("ci=" + txt);
            var n = ci.n;
            if (n == undefined || n == null || n == "") {
                dlg.msg("invalid property item");
                return;
            }
            //var tp = ret.tp ;
            loadLayer.getCompInter().setInterProp(n, ci);
            inter_refresh();

            dlg.msg("paste ok");
        });
    }

    var map_li_items = [];
    var map_sel_items = [];

    function inter_prop_map() {
        let sdi = editor.getSingleSelectedItem();
        if (sdi == null) {
            dlg.msg("please select one Item")
            return;
        }

        let n = sdi.getName();
        if (!n) {
            dlg.msg("select item has no Name ,please set Name first");
            return;
        }
        var compinter = loadLayer.getCompInter();
        let bps = sdi.listAllBindPropDef()
        let mitems = [];
        for (let bp of bps) {
            let newn = n + "_" + bp.id;
            let cip = compinter.getInterPropByName(newn);
            mitems.push({
                id: newn,
                title: bp.title,
                sel: cip != null,
                itemid: sdi.getId(),
                propn: bp.id,
                prop_vt: bp.tp
            });
        }
        map_li_items = mitems;

        dlg.open("../util/dlg_sel_in_list.jsp?opener_list_id=map_li_items",
            {title: "Map To Interface Properties", w: '500px', h: '400px'},
            ['Ok', 'Cancel'],
            [
                function (dlgw) {
                    var ids = dlgw.get_select();
                    for (let tmpitem of map_li_items) {
                        let tmpid = tmpitem.id;
                        tmpitem.sel = (ids.indexOf(tmpid) >= 0);
                    }
                    loadLayer.getCompInter().setInterPropMap(map_li_items);
                    inter_refresh();

                    dlg.msg("interface properties map ok");

                    dlg.close();
                },
                function (dlgw) {
                    dlg.close();
                }
            ]);
    }

    function inter_event_del(n) {
        loadLayer.getCompInter().setInterEvent(n, null);
        inter_refresh();
    }

    function updown_prop_edit(n, b_up) {
        let b = loadLayer.getCompInter().upOrDownInterProp(n, b_up);
        if (b)
            inter_refresh();
    }

    function inter_prop_edit(n) {
        var tt = "Add Interface Property";
        if (n == undefined || n == null)
            n = "";
        else
            tt = "Edit Interface Property";
        dlg.open("comp_inter_prop_edit.jsp?n=" + n + "&libid=" + libid + "&catid=" + catid + "&id=" + itemid,
            {title: tt, w: '500px', h: '400px'},
            ['Ok', 'Cancel'],
            [
                function (dlgw) {
                    dlgw.do_submit(function (bsucc, ret) {
                        if (!bsucc) {
                            dlg.msg(ret);
                            return;
                        }
                        var n = ret.n;
                        if (n == undefined || n == null || n == "") {
                            dlg.msg("invalid property item");
                            return;
                        }
                        //var tp = ret.tp ;
                        loadLayer.getCompInter().setInterProp(n, ret);
                        inter_refresh();
                        dlg.close();
                    });
                },
                function (dlgw) {
                    dlg.close();
                }
            ]);
    }


    function inter_event_edit(n) {
        var tt = "Add Interface Event";
        if (n == undefined || n == null)
            n = "";
        else
            tt = "Edit Interface Event";
        dlg.open("comp_inter_event_edit.jsp?n=" + n + "&libid=" + libid + "&catid=" + catid + "&id=" + itemid,
            {title: tt, w: '500px', h: '400px'},
            ['Ok', 'Cancel'],
            [
                function (dlgw) {
                    dlgw.do_submit(function (bsucc, ret) {
                        if (!bsucc) {
                            dlg.msg(ret);
                            return;
                        }
                        var n = ret.n;
                        if (n == undefined || n == null || n == "") {
                            dlg.msg("invalid event item");
                            return;
                        }
                        loadLayer.getCompInter().setInterEvent(n, ret);
                        inter_refresh();
                        dlg.close();
                    });
                },
                function (dlgw) {
                    dlg.close();
                }
            ]);
    }

    function do_apply() {
        if (!editor.applyUI2SelectedItem()) {
            dlg.msg("apply failed");
            return;
        }
        //panel.on_draw();
    }

    function do_add_di(dicn, opts) {
        if (intedit == null)
            return;
        if (!intedit.setOperAddItem(dicn, opts)) {
            dlg.msg("set oper error");
            return;
        }

    }

    function do_add_pts(tp, opts) {
        if (intedit == null)
            return;
        if (!intedit.setOperAddPts(tp, opts)) {
            dlg.msg("set oper error");
            return;
        }

    }

    function do_add_unit_ins(unitid) {
        if (intedit == null)
            return;
        if (!intedit.setOperAddUnitIns(unitid)) {
            dlg.msg("set oper add unit ins error");
            return;
        }

    }

    function on_model_chg() {
        tab_notify();
    }

    function tab_save() {
        var pm = {};
        pm.op = "comp_txt_save";
        pm.libid = libid;
        pm.catid = catid;
        pm.id = itemid;
        pm.txt = JSON.stringify(loadLayer.extract(null));
        oc.util.doAjax("comp_ajax.jsp", pm, (bsucc, ret) => {
            dlg.msg(ret);
            if ("save ok" == ret) {
                panel.setModelDirty(false);
                tab_notify();
            }

        });
    }

    window.addEventListener("keydown", function (e) {
        if ((e.key == 's' || e.key == 'S') && (navigator.platform.match("Mac") ? e.metaKey : e.ctrlKey)) {//ctrl + s
            e.preventDefault();
            tab_save();
        }
    }, false);

    function tab_notify() {
        //parent.tab_notify(tab_id);
    }

    function tab_st() {
        return {tabid: tab_id, dirty: panel.isModelDirty()};
    }

    function draw_fit() {
        if (loadLayer == null)
            return;
        loadLayer.ajustDrawFit();
    }

    var bInRefresh = false;
    var lastRefreshDT = -1;

    function refresh_dyn() {
        if (bInRefresh)
            return;
        if (new Date().getTime() - lastRefreshDT < 2000)
            return;
        try {
            bInRefresh = true;
            hmiModel.refreshDyn(function () {
                lastRefreshDT = new Date().getTime();
                bInRefresh = false;
            });
        } finally {

        }
    }


    layui.use('form', function () {

    });


    //////////edit panel
    $(document).ready(function () {
        $('#edit_panel_btn').click(function () {
            $('#edit_panel').slideToggle();
            $(this).toggleClass("cerrar");
        });

        $('#lr_btn_fitwin').click(function () {
            //draw_fit();
        });

        init_iottpanel();

        //init_top_menu();
    });

    function slide_toggle(obj, w) {
        if (obj.attr('topm_show') == '1') {
            obj.animate({width: '0px', opacity: 'hide'}, 'normal', function () {
                obj.hide();
            });
            obj.attr('topm_show', "0");
            return 0;
        } else {
            obj.animate({width: w, opacity: 'show'}, 'normal', function () {
                obj.show();
            });
            obj.attr('topm_show', "1");
            return 1;
        }
    }

    function hide_toggle(obj) {
        obj.hide();
        obj.attr('topm_show', "0");
    }


    var left_cur = null;

    function leftcat_sel(n, t, w) {
        if (w == undefined)
            w = "300px";
        else
            w = w + "px";
        if (left_cur != null) {
            //slide_toggle($('#left_panel'));
            hide_toggle($('#left_panel'))
            if (left_cur == n) {//close only
                $('.lr_btn_div').removeClass("lr_btn_div");
                left_cur = null;
                return;
            }
        }

        //if()
        left_cur = n;
        $('.lr_btn_div').removeClass("lr_btn_div");
        $("#leftcat_" + n).addClass("lr_btn_div");
        $("#left_panel_title").html(t);
        if ("basic_icon" == n)
            document.getElementById("left_pan_iframe").src = "../pic/icon_fa.jsp";
        else
            document.getElementById("left_pan_iframe").src = "hmi_left_" + n + ".jsp";

        //top_menu_hide_other('filter');
        //$('#left_panel').hide();
        //$('#topm_filter_panel').slideToggle();
        var r = slide_toggle($('#left_panel'), w);
        //$(this).toggleClass("top_menu_tog");
    }

    function leftcat_close() {
        $('.lr_btn_div').removeClass("lr_btn_div");
        left_cur = null;
        slide_toggle($('#left_panel'));
    }

    function fit_right_height() {
        var hpx = ($(window).height() - 100) + "px";
        $("#edit_props").css("height", hpx)
        $("#edit_events").css("height", hpx)
    }

    fit_right_height();

    var resize_cc = 0;
    $(window).resize(function () {
        panel.updatePixelSize();
        resize_cc++;
        if (resize_cc <= 1)
            draw_fit();
    });

    var ref_list = null;

    var pre_prj = "<%=IResCxt.PRE_PRJ%>"
    var pre_devdef = "<%=IResCxt.PRE_DEVDEF%>"

    function up_to_refs(ids, pre) {
        if (!ids || ids.length <= 0)
            return;
        send_ajax("comp_ajax.jsp", {
            op: "up_to_ref",
            ids: combine_to_str(ids, ','),
            pre: pre,
            libid: libid,
            compid: res_id
        }, function (bsucc, ret) {
            if (!bsucc || ret.indexOf("succ=") != 0) {
                dlg.msg(ret);
                return;
            }
            var scc = ret.substring(5);
            dlg.msg("Successfully updated " + scc + " reference targets");
        });
    }

    function up_to_prj() {
        send_ajax("../ua/prj_ajax.jsp", {op: "list_json"}, function (bsucc, ret) {
            if (!bsucc || ret.indexOf("[") != 0) {
                dlg.msg(ret);
                return;
            }
            eval("ref_list=" + ret);
            dlg.open("../util/dlg_sel_in_list.jsp?opener_list_id=ref_list",
                {title: "Select Project", w: '500px', h: '400px'},
                ['Ok', 'Cancel'],
                [
                    function (dlgw) {
                        var ids = dlgw.get_select();
                        up_to_refs(ids, pre_prj)
                        dlg.close();
                    },
                    function (dlgw) {
                        dlg.close();
                    }
                ]);
        });

    }

    function up_to_devdef() {
        send_ajax("../ua/prj_ajax.jsp", {op: "list_json"}, function (bsucc, ret) {
            if (!bsucc || ret.indexOf("[") != 0) {
                dlg.msg(ret);
                return;
            }
            eval("prj_list=" + ret);
            dlg.open("../util/dlg_sel_in_list.jsp?opener_list_id=prj_list",
                {title: "Select Project", w: '500px', h: '400px'},
                ['Ok', 'Cancel'],
                [
                    function (dlgw) {
                        var ret = dlgw.get_select();
                        up_to_prjs(ret);
                        dlg.close();
                    },
                    function (dlgw) {
                        dlg.close();
                    }
                ]);
        });

    }
</script>
</body>
</html>