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
                 cn.doraro.flexedge.core.msgnet.nodes.*,
                 cn.doraro.flexedge.core.station.*" %>
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
    IMNContainer mnc = net.getBelongTo().getBelongTo();
    if (mnc == null || !(mnc instanceof UAPrj)) {
        out.print("no in prj");
        return;
    }
    UAPrj prj = (UAPrj) mnc;
    String prj_path = "/" + prj.getName();
    MNBase item = net.getItemById(itemid);
    if (item == null || !(item instanceof NS_TagValChgTrigger)) {
        out.print("no item found");
        return;
    }

    NS_TagValChgTrigger stb_node = (NS_TagValChgTrigger) item;

    boolean ignore_invalid = stb_node.isIgnoreInvalid();
    List<String> tagpaths = stb_node.getTagPaths();
    JSONArray tagsubpaths_jarr = new JSONArray(tagpaths);
%>
<div class="layui-form-item">
    <div class="layui-form-label"><span style="white-space: nowrap;">Ignore Invalid:</span>
    </div>
    <div class="layui-input-inline" style="width: 75%;">
        <input type="checkbox" id="ignore_invalid" class="layui-input" lay-skin="primary"/>
    </div>
</div>
</div>

<div class="layui-form-item">
    <div class="layui-form-label"><span style="white-space: nowrap;">Project Tags:</span>
    </div>
    <div class="layui-input-inline" style="width: 75%;">
        <div id="tag_paths" onclick="sel_tags()" style="border:1px solid #ececec;width:100%;height:220px;overflow:auto">
        </div>
    </div>
</div>

<script>
    var prj_path = "<%=prj_path%>";
    var container_id = "<%=container_id%>";
    var netid = "<%=netid%>";

    var ignore_invalid = <%=ignore_invalid%>;
    var tag_paths = <%=tagsubpaths_jarr%>;

    function sel_tags() {
        dlg.open(`\${PM_URL_BASE}/../../ua_cxt/cxt_tag_selector.jsp?path=\${prj_path}&multi=true&bind_tag_only=true`,
            {title: "<w:g>select,tags</w:g>", w: '500px', h: '400px', sel_tagpaths: tag_paths},
            ['<w:g>ok</w:g>', '<w:g>cancel</w:g>'],
            [
                function (dlgw) {
                    let ret = dlgw.get_selected_tagpaths();
                    if (!ret || ret.length <= 0) {
                        dlg.msg("please select tags");
                        return;
                    }
                    tag_paths = ret;
                    update_ui();
                    dlg.close();
                },
                function (dlgw) {
                    dlg.close();
                }
            ]);
    }

    function update_ui() {
        let tmps = "";

        //if(store_tb.b_all_subt)
        //	tmps += `<span style="color:green">Using all sub tags</span>`;

        if (tag_paths) {
            for (let subt of tag_paths) {
                tmps += `<br>&nbsp;&nbsp;\${subt}`;
            }
        }
        $("#tag_paths").html(tmps);
    }

    function on_after_pm_show(form) {
        update_ui();
    }


    function get_pm_jo() {
        let ret = {tag_paths: tag_paths};
        ret.ignore_invalid = $("#ignore_invalid").prop("checked");
        return ret;
    }

    function set_pm_jo(jo) {
        tag_paths = jo.tag_paths;
        $("#ignore_invalid").prop("checked", jo.ignore_invalid || true);
    }

    function get_pm_size() {
        return {w: 600, h: 450};
    }

    //on_init_pm_ok() ;
</script>