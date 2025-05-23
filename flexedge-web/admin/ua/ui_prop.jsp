<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ page import="java.util.*,
                 java.io.*,
                 java.util.*,
                 cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.basic.*,
                 java.net.*" %>
<%@ taglib uri="wb_tag" prefix="wbt" %>
<%
    if (!Convert.checkReqEmpty(request, out, "path"))
        return;
    //boolean bdev = "true".equals(request.getParameter("bdev")) ;
    boolean bmgr = "true".equals(request.getParameter("mgr"));
    String path = request.getParameter("path");
    UANode n = UAUtil.findNodeByPath(path);
    if (n == null) {
        out.print("no node found");
        return;
    }

    String usingln = Lan.getUsingLang();
	/*
	String repid = request.getParameter("repid") ;
	String id = request.getParameter("id") ;
	UANode n = null;
	if(Convert.isNotNullEmpty(repid)&&Convert.isNotNullEmpty(id))
	{//node in rep
		UAManager uam = UAManager.getInstance();
		UARep dc = uam.getRepById(repid) ;
		if(dc==null)
		{
			out.print("no rep found with id="+repid) ;
			return ;
		}
		n = dc.findNodeById(id);
		if(n==null&&dc.getId().equals(id))
			n = dc ;
		
	}
	else
	{
		
	}
	*/
    String t = n.getTitle();
    boolean bdlg = !"false".equals(request.getParameter("dlg"));

    List<PropGroup> pgs = n.listPropGroups();
    if (pgs == null)
        pgs = new ArrayList<>(0);
%><!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Properties Editor</title>
    <jsp:include page="../head.jsp">
        <jsp:param value="true" name="oc"/>
        <jsp:param value="false" name="simple"/>
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

        .prop_table {
            width: 99%;
            height: 99%;
            border: 0px;
            margin: 0 auto;
        }

        .prop_table tr > td {
            border: 0;
            height: 100%
        }

        .prop_table tr > div {
            border: 0;

        }

        .prop_edit_cat {
            border: 1px solid #cccccc;
            height: 400px;
            padding: 3px;
            margin: 2px;
            overflow: auto;
        }

        .prop_edit_panel {
            border: 1px solid #cccccc;
            height0: 550px;
            padding: 0px;
            margin: 2px;
            overflow: auto;
        }

        .prop_edit_path {
            font-weight: bold;
            border: 1px solid #cccccc;
            background-color: #f0f0f0;
            padding: 3px;
            margin: 2px;
            overflow: hidden;
        }

        .prop_edit_desc {
            border: 1px solid #cccccc;
            background-color: #f0f0f0;
            height: 48px;
            padding-left: 3px;
            padding-right: 3px;
            padding-bottom: 0px;
            padding-top: 0px;
            margin-left: 2px;
            margin-right: 2px;
            margin-top: 0px;
            margin-bottom: 0px;
            overflow: hidden;
        }

        .site-dir li {
            line-height: 26px;
            margin-left: 20px;
            overflow: visible;
            list-style-type: square;
        }

        li {
            list-style: none;
        }

        .site-dir li a {
            display: block;
            color: #333;
            cursor: pointer;
            text-decoration: none;
        }


        .site-dir li a.layui-this {
            color: #01AAED;
        }

        .pi_edit_table {
            width: 100%;
            border: 0px solid #b4b4b4;
            margin: 0 auto;
        }


        .pi_edit_table tr > td {
            border: 1px solid #b4b4b4;
            height: 100%;


        }

        .pi_edit_table .td_left {
            padding-left: 20px;
        }

        .pi_edit_table tr > div {
            border: 0;

        }

        .pi_sel {
            background-color: #0078d7;
            color: #ffffff;
        }

        .pi_edit_unit {
            border: 0px;
            width: 100%;
        }

    </style>
</head>
<script type="text/javascript">
</script>
<body>
<blockquote class="layui-elem-quote">&nbsp;
    <div style="left:20px;top:5px;position:absolute;font:bold;font-size: 18px"><%=n.getNodePathTitle() %>
    </div>
    <div style="left:20px;top:25px;position:absolute;font-size:10px;"> [<%=n.getNodePath() %>]</div>
    <div style="float: right;margin-right:10px;font: 15px solid;color:#fff5e2">
        <%
            if (!bdlg) {
        %>
        <div style="border:0px solid #ffff00;height:45px;text-align:right;padding-right:30px">
            <button id="btn_apply" type="button" class="layui-btn layui-btn-sm  layui-btn-warm layui-btn-disabled"
                    style="margin-right:5px;width:80px"><wbt:g>apply</wbt:g></button>
            <button id="btn_help" type="button" class="layui-btn layui-btn-sm  layui-btn-primary "
                    style="margin-right:5px;width:80px"><wbt:g>help</wbt:g></button>
        </div>
        <%
            }
        %>
    </div>
