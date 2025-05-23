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
                 cn.doraro.flexedge.core.msgnet.util.*" %>
<%@ taglib uri="wb_tag" prefix="w" %>
<%

%>
<style>
    .rule {
        position: relative;
        width: 98%;
        left: 1%;
        border: 1px solid;
        border-color: #dddddd;
        margin-top: 5px;
        min-height: 50px;
    }

    .rule .del {
        position: absolute;
        right: 2px;
        top: 2px;
        z-index: 10;
        width: 20px;
        height: 20px;
        color: #dddddd;
        border-color: #dddddd;
    }

    .rule .del:hover {
        background-color: red;

    }

    .row {
        position: relative;
        width: 100%;
        height: 55px;
    }

    .row .msg {
        position: absolute;
        left: 30px;
        top: 10px;
        width: 40px;
        height: 36px;
        border: 0px solid #dddddd;
        vertical-align: middle;
    }

    .act {
        position: absolute;
        left: 50px;
        top: 10px;
    }

    .row .mid {
        position: absolute;
        left: 207px;
        top: 20px;
    }

    .row .tar_pktp {
        position: absolute;
        left: 150px;
        top: 10px;
        width: 100px
    }

    .row .tar_subn {
        position: absolute;
        left: 230px;
        top: 10px;
        width: 260px;
    }

    .outidx {
        position: absolute;
        right: 30px;
        top: 20px;
        width: 60px;
    }

    .row .tar_pktp .layui-edge {
        right: 80px;
    }

    .row .tar_pktp .layui-input {
        padding-left: 20px;
        padding-right: 20px;
        text-align: right;
        border-right: 0px;
    }
</style>

<div class="row">
    <div class="msg"><span style="top:10px;position: absolute;">&nbsp;Property</span></div>
    <div class="tar_pktp" style="">
        <select id="prop_pktp" class="layui-input" lay-filter="sor_valsty" style="width:100px;border-right: 0px;">
            <%
                for (MNCxtPkTP pktp : MNCxtPkTP.values()) {
            %>
            <option value="<%=pktp.name()%>"><%=pktp.getTitle() %>.</option>
            <%
                }
            %>
        </select>
    </div>
    <div class="tar_subn">
        <input type="text" id="prop_subn" class="layui-input" style="border-left: 0px;left:2px;"/>
    </div>
</div>

<div id="rules">
</div>
<div class="rule" id="otherwise_cc">
    <div class="act" style="width:250px;top:15px;">
        <input type="checkbox" class="layui-input" lay-skin="primary" id="otherwise"/> <w:g>otherwise</w:g>
    </div>
    <div id="outidx" class="outidx">
    </div>
</div>


<div class="rule" id="rule_temp" style="display:none">
    <button class="del" onclick="del_rule(this)">X</button>

    <div class="row">
        <div class="msg"></div>
        <div class="act" style="width:150px;">
            <select id="op" class="layui-input" lay-filter="op" style="width:100px;border-right: 0px;">
                <%
                    for (ValOper vo : ValOper.ALL) {
                        boolean b_pm2 = vo.isNeedPm2();
                        boolean b_pm3 = vo.isNeedPm3();
                %>
                <option value="<%=vo.getName()%>" pm2="<%=b_pm2%>" pm3="<%=b_pm3%>"><%=vo.getTitle() %>
                </option>
                <%
                    }
                %>
            </select>
        </div>
        <div class="mid"></div>
        <div id="pm2">
            <div class="tar_pktp">
                <select id="pm2_valsty" class="layui-input" lay-filter="pm2_valsty"
                        style="width:100px;border-right: 0px;">
                    <%
                        for (MNCxtValSty pktp : MNCxtValSty.values()) {
                    %>
                    <option value="<%=pktp.name()%>"><%=pktp.getTitle() %><%=pktp.isCxtPk() ? "." : "" %>
                    </option>
                    <%
                        }
                    %>
                </select>
            </div>
            <div class="tar_subn">
                <input type="text" id="pm2_subn" class="layui-input" style="border-left: 0px;left:2px;"/>
            </div>
        </div>
        <div id="outidx" class="outidx">

        </div>
    </div>

    <div class="row" id="pm3" style="display:none;margin-top:0px;">
        <div class="msg"></div>
        <div class="act" style="width:150px;">

        </div>

        <div class="tar_pktp" style="visibility: visible;">
            <select id="pm3_valsty" class="layui-input" lay-filter="pm2_valsty" style="width:100px;border-right: 0px;">
                <%
                    for (MNCxtValSty pktp : MNCxtValSty.values()) {
                %>
                <option value="<%=pktp.name()%>"><%=pktp.getTitle() %><%=pktp.isCxtPk() ? "." : "" %>
                </option>
                <%
                    }
                %>
            </select>
        </div>
        <div class="tar_subn">
            <input type="text" id="pm3_subn" class="layui-input" style="border-left: 0px;left:2px;"/>
        </div>

    </div>

