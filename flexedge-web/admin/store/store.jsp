<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,
                 java.io.*,
                 cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.basic.*,
                 cn.doraro.flexedge.core.task.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.dict.*,
                 cn.doraro.flexedge.core.comp.*" %>
<%@ taglib uri="wb_tag" prefix="wbt" %>
<%!

%><%
    if (!Convert.checkReqEmpty(request, out, "prjid"))
        return;

    String prjid = request.getParameter("prjid");
    UAPrj prj = UAManager.getInstance().getPrjById(prjid);
    if (prj == null) {
        out.print("no prj found");
        return;
    }

%>
<html>
<head>
    <title></title>
    <jsp:include page="../head.jsp"></jsp:include>
</head>
<style>
    body {
        margin: 0px;
        padding: 0px;
        font-size: 12px;
        -moz-user-select: none;
        -webkit-user-select: none;
    }

    select option {
        font-size: 12px;
    }

    .oc-toolbar .toolbarbtn {
        width: 40px;
        height: 40px;
        margin: 5px;
        font-size: 13px;
        background-color: #eeeeee
    }

    .left {
        position: absolute;
        top: 0px;
        left: 0px;
        border: 0px solid;
        width: 20%;
        bottom: 0px;
    }

    .mid {
        position: absolute;
        top: 0px;
        left: 20%;
        border: 0px solid;
        right: 30%;
        bottom: 0px;
    }

    .right {
        position: absolute;
        top: 0px;
        right: 0px;
        border: 0px solid;
        width: 30%;
        bottom: 0px;
    }


    .list {
        position: absolute;
        top: 60px;
        bottom: 0px;
        width: 100%;
        border: 0px solid;
        border-color: red;
        scrollbar-width: none; /* firefox */
        -ms-overflow-style: none; /* IE 10+ */
        overflow-x: hidden;
        overflow-y: auto;
    }

    .list::-webkit-scrollbar {
        display: none; /* Chrome Safari */
    }

    .tag_item {
        position: relative;
        margin-bottom: 3px;
        height: 25px;
        font-size: 15px;
        margin-left: 10px;
        border: 1px solid;
        right: 0px;
    / / display: none;
    }

    .tag_item .tt {
        position: absolute;
        left: 20px;
        right: 20px;
        text-overflow: ellipsis;
        overflow: hidden;
        white-space: nowrap;
    }

    .tag_item .en {
        position: absolute;
        top: 3px;
        left: 2px;
    }

    .chk_tag_c {
        position: absolute;
        right: 0px;
        margin-top: 0px;
    / / visibility: hidden;
    }

    .h_item {
        position: absolute;
        left: 10%;
        width: 80%;
        height: 60px;
        border: 1px solid;
        margin-bottom: 10px;
    }

    .h_item .t {
        position: absolute;
        font-size: 18px;
        top: 2px;
        left: 16px;
    }

    .h_item .rt {
        position: absolute;
        font-size: 15px;
        top: 2px;
        right: 76px;
    }


    .h_item .f {
        position: absolute;
        font-size: 15px;
        left: 3px;
        color: #00988b;
        cursor: pointer;
        bottom: 7px;
    }

    .h_item .s {
        position: absolute;
        font-size: 15px;
        left: 180px;
        color: #00988b;
        cursor: pointer;
        bottom: 7px;
    }

    .h_item .save_btn {
        position: absolute;
        font-size: 15px;
        right: 30px;
        color: #00988b;
        cursor: pointer;
        visibility: hidden;
        top: 0px;
    }

    .h_item .trigger_c {
        position: absolute;
        font-size: 20px;
        border: 1px solid;
        top: 5px;
        right: 170px;
    }

    .h_item .release_c {
        position: absolute;
        font-size: 20px;
        top: 5px;
        border: 1px solid;
        right: 80px;

    }

    .h_item .oper {
        position: absolute;
        font-size: 15px;
        right: 10px;
        bottom: 7px;
    }


    .out_item {
        position: relative;
        left: 0%;
        width: 90%;
        min-height: 50px;
        border: 1px solid;
        display: flex;
        margin-bottom: 10px;
    }

    .out_item .n {
        position: absolute;
        font-size: 18px;
        left: 5px;
    }

    .out_item .t {
        font-size: 20px;
        top: 18px;
        margin-left: 0px;
        left: 0px;
        border: 0px solid;
        word-break: break-all;
        margin-bottom: 30px;
    }

    .out_item .rt {
        position: absolute;
        font-size: 18px;
        top: 3px;
        right: 10px;
        visibility: hidden;
    }

    .out_item .tp {
        position: absolute;
        font-size: 25px;
        bottom: 10px;
        left: 10px;
    }

    .out_item .tpt {
        position: absolute;
        font-size: 13px;
        bottom: 1px;
        left: 5px;
    }

    .out_item .oper {
        position: absolute;
        font-size: 15px;
        right: 10px;
        bottom: 5px;
    }

    .out_item_add {
        position: relative;
        left: 0%;
        width: 35px;
        height: 35px;
        font-size: 25px;
        border: 1px solid;
        display: flex;
        margin-bottom: 20px;
        justify-content: center;
        align-items: center;
    }

    .sel {
        border: 2px solid;
        border-color: blue;
    }

    .enable_c {
        font-size: 15px;
    }

    .in_conn_panel {
        position: absolute;
        left: 0px;
        width: 10%;
        top: 0px;
        bottom: 0px;
        border: 0px solid;
        border-color: red;

    }

    .out_conn_panel {
        position: absolute;
        right: 0px;
        width: 10%;
        top: 0px;
        bottom: 0px;
        border: 0px solid;
        border-color: red;
    }
