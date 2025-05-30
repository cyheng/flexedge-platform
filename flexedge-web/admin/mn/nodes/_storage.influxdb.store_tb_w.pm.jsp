<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,
                 java.io.*,
                 org.json.*,
                 cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.basic.*,
                 cn.doraro.flexedge.core.task.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.dict.*,
                 cn.doraro.flexedge.core.comp.*,
                 cn.doraro.flexedge.core.msgnet.*,
                 cn.doraro.flexedge.core.msgnet.util.*,
                 cn.doraro.flexedge.core.msgnet.store.*" %>
<%@ taglib uri="wb_tag" prefix="w" %>
<%

    String container_id = request.getParameter("container_id");
    String netid = request.getParameter("netid");
    String itemid = request.getParameter("itemid");
    MNManager mnm = MNManager.getInstanceByContainerId(container_id);
    MNNet net = mnm.getNetById(netid);
    if (net == null) {
        out.print("no net found");
        return;
    }

    MNBase item = net.getItemById(itemid);
    if (item == null) {
        out.print("no item found");
        return;
    }

    StoreTbWriter_NE stb_node = (StoreTbWriter_NE) item;
    if (stb_node == null) {
        out.print("no node item found");
        return;
    }
    StoreTb stb = stb_node.getStoreTb();
    JSONObject stb_jo = null;
    if (stb != null)
        stb_jo = stb.toJO();
%>
<div class="layui-form-item">
    <label class="layui-form-label">Table Name:</label>
    <div class="layui-input-inline" style="width: 200px;">
        <input type="text" id=tablen name="tablen" value="" autocomplete="off" class="layui-input">
    </div>
    <label class="layui-form-mid">Title:</label>
    <div class="layui-input-inline" style="width: 200px;">
        <input type="text" id=tablet name="tablet" value="" autocomplete="off" class="layui-input">
    </div>
</div>

<div class="layui-form-item">
    <div class="layui-form-label"><span style="white-space: nowrap;">Project Tags:</span>
        <br><br> <input type="checkbox" id="b_all_subt" class="layui-input" lay-skin="primary"/><br>All Sub Tags
    </div>
    <div class="layui-input-inline" style="width: 75%;">
        <div id="station_prj_tags" onclick="sel_tags()"
             style="border:1px solid #ececec;width:100%;height:220px;overflow:auto">
        </div>
    </div>
</div>

<div class="layui-form-item">
    <label class="layui-form-label">Tag1</label>
    <div class="layui-form-mid"> Name:</div>
    <div class="layui-input-inline" style="width: 100px;">
        <input type="text" id="tag1_name" name="tag1_name" value="" autocomplete="off" class="layui-input">
    </div>
    <div class="layui-form-mid"> Value:</div>
    <div class="layui-input-inline" style="width: 100px;">
        <input type="text" id="tag1_val" name="tag1_val" value="" autocomplete="off" class="layui-input">
    </div>
</div>
<div class="layui-form-item">
    <label class="layui-form-label">Tag2</label>
    <div class="layui-form-mid"> Name:</div>
    <div class="layui-input-inline" style="width: 100px;">
        <input type="text" id="tag2_name" name="tag2_name" value="" autocomplete="off" class="layui-input">
    </div>
    <div class="layui-form-mid"> Value:</div>
    <div class="layui-input-inline" style="width: 100px;">
        <input type="text" id="tag2_val" name="tag2_val" value="" autocomplete="off" class="layui-input">
    </div>
</div>
<script>
    var container_id = "<%=container_id%>";
    var netid = "<%=netid%>";

    var store_tb = <%=stb_jo%>;

    function sel_tags() {
        dlg.open("/system/plat/plat_sel_tags_in_subp.jsp?dlg=true&w_only=" + false + "&multi=true&path=",
            {title: "<w:g>select,tags</w:g>", w: '500px', h: '400px', tags_in_subp: store_tb},
            ['<w:g>ok</w:g>', '<w:g>cancel</w:g>'],
            [
                function (dlgw) {
                    let ret = dlgw.get_selected((bok, ret) => {
                        if (!bok) {
                            dlg.msg(ret);
                            return;
                        }
                        if (store_tb)
                            ret.tablen = store_tb.tablen || "";
                        store_tb = ret;
                        update_ui();
                        dlg.close();
                    });
                },
                function (dlgw) {
                    dlg.close();
                }
            ]);
    }

    function update_ui() {
        let tmps = "";
        if (!store_tb) {
            $("#station_prj_tags").html(tmps);
            return;
        }
        tmps += `uid=\${store_tb.station_id||""}.\${store_tb.prj_name}<br>Parent Node Path=\${store_tb.cxt_nodep||""}<br>`;

        if (store_tb.b_all_subt)
            tmps += `<span style="color:green">Using all sub tags</span>`;
        else if (store_tb.tag_subts) {
            for (let subt of store_tb.tag_subts) {
                tmps += `<br>&nbsp;&nbsp;\${subt}`;
            }
        }
        $("#station_prj_tags").html(tmps);
    }

    function on_after_pm_show(form) {
        update_ui();
    }


    function get_pm_jo() {
        store_tb.tablen = $('#tablen').val();
        store_tb.tablet = $('#tablet').val();

        let ret = {tablen: tablen};
        let n = $("#tag1_name").val();
        let v = $("#tag1_val").val();
        if (n && v) {
            ret.tag1 = {n: n, v: v};
        }

        n = $("#tag2_name").val();
        v = $("#tag2_val").val();
        if (n && v) {
            ret.tag2 = {n: n, v: v};
        }

        n = $("#tag3_name").val();
        v = $("#tag3_val").val();
        if (n && v) {
            ret.tag3 = {n: n, v: v};
        }
        store_tb.b_all_subt = $("#b_all_subt").prop("checked");
        return store_tb;
    }

    function set_pm_jo(jo) {
        $('#tablen').val(jo.tablen || "");
        $('#tablet').val(jo.tablet || "");
        let tag = jo.tag1;
        if (tag) {
            $("#tag1_name").val(tag.n);
            $("#tag1_val").val(tag.v);
        }
        tag = jo.tag2;
        if (tag) {
            $("#tag2_name").val(tag.n);
            $("#tag2_val").val(tag.v);
        }
        tag = jo.tag3;
        if (tag) {
            $("#tag3_name").val(tag.n);
            $("#tag3_val").val(tag.v);
        }

        store_tb = jo;
        $("#b_all_subt").prop("checked", store_tb.b_all_subt);
    }

    function get_pm_size() {
        return {w: 600, h: 450};
    }

    //on_init_pm_ok() ;
</script>