</blockquote>
<div style="position: absolute;top:53px;bottom:3px;overflow: auto;width:100%;">
    <table class="prop_table">
        <tr>
            <%--

              <td colspan="2"><div id="prop_edit_path" class="prop_edit_path"></div></td>
            </tr>
            <tr>
              <td style="width:30%" >
                  <div id="editcats" class="prop_edit_cat">
                      <ul class="site-dir layui-layer-wrap" style="display: block;">
          <%

          String pg0="" ;

              for(PropGroup pg:pgs)
              {
                  if("".equals(pg0))
                      pg0 = pg.getName() ;
          %>
           <li><a id="pg_<%=pg.getName() %>" onclick="sel_pg('<%=pg.getName() %>')"><%=pg.getTitle() %></a></li>
          <%
              }
          %>
          </ul>
                  </div>
              </td>
               --%>
            <td style="width:90%;vertical-align: top;">
                <div id="editpanel" class="prop_edit_panel">

                    <%
                        for (PropGroup pg : pgs) {
                            String pgn = pg.getName();
                            List<PropItem> pis = pg.getPropItems();
                            String help_url = pg.getHelpUrl();
                    %>
                    <table id="tb_pg_<%=pgn %>" class="pi_edit_table">
                        <tr>
                            <td colspan="2" class="td_left"
                                style="font-weight: bold;color: #000000;background-color: #f0f0f0"><%=pg.getTitle() %>
                                <%
                                    if (Convert.isNotNullEmpty(help_url)) {
                                %><span onclick="open_help('<%=help_url %>')" style="color:#1d89f0"><i
                                        class="fa fa-question-circle fa-lg"></i>&nbsp;<wbt:g>help</wbt:g></span><%
                                    }
                                %>
                            </td>
                        </tr>
                        <%
                            for (PropItem pi : pis) {
                        %>
                        <tr id="pi_<%=pi.getName()%>" onclick="sel_pi('<%=pi.getName()%>')" title="<%=pi.getDesc()%>">
                            <td style="width:50%" class="td_left"><%=pi.getTitle() %>
                            </td>
                            <td style="width:50%;color:#010101">
                                <%
                                    PropItem.ValOpts vopts = pi.getValOpts();
                                    if (vopts == null || pi.isReadOnly()) {
                                        String inptp = "text";
                                        String inp_step = "";
                                        String readonly = "";
                                        if (pi.isReadOnly())
                                            readonly = "readonly='readonly'";
                                        if (pi.getVT() == PropItem.PValTP.vt_int) {
                                            inptp = "number";
                                            inp_step = "1";
                                        } else if (pi.getVT() == PropItem.PValTP.vt_float) {
                                            inptp = "number";
                                            //inp_step="1" ;
                                        }
                                        String defv = "";
                                        if (pi.getDefaultVal() != null)
                                            defv = pi.getDefaultVal().toString();
                                        String multiln = "";
                                        boolean bmultiln = pi.isTxtMultiLine();
                                        String popname = pi.getPopName();
                                        String poptitle = pi.getPopTitle();
                                        boolean has_pop = Convert.isNotNullEmpty(popname);
                                        String uname = pg.getName() + "." + pi.getName();
                                        if (bmultiln || has_pop) {
                                            if (has_pop) {
                                %>
                                <textarea id="piv_<%=uname %>" class="pi_edit_unit" type="<%=inptp %>"
                                          step="<%=inp_step %>"
                                          style="padding-left: 3px;height:100%" <%=readonly %>
                                          onclick="pop_edit('<%=popname %>','<%=poptitle %>','piv_<%=uname %>')"><%=defv %></textarea>
                                <%
                                } else {
                                %>
                                <textarea id="piv_<%=uname %>" class="pi_edit_unit" type="<%=inptp %>"
                                          step="<%=inp_step %>"
                                          style="padding-left: 3px;height:100%" <%=readonly %>
                                          onclick="pop_multi_ln_edit('piv_<%=uname %>')"><%=defv %></textarea>

                                <%
                                    }
                                } else {
                                %>
                                <input id="piv_<%=uname %>" class="pi_edit_unit" type="<%=inptp %>"
                                       step="<%=inp_step %>" value="<%=defv %>"
                                       style="padding-left: 3px" <%=readonly %>/>
                                <%
                                    }
                                } else {
                                    List<PropItem.ValOpt> opts = vopts.getOpts();
                                %>
                                <select id="piv_<%=pg.getName() %>.<%=pi.getName() %>" class="pi_edit_unit">
                                    <%
                                        String sel = "";
                                        for (PropItem.ValOpt opt : opts) {
                                            sel = "";
                                            if (opt.getVal() != null && opt.getVal().equals(pi.getDefaultVal()))
                                                sel = "selected=\"selected\"";
                                    %>
                                    <option value="<%=opt.getVal()%>" <%=sel %>><%=opt.getTitle() %>
                                    </option>
                                    <%
                                        }
                                    %>
                                </select>
                                <%
                                    }
                                %>
                            </td>
                        </tr>
                        <%
                            }
                        %>
                    </table>
                    <%
                        }
                    %>

                </div>
                <div id="editdesc" class="prop_edit_desc">

                </div>
            </td>
        </tr>
    </table>