</div>
<button onclick="add_rule()" style="border-color:#dddddd">+Add</button>
<script>


    function on_after_pm_show(form) {
        form.on("select(op)", function (obj) {
            //console.log(obj);
            let opv = $("#op").val();
            let opt = $("#op").find("option:selected");
            let b_pm2 = opt.attr("pm2") == 'true';
            let b_pm3 = opt.attr("pm3") == 'true';

            $("#pm2_subn").css("visibility", opv == 'is_type' ? "hidden" : "visible");
            $("#pm2").css("visibility", b_pm2 ? "visible" : "hidden");
            $("#pm3").css("display", b_pm3 ? "block" : "none");
        });
    }


    function add_rule(jo) {
        let html = $("#rule_temp")[0].outerHTML;
        let ele = $(html);
        ele.css("display", "");
        ele.attr("id", "rulex");
        $("#rules").append(ele);

        ele.find("#op").val("eq");
        ele.find("#pm2_subn").val("");
        ele.find("#pm2_valsty").val("vt_str");

        if (jo) {
            ele.find("#op").val(jo.op || "");
            if (jo.pm2_valsty)
                ele.find("#pm2_valsty").val(jo.pm2_valsty);
            ele.find("#pm2_subn").val(jo.pm2_subn || "");
            if (jo.pm3_valsty)
                ele.find("#pm3_valsty").val(jo.pm3_valsty);
            ele.find("#pm3_subn").val(jo.pm3_subn || "");
        }

        form.render();

        update_rules();
        return ele;
    }

    function del_rule(ele) {
        //console.log(ele) ;
        $(ele).parent().remove();
        update_rules();
    }

    function update_rules() {
        let cc = 0;

        $(".outidx").each(function () {
            cc++;
            $(this).html(" → " + cc);
        });
        /*
        $("#rules").find("#rule").each(function(){
            cc ++ ;
            let oidx = $(this).find("#outidx") ;
            oidx.html(" → "+cc) ;
        })
        */
        ;
    }

    function extract_rule_jo(ele) {
        let ret = {};
        ret.op = ele.find("#op").val() || "";
        ret.pm2_valsty = ele.find("#pm2_valsty").val();
        ret.pm2_subn = ele.find("#pm2_subn").val() || "";
        ret.pm3_valsty = ele.find("#pm3_valsty").val();
        ret.pm3_subn = ele.find("#pm3_subn").val() || "";
        return ret;
    }


    function get_chk_vals(name) {
        let ret = [];
        $("input[name=" + name + "]").each(function () {
            let ob = $(this);
            if (ob.prop("checked"))
                ret.push(ob.val());
        });
        return ret;
    }

    function get_pm_jo() {
        let jo = {};
        jo.prop_subn = $("#prop_subn").val();
        jo.prop_pktp = $("#prop_pktp").val();

        let rule_jos = [];
        jo.rules = rule_jos;
        $("#rules").find(".rule").each(function () {
            let ruleele = $(this);
            let tmpjo = extract_rule_jo(ruleele);
            rule_jos.push(tmpjo);
        });
        jo.otherwise = $("#otherwise").prop("checked");
        //console.log(jo) ;
        return jo;
    }

    function set_pm_jo(jo) {
        if (!jo || !jo.rules || jo.rules.length <= 0) {
            $("#prop_subn").val("payload");
            $("#prop_pktp").val("msg");

            add_rule(null);
            return;
        }

        $("#prop_subn").val(jo.prop_subn || "payload");
        $("#prop_pktp").val(jo.prop_pktp || "msg");

        //console.log("set pm",jo) ;
        for (let rule of jo.rules) {
            let ele = add_rule(rule);
            //update_rule(ele) ;
        }
        $("#otherwise").prop("checked", jo.otherwise || false);
        update_rules();
    }

    function get_pm_size() {
        return {w: 600, h: 550};
    }

    //on_init_pm_ok() ;
</script>