</style>
<body marginwidth="0" marginheight="0">
<div class="left">
    <blockquote class="layui-elem-quote "><wbt:g>tags,list</wbt:g></blockquote>
    <div class="list" onscroll="on_list_scroll()">
        <%
            List<UATag> tags = prj.listTagsAll();
            int tags_num = tags.size();
            for (UATag tag : tags) {
                String tagid = tag.getId();
                String np = tag.getNodeCxtPathIn(prj);
                String tt = "[" + tag.getValTp() + "]  " + tag.getNodePathName() + "&#13;" + tag.getNodePathTitle();
                String en_c = true ? "green" : "gray";
                String en_t = true ? "Enabled" : "Disabled";
        %>
        <div class="tag_item" title="<%=tt%>" tag_id="<%=tagid%>" tag_np="<%=np %>" id="tag_<%=tagid%>"><i
                class="fa fa-square en" style="color:<%=en_c%>" title="<%=en_t%>"></i>
            <span class="tt"><%=np %></span><span class="chk_tag_c"><input type="checkbox" tag_np="<%=np %>"
                                                                           class="chk_tag"
                                                                           onclick="on_chk_tag(this,'<%=np%>')"/></span>
        </div>
        <%
            }
        %>
        <div style="height:60%;"></div>
    </div>
</div>
<div class="mid" onclick="on_handler_clk()">
    <blockquote class="layui-elem-quote "><wbt:g>handlers</wbt:g>
        <div style="position: absolute;right:10px;top:11px;width:100px;border:0px solid;height:35px;">
            <button type="button" style="top:3px;" class="layui-btn layui-btn-sm "
                    onclick="add_or_edit_h('<%=prjid %>','rt',null)">+<wbt:g>add</wbt:g></button>
        </div>

    </blockquote>
    <div id="handler_list" class="list" onscroll="on_list_scroll()">

    </div>
    <div style="height:20%;"></div>
    <div id="in_conn_panel" class="in_conn_panel"></div>
    <div id="out_conn_panel" class="out_conn_panel"></div>
</div>

<div class="right">

    <blockquote class="layui-elem-quote "><wbt:g>outputs</wbt:g>
        <div style="float: right;margin-right:10px;font: 15px solid;color:#fff5e2">

        </div>
    </blockquote>

    <div id="out_list" class="list" onscroll="on_list_scroll()">
    </div>
    <div style="height:20%;"></div>
</div>