</div>
<%--
<%
if(!bdlg)
{
%>
<div style="border:0px solid #ffff00;height:45px;text-align:right;padding-right:30px;margin-top:10px">
<button id="btn_apply" type="button" class="layui-btn layui-btn-sm  layui-btn-warm layui-btn-disabled" style="margin-right:5px;width:80px" onclick="dlg.btn_clk('x20210306090015_1',1)">Apply</button>
<button id="btn_help" type="button" class="layui-btn layui-btn-sm  layui-btn-primary " style="margin-right:5px;width:80px" onclick="dlg.btn_clk('x20210306090015_1',3)">Help</button>
</div>
<%
}
%>
 --%>
</body>
<script type="text/javascript">
    //var repid="";
    var path = "<%=path%>";
    var nodeid = "<%=n.getId()%>";
    var lan = "<%=usingln%>";
    var bdirty = false;

    function sel_pg(pgn) {
        $('.layui-this').removeClass("layui-this");
        $("#pg_" + pgn).addClass("layui-this");

        show_pg_props(pgn);
    }

    function show_pg_props(pgn) {

    }

    var cur_txt = "";


    function pop_multi_ln_edit(inputid) {
        var input = document.getElementById(inputid);
        cur_txt = input.value;
        dlg.open("ui_prop_multiln.jsp",
            {title: "<wbt:lang>edit_txt</wbt:lang>", w: '500px', h: '400px'},
            ['<wbt:lang>ok</wbt:lang>', '<wbt:lang>cancel</wbt:lang>'],
            [
                function (dlgw) {
                    dlgw.do_submit(function (bsucc, ret) {
                        if (!bsucc) {
                            dlg.msg(ret);
                            return;
                        }
                        input.value = ret.txt;

                        setDirty(true);
                        dlg.close();
                    });
                },
                function (dlgw) {
                    dlg.close();
                }
            ]);
    }

    function pop_edit(popname, poptitle, inputid) {
        var input = document.getElementById(inputid);
        cur_txt = input.value;
        dlg.open("ui_prop_pop_" + popname + ".jsp?nodepath=" + path,
            {title: poptitle, w: '500px', h: '400px', inputv: cur_txt},
            ['<wbt:lang>ok</wbt:lang>', '<wbt:lang>cancel</wbt:lang>'],
            [
                function (dlgw) {
                    dlgw.do_submit(function (bsucc, ret) {
                        if (!bsucc) {
                            dlg.msg(ret);
                            return;
                        }
                        input.value = ret.txt;

                        setDirty(true);
                        dlg.close();
                    });
                },
                function (dlgw) {
                    dlg.close();
                }
            ]);
    }

    function init_chg() {
        $("#editpanel input,select,textarea").on('input', function (e) {
            setDirty(true);
        });
    }

    function isDirty() {
        return bdirty;
    }

    function setDirty(b) {
        bdirty = b;
        dlg.btn_set_enable(1, bdirty);

        if (b)
            $('#btn_apply').removeClass("layui-btn-disabled");
        else
            $('#btn_apply').addClass("layui-btn-disabled");
    }

    $('#btn_apply').on('click', function (e) {
        do_apply();
    });

    function do_apply(succcb) {
        if (!bdirty)
            return;

        var pm = {};
        pm.path = path;
        pm.op = "save";
        pm.txt = get_prop_vals();
        ;
        send_ajax("./ui_prop_ajax.jsp", pm, function (bsucc, ret) {
            if (!bsucc || ret != "succ") {
                dlg.msg(ret);
                return;
            }

            setDirty(false);
            if (succcb != undefined && succcb != null)
                succcb();
        });

    }

    function sel_pi(pin) {
        $('.pi_sel').removeClass("pi_sel");
        $("#pi_" + pin).addClass("pi_sel");
    }

    function load_val() {
        var pm = {};
        pm.path = path;
        //pm.id = nodeid ;
        pm.op = "load";
        send_ajax("ui_prop_ajax.jsp", pm, function (bsucc, ret) {
            if (!bsucc) {
                dlg.msg(ret);
                return;
            }
            var ob = null;
            eval("ob=" + ret);
            for (var n in ob) {
                var v = ob[n];
                var piob = document.getElementById("piv_" + n);
                if (piob == null)
                    continue;
                $(piob).val(v + "");
            }

            init_chg();
        });
    }

    //sel_pg(pg0);
    load_val();

    function get_prop_vals() {
        var r = {};
        $("#editpanel input,select,textarea").each(function () {
            if (this.id.indexOf('piv_') != 0)
                return;
            var id = this.id.substring(4);
            var v = $(this).val();
            r[id] = v;
        });
        return JSON.stringify(r);
    }

    function open_help(u) {
        window.open("/doc/" + lan + u);
    }
</script>
</html>