<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,
                 java.io.*,
                 org.json.*,
                 cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.basic.*,
                 cn.doraro.flexedge.core.task.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.dict.*,
                 cn.doraro.flexedge.core.store.*,
                 cn.doraro.flexedge.core.alert.*,
                 cn.doraro.flexedge.core.comp.*" %>
<%@ taglib uri="wb_tag" prefix="lan" %>
<%!

%><%
    if (!Convert.checkReqEmpty(request, out, "prjid", "tag", "outid"))
        return;

    String prjid = request.getParameter("prjid");
    UAPrj prj = UAManager.getInstance().getPrjById(prjid);
    if (prj == null) {
        out.print("no prj found");
        return;
    }
    String tagpath = request.getParameter("tag");
    UATag tag = (UATag) UAManager.getInstance().findNodeByPath(tagpath);
    if (tag == null || tag.getBelongToPrj() != prj) {
        out.print("no tag found");
        return;
    }
    StoreManager storem = StoreManager.getInstance(prjid);

    List<StoreOut> storeos = storem.findStoreOutsByTag(tag, true, true);
    if (storeos == null || storeos.size() < 0) {
        out.print("no valid store out found");
        return;
    }
    String outid = request.getParameter("outid");
    StoreOut storeo = null;
    for (StoreOut so : storeos) {
        if (so.getId().equals(outid)) {
            storeo = so;
            break;
        }
    }
    if (storeo == null) {
        out.print("no store out matched");
        return;
    }
    String oid = storeo.getId();
    StoreHandler storeh = storeo.getBelongTo();
    String hid = storeh.getId();
%><!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title></title>
    <jsp:include page="./head.jsp"></jsp:include>
    <script type="text/javascript" src="/_js/echarts/echarts.min.js"></script>
    <style>
        .layui-form-label {
            width: 120px;
        }

        .layui-input-block {
            margin-left: 140px;
            min-height: 36px;
            width: 240px;
        }

        .layui-table {
            margin-top: 0px;
        }

        .layui-table-view {
            margin-top: 0px;
        }

        .layui-table-cell {
            height: auto;
            line-height: 18px;
        }

        .sel {
            background-color: rgba(00, 173, 229, 0.3);
        }

        .top {
            position: absolute;
            top: 00px;
            left: 0px;
            width: 100%;
            border: 1px solid #c1c1c1;
            height: 30px;
        }

        .main_left {
            position: absolute;
            top: 30px;
            left: 0px;
            width: 40%;
            height0: 100%;
            border: 1px solid #c1c1c1;
            bottom: 0px;
            overflow-y: scroll;
        }

        td, th {
            white-space: nowrap;
            text-overflow: ellipsis;
        }

        .main_right {
            position: absolute;
            top: 30px;
            right: 0px;
            left: 40%;
            height0: 100%;
            border: 1px solid #c1c1c1;
            bottom: 0px;
            overflow-y: scroll;
        }

    </style>
</head>
<script type="text/javascript">
    //dlg.resize_to(960,600) ;
</script>
<body>
<div class="top">

    <lan:g>start_dt</lan:g>
    <input type="datetime-local" id="start_dt" name="start_dt"/>
    <lan:g>end_dt</lan:g>
    <input type="datetime-local" id="end_dt" name="end_dt"/>
    <button onclick="do_search()"><lan:g>search</lan:g></button>
    <button onclick="do_search_all()"><lan:g>all</lan:g></button>
</div>
<div class="main_left" id="main_left">
    <table id="" class="layui-table" lay-filter="apiquote_list" lay-size="sm" lay-even0="true" style="width:100%">
        <colgroup>
            <col width="100">
            <col width="100">
            <col width="30">
            <col width="170">
            <col width="100">
            <col>
        </colgroup>
        <thead>
        <tr style="background-color: #cccccc">
            <th><lan:g>up_dt</lan:g></th>
            <th><lan:g>chg_dt</lan:g></th>
            <th><lan:g>valid</lan:g></th>
            <th><lan:g>val</lan:g></th>
            <th><lan:g>alert</lan:g></th>
        </tr>
        </thead>
        <tbody id="list_cont" class="list_cont">

        </tbody>
    </table>
</div>
<div class="main_right" id="main_right">
</div>
<script>

    var prjid = "<%=prjid%>";
    var tagpath = "<%=tagpath%>";
    var hid = "<%=hid%>";
    var oid = "<%=oid%>";

    var table;
    var table_cur_page = 1;

    var valid = "";
    var start_dt = "";
    var end_dt = "";
    layui.use('table', function () {
        table = layui.table;
    });


    function show_list(u, pm, bappend) {

        cur_list_u = u;
        pm.tag = tagpath || "";
        pm.valid = valid || "";
        pm.start_dt = start_dt || "";
        pm.end_dt = end_dt || "";
        send_ajax(u, pm, (bsucc, ret) => {
            if (!bsucc) {
                dlg.msg(ret);
                return;
            }
            if (bappend)
                $("#list_cont").append(ret);
            else
                $("#list_cont").html(ret);
        });
    }

    function update_list() {
        page_idx = 0;
        show_list("prj_data_r_tb_his_list.jsp", {prjid: prjid, hid: hid, oid: oid, pageidx: 0}, false);
    }


    function show_list_more() {
        page_idx++;
        show_list("prj_data_r_tb_his_list.jsp", {prjid: prjid, hid: hid, oid: oid, pageidx: page_idx}, true);
    }

    update_list();

    var chart = null;


    //var X_Data=[];
    var chart_data = [];

    function show_chart(data) {
        //console.log(xdata,ydata) ;
        //X_Data.push(...xdata);
        chart_data.push(...data);
        chart = echarts.init($("#main_right")[0]);

        var option = {

            tooltip: {
                trigger: 'axis'
            },

            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            toolbox: {
                feature: {
                    //saveAsImage: {}
                }
            },
            xAxis: {
                type: 'time',
                //boundaryGap: false,
                //data: X_Data //[1, 2, 3, 4, 5, 6, 7] //
            },
            yAxis: {
                type: 'value',
                scale: true
            },
            series: [
                {

                    type: 'line',
                    //stack: '总量',
                    data: chart_data //[120, 132, '', 134, 90, 230, 210] //
                }
            ]
        };

        chart.setOption(option);
    }

    //show_chart();

    function on_row_clk(id) {
    }

    var allshow = false;

    var sdiv = $("#main_left")[0];
    $("#main_left").scroll(() => {
        var wholeHeight = sdiv.scrollHeight;
        var scrollTop = sdiv.scrollTop;
        var divHeight = sdiv.clientHeight;
        if (divHeight + scrollTop >= wholeHeight) {//reach btm
            if (!page_has_next) {
                if (!allshow)
                    dlg.msg("no more list items");
                allshow = true;
                return;
            }

            //console.log("show more");
            show_list_more();
            $("main_left").scroll(scrollTop);
        }
        if (scrollTop == 0) {//reach top

        }
    });

    function do_search() {
        //tagpath = $("#tag_sel").val() ;
        valid = $("#valid_sel").val();
        start_dt = $("#start_dt").val();
        end_dt = $("#end_dt").val();

        chart_data = [];
        update_list();
    }

    function do_search_all() {
        //tagpath="" ;
        //$("#tag_sel").val("") ;
        valid = "";
        $("#valid_sel").val("");
        start_dt = "";
        $("#start_dt").val("");
        end_dt = "";
        $("#end_dt").val("");

        chart_data = [];
        update_list();
    }
</script>
</body>
</html>