<script>
    var prjid = "<%=prjid%>";
    var tags_num =<%=tags_num%>;

    var handlers = null;

    var cur_handler = null;
    var cur_h = null;

    function get_h_by_id(id) {
        if (!handlers)
            return null;
        for (let h of handlers) {
            if (id == h.id)
                return h;
        }
        return null;
    }

    function chk_cur_h_filter_fit(tag_path) {
        if (!cur_h) return false;
        if (cur_h.filter_all == true) return true;
        if (!cur_h.filter_prefixs) return false;

        for (let fp of cur_h.filter_prefixs) {
            if (tag_path.indexOf(fp) == 0)
                return true;
        }
        return false;
    }

    function on_handler_clk(item) {
        event.stopPropagation();
        cur_handler = item;
        if (cur_handler == null) {
            cur_h = null;
        } else {
            let id = $(item).attr("hid");
            cur_h = get_h_by_id(id);
            if (!cur_h) {
                dlg.msg("no handler with id found [" + id + "]");
                return;
            }
        }

        $(".h_item").removeClass("sel");
        if (item)
            $(item).addClass("sel");

        update_tags();
        redraw_conn();
    }

    function update_tags() {
        if (!cur_h) {
            $(".tag_item").css("display", "none");
            return;
        }
        //console.log(cur_h) ;

        $(".tag_item").each(function () {
            let me = $(this);
            let np = me.attr("tag_np");
            if (!chk_cur_h_filter_fit(np)) {
                me.css("display", "none");
                return;
            }

            me.css("display", "");
            let inp = me.find("input");
            if (cur_h.sel_all) {
                let inp = me.find("input");
                inp.prop("checked", true);
                inp.prop("disabled", true);
                return;
            }

            inp.prop("disabled", false);
            inp.prop("checked", cur_h.sel_tags.indexOf(np) >= 0);
        });
    }

    update_tags();

    function set_dirty(hid, b) {
        let id = hid;
        if (b) {
            $("#btn_save_" + id).removeClass("layui-btn-primary");
            $("#btn_save_" + id).addClass("layui-btn-warm");
            $("#btn_save_" + id).css("visibility", "visible");
        } else {
            $("#btn_save_" + id).removeClass("layui-btn-warm");
            $("#btn_save_" + id).addClass("layui-btn-primary");
            $("#btn_save_" + id).css("visibility", "hidden");
        }

        redraw_conn();
    }

    function on_chk_tag(inp_ele, tag_np) {
        if (!cur_h)
            return;

        let chked = $(inp_ele).prop("checked");

        if (chked) {
            if (cur_h.sel_tags.indexOf(tag_np) < 0)
                cur_h.sel_tags.push(tag_np);
        } else {
            let k = cur_h.sel_tags.indexOf(tag_np);
            if (k >= 0) {
                cur_h.sel_tags.splice(k, 1);
            }
        }

        set_dirty(cur_h.id, true)
    }


    function update_outs() {
        let tmps = "";
        for (let h of handlers) {
            for (let ob of h.outs) {
                //console.log(ob) ;
                let en_c = ob.en ? "green" : "gray";
                let en_t = ob.en ? "<wbt:g>enabled</wbt:g>" : "<wbt:g>disabled</wbt:g>";
                tmps += `<div id="out_\${ob.id}" class="out_item" tp="\${ob.tp}" hid="\${h.id}" t="\${ob.t}" title="\${ob.t}" >
				<span class="enable_c"><i class="fa fa-square en chk_out" id="\${ob.id}" hid="\${h.id}"  style="color:\${en_c}" title="\${en_t}"></i></span>
				<span class="t">\${ob.n}-[\${ob.table}]</span>
				<span class="rt" id="o_rt_\${ob.id}"><span id="o_init_\${ob.id}" ><i class="fa-solid fa-circle"></i></span> <span id="o_run_\${ob.id}" ><i class="fa-solid fa-circle"></i></span> <span id="o_err_\${ob.id}"  style="visibility:hidden" ><i class="fa-solid fa-triangle-exclamation"></i></span></span>
				<span class="tpt">\${ob.tpt}</span>
				<span class="oper">
					<button title="init or create out" type="button" class="layui-btn layui-btn-xs layui-btn-normal" onclick="init_o('\${prjid}','\${h.id}','\${ob.id}')"><i class="fa-solid fa-rotate-right"></i></button>
					<button type="button" class="layui-btn layui-btn-xs layui-btn-normal" onclick="add_or_edit_o('\${prjid}','\${ob.tp}','\${h.id}','\${ob.id}')" title="edit out"><i class="fa fa-pencil"></i></button>
					<button type="button" class="layui-btn layui-btn-xs layui-btn-normal" onclick="view_o('\${prjid}','\${ob.tp}','\${h.id}','\${ob.id}')" title="view data"><i class="fa fa-list-alt"></i></button>
					<button type="button" class="layui-btn layui-btn-xs layui-btn-danger" onclick="del_out('\${prjid}','\${h.id}','\${ob.id}')" title="delete"><i class="fa-regular fa-rectangle-xmark"></i></button>
				</span>
				</div>`;
            }

            tmps += `<div id="out_add_\${h.id}" hid="\${h.id}" class="out_item_add chk_out" h_tp="\${h.tp}" title="<wbt:g>add,output</wbt:g>" onclick="add_o('\${prjid}','\${h.id}')" ><i class="fa fa-plus"></i></div>`;
        }
        $("#out_list").html(tmps);
    }

    function calOutY(hid) {
        let min_y = -1;
        let max_y = -1;
        $(".chk_out").each(function () {
            let me = $(this);

            if (hid != me.attr("hid"))
                return;
            let y = me.offset().top + me.height() / 2;
            if (min_y < 0) {
                min_y = max_y = y;
            } else {
                max_y = y;
            }
        });
        return (min_y + max_y) / 2 - 71;
    }

    function update_handlers() {
        send_ajax("store_ajax.jsp", {op: "list_h", prjid: prjid}, (bsucc, ret) => {
            if (!bsucc || ret.indexOf("[") != 0) {
                dlg.msg(ret);
                return;
            }
            let os = null;
            eval("os=" + ret);
            handlers = os;

            update_outs();

            let tmps = "";
            for (let ob of os) {
                let trigger_c = ob.trigger_c ? 'background-color:' + ob.trigger_c : '';
                let trigger_dis = ob.trigger_en ? '' : 'display:none';
                let release_c = ob.release_c ? 'background-color:' + ob.release_c : '';
                let release_dis = ob.release_en ? '' : 'display:none';
                let y = calOutY(ob.id);
                //console.log(ob) ;
                let f_tt = "";
                if (ob.filter_all)
                    f_tt = "<wbt:g>user_all_tags</wbt:g> [" + tags_num + "]";
                else
                    f_tt = "<wbt:g>filter_by_prefix</wbt:g> [" + "]";
                let s_tt = "";
                if (ob.sel_all)
                    s_tt = "<wbt:g>sel_all</wbt:g>";
                else
                    s_tt = "<wbt:g>chk_the_box</wbt:g>";
                let en_c = ob.en ? "green" : "gray";
                let en_t = ob.en ? "<wbt:g>enabled</wbt:g>" : "<wbt:g>disabled</wbt:g>";
                tmps += `<div id="h_\${ob.id}" style="top:\${y}px" class="h_item" hid="\${ob.id}" n="\${ob.n}" t="\${ob.t}" tp="\${ob.tp}" onclick="on_handler_clk(this)" alert_uids="\${ob.alert_uids}">
				<span class="t">\${ob.t} [\${ob.n}]</span>
				<span class="enable_c"><i class="fa fa-square en" style="color:\${en_c}" title="\${en_t}"></i></span>
				<span class="f" ><i class="fa fa-filter" style="font-size:18px;"></i>\${f_tt}</span>
				<span class="rt" ><i id='h_run_\${ob.id}' class='fa fa-circle-notch fa-lg'></i></span>
				<span class="s" ><i class="fa-regular fa-square-check" style="font-size:18px;"></i>\${s_tt}</span>
				<span class="trigger_c" style="\${trigger_c};\${trigger_dis}">Trigger [\${ob.lvl}]&nbsp;</span>
				<span class="release_c" style="\${release_c};\${release_dis}">Release</span>
				<button id="btn_save_\${ob.id}" class="save_btn" type="button" class="layui-btn layui-btn-sm layui-border-blue layui-btn-primary" onclick="save_h_inout_ids('\${prjid}','\${ob.id}')"><wbt:g>save</wbt:g></button>
				<span class="oper">
					<button type="button" class="layui-btn layui-btn-xs layui-btn-normal" onclick="add_or_edit_h('\${prjid}','\${ob.tp}','\${ob.id}')"><i class="fa fa-pencil"></i></button>
					<button type="button" class="layui-btn layui-btn-xs layui-btn-danger" onclick="del_handler('\${prjid}','\${ob.id}')" title="delete"><i class="fa-regular fa-rectangle-xmark"></i></button>
				</span>

				</div>`;
            }
            $("#handler_list").html(tmps);

            redraw_conn();
        });
    }

    update_handlers();

    function save_h_inout_ids(prjid, hid) {
        let h = get_h_by_id(hid);
        let idstr = "";
        if (h.sel_tags)
            idstr = h.sel_tags.join();

        send_ajax("store_ajax.jsp", {op: "set_h_sel_tagids", prjid: prjid, hid: hid, idstr: idstr}, (bsucc, ret) => {
            dlg.msg(ret);
            set_dirty(hid, false);
        });
    }

    function del_handler(prjid, id) {
        event.stopPropagation();
        let ob = $("#h_" + id);
        dlg.confirm('<wbt:g>del,this,handler</wbt:g> [' + ob.attr("n") + ']?', {
            btn: ["<wbt:g>yes</wbt:g>", "<wbt:g>cancel</wbt:g>"],
            title: "<wbt:g>del,confirm</wbt:g>"
        }, function () {
            send_ajax("store_ajax.jsp", {op: "del_h", prjid: prjid, id: id}, function (bsucc, ret) {
                if (!bsucc || ret != 'succ') {
                    dlg.msg("<wbt:g>del,err</wbt:g>:" + ret);
                    return;
                }
                //
                update_handlers();
            });
        });
    }

    function add_or_edit_h(prjid, tp, id) {
        event.stopPropagation();
        var tt = "<wbt:g>add,handler</wbt:g>";
        if (id) {
            tt = "<wbt:g>edit,handler</wbt:g>";
        }
        if (id == null)
            id = "";
        dlg.open("h_" + tp + "_edit.jsp?prjid=" + prjid + "&id=" + id,
            {title: tt},
            ['<wbt:g>ok</wbt:g>', '<wbt:g>cancel</wbt:g>'],
            [
                function (dlgw) {
                    dlgw.do_submit((bsucc, ret) => {
                        if (!bsucc) {
                            dlg.msg(ret);
                            return;
                        }

                        let op = "set_h";
                        ret.prjid = prjid;
                        ret.id = id;
                        var pm = {
                            type: 'post',
                            url: "./store_ajax.jsp",
                            data: {op: op, prjid: prjid, jstr: JSON.stringify(ret)}
                        };
                        $.ajax(pm).done((ret) => {
                            if ("succ" != ret) {
                                dlg.msg(ret);
                                return;
                            }

                            dlg.close();
                            update_handlers();
                        }).fail(function (req, st, err) {
                            dlg.msg(err);
                        });
                    });
                },
                function (dlgw) {
                    dlg.close();
                }
            ]);
    }

    function del_out(prjid, hid, id) {
        event.stopPropagation();
        dlg.confirm('<wbt:g>delete,this,output</wbt:g>?', {
            btn: ["<wbt:g>yes</wbt:g>", "<wbt:g>cancel</wbt:g>"],
            title: "<wbt:g>del,confirm</wbt:g>"
        }, function () {
            send_ajax("store_ajax.jsp", {op: "del_o", prjid: prjid, hid: hid, id: id}, function (bsucc, ret) {
                if (!bsucc || ret != 'succ') {
                    dlg.msg("<wbt:g>del,err</wbt:g>:" + ret);
                    return;
                }
                //
                update_handlers();
            });
        });
    }


    function on_clk_filter(hid, filter_str) {
        if (!filter_str) {
            dlg.open("./filter_sel.jsp",
                {title: "<wbt:g>select,filter,type</wbt:g>"},
                ['<wbt:g>cancel</wbt:g>'],
                [
                    function (dlgw) {
                        dlg.close();
                    }
                ], (ret) => {
                    if (!ret)
                        return;
                    add_or_edit_filter(hid, ret.tp, null);
                });
            return;
        }

        let k = filter_str.indexOf("-");
        let tp = filter_str.substring(0, k);
        let sub_str = filter.substring(k + 1);
        add_or_edit_filter(hid, tp, sub_str)
    }

    function add_o(prjid, hid) {
        dlg.open("./o_sel.jsp?prjid=" + prjid + "&hid=" + hid,
            {title: "<wbt:g>select,sor,type</wbt:g>"},
            ['<wbt:g>cancel</wbt:g>'],
            [
                function (dlgw) {
                    dlg.close();
                }
            ], (ret) => {
                if (!ret)
                    return;
                add_or_edit_o(prjid, ret.tp, hid, null);
            });
    }

    function add_or_edit_o(prjid, tp, hid, id) {
        if (event)
            event.stopPropagation();
        var tt = "<wbt:g>add,handler,output</wbt:g> [" + tp + "]";
        if (id) {
            tt = "<wbt:g>edit,handler,output</wbt:g> [" + tp + "]";
        }
        if (id == null)
            id = "";
        dlg.open("o_" + tp + "_edit.jsp?prjid=" + prjid + "&hid=" + hid + "&id=" + id,
            {title: tt},
            ['<wbt:g>ok</wbt:g>', '<wbt:g>cancel</wbt:g>'],
            [
                function (dlgw) {
                    dlgw.do_submit((bsucc, ret) => {
                        if (!bsucc) {
                            dlg.msg(ret);
                            return;
                        }

                        let op = "set_o";
                        ret.prjid = prjid;
                        ret.hid = hid;
                        ret.id = id;
                        var pm = {
                            type: 'post',
                            url: "./store_ajax.jsp",
                            data: {op: op, prjid: prjid, jstr: JSON.stringify(ret)}
                        };
                        $.ajax(pm).done((ret) => {
                            if ("succ" != ret) {
                                dlg.msg(ret);
                                return;
                            }

                            dlg.close();
                            update_handlers();
                        }).fail(function (req, st, err) {
                            dlg.msg(err);
                        });
                    });
                },
                function (dlgw) {
                    dlg.close();
                }
            ]);
    }

    function view_o(prjid, tp, hid, id) {
        if (event)
            event.stopPropagation();
        var tt = "<wbt:g>view,output,data</wbt:g> [" + tp + "]";
        dlg.open("o_" + tp + "_view.jsp?prjid=" + prjid + "&hid=" + hid + "&id=" + id,
            {title: tt},
            ['<wbt:g>close</wbt:g>'],
            [
                function (dlgw) {
                    dlg.close();
                }
            ]);
    }

    function init_o(prjid, hid, id) {
        dlg.loading(true);
        send_ajax("store_ajax.jsp", {op: "init_o", prjid: prjid, hid: hid, id: id}, (bsucc, ret) => {
            dlg.loading(false);
            dlg.msg(ret);
        });
    }


    var in_panel = null;
    var in_can = null;
    var in_cxt = null;
    var in_w = null;

    var out_panel = null;
    var out_can = null;
    var out_cxt = null;
    var out_w = null;

    function conn_init_in() {
        in_cxt = document.createElement('canvas').getContext('2d');
        let can = $(in_cxt.canvas);
        in_can = can;
        can.css("position", "relative");
        can.css("left", "0px");
        can.css("top", "0px");
        can.css("display", "");
        in_panel = $("#in_conn_panel");
        can.attr('width', in_panel[0].offsetWidth);
        can.attr('height', in_panel[0].offsetHeight - 5);
        in_panel.append(can);
        in_w = in_panel[0].offsetWidth;
        in_panel.resize(() => {
            in_w = in_panel[0].offsetWidth;
            var h = in_panel[0].offsetHeight;
            //console.log(w,h)
            can.attr('width', in_w);
            can.attr('height', h);
            redraw_conn();
        });
    }

    function conn_init_out() {
        out_cxt = document.createElement('canvas').getContext('2d');
        let can = $(out_cxt.canvas);
        out_can = can;
        can.css("position", "relative");
        can.css("left", "0px");
        can.css("top", "0px");
        can.css("display", "");
        out_panel = $("#out_conn_panel");
        can.attr('width', out_panel[0].offsetWidth);
        can.attr('height', out_panel[0].offsetHeight - 5);
        out_panel.append(can);
        out_w = out_panel[0].offsetWidth;
        out_panel.resize(() => {
            in_w = out_panel[0].offsetWidth;
            var h = out_panel[0].offsetHeight;
            //console.log(w,h)
            can.attr('width', in_w);
            can.attr('height', h);
            redraw_conn();
        });
    }

    function conn_init() {
        conn_init_in();
        conn_init_out();
    }


    function redraw_conn() {
        in_can[0].width = in_can[0].width;
        out_can[0].width = out_can[0].width;
        $(".h_item").each(function () {
            redraw_conns_h(this);
        });
        //if(cur_h)
        //	redraw_conns_h(cur_handler) ;
    }

    function redraw_conns_h(handler) {
        let handler_id = $(handler).attr("hid");
        let h_y = $(handler).offset().top + $(handler).height() / 2;

        in_cxt.save();
        out_cxt.save();
        //in_cxt.
        if (handler == cur_handler) {
            in_cxt.lineWidth = 1;
            in_cxt.strokeStyle = "blue";
            out_cxt.lineWidth = 2;
            out_cxt.strokeStyle = "blue";

            $(".chk_tag").each(function () {

                if (!cur_h) return;
                let me = $(this);

                if (me.is(':hidden'))
                    return;
                let np = me.attr("tag_np");
                let chked = cur_h.sel_all || cur_h.sel_tags.indexOf(np) >= 0
                if (!chked) return;

                let y = me.offset().top + me.height() / 2;
                //if(y==0) return ;
                //console.log(y) ;
                in_cxt.beginPath();
                in_cxt.moveTo(0, y);
                in_cxt.lineTo(in_w / 2, y);
                in_cxt.lineTo(in_w / 2, h_y);
                in_cxt.lineTo(in_w, h_y);
                in_cxt.stroke();
            });

            in_cxt.lineWidth = 2;
            in_cxt.strokeStyle = "blue";
            out_cxt.lineWidth = 2;
            out_cxt.strokeStyle = "blue";
        }


        $(".chk_out").each(function () {
            let me = $(this);
            let hid = me.attr("hid");
            //let id = me.attr("id") ;
            if (hid != handler_id)
                return;
            let y = me.offset().top + me.height() / 2;
            out_cxt.beginPath();
            out_cxt.moveTo(out_w, y);
            out_cxt.lineTo(0, h_y);
            out_cxt.stroke();
        });

        in_cxt.restore();
        out_cxt.restore();
    }

    conn_init();

    function on_list_scroll() {
        redraw_conn()
    }


    function rt_update() {
        send_ajax("store_ajax.jsp", {op: "rt_data", prjid: prjid}, (bsucc, ret) => {
            if (!bsucc && ret.indexOf("{") != 0) {
                //show err
                return;
            }
            let ob = null;
            eval("ob=" + ret);
            for (let h of ob.handlers) {
                let c = "grey";
                if (h.en)
                    c = h.run ? "green" : "red";

                if (h.run)
                    $("#h_run_" + h.id).addClass("fa-spin").css("color", c);
                else
                    $("#h_run_" + h.id).removeClass("fa-spin").css("color", c);

                for (let o of h.outs) {
                    let init_ok = o.init_ok;
                    let init_c = "grey";
                    let init_t = "";
                    if (h.run && h.en) {
                        init_c = init_ok ? "green" : "red";
                        init_t = init_ok ? "<wbt:g>init,ok</wbt:g>" : "<wbt:g>init,failed</wbt:g>";
                    }

                    let run_ok = o.run_ok;
                    let run_c = "grey";
                    let run_t = "";
                    if (!h.run) {
                        $("#o_rt_" + o.id).css("visibility", "hidden");
                        return;
                    }

                    $("#o_rt_" + o.id).css("visibility", "visible");
                    if (h.run && h.en) {
                        run_c = run_ok ? "green" : "red";
                        run_t = run_ok ? "<wbt:g>run_ok</wbt:g>" : "<wbt:g>run_err</wbt:g>";
                    }

                    $("#o_init_" + o.id).css("color", init_c).attr("title", init_t);
                    $("#o_run_" + o.id).css("color", init_c).attr("title", run_t);
                    if (h.run && h.en && o.err)
                        $("#o_err_" + o.id).css("color", "red").css("visibility", "visible").attr("title", o.err);
                    else
                        $("#o_err_" + o.id).css("visibility", "hidden");
                }
            }

        });
    }

    setInterval(rt_update, 7000);

</script>

</body>
</html>