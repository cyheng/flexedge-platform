<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="
	cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.*,
                 java.io.*,
                 java.util.*,
                 java.net.*,
                 java.util.*,
                 cn.doraro.flexedge.core.cxt.*,
                 cn.doraro.flexedge.core.ws.*,
                 cn.doraro.flexedge.core.util.xmldata.*" %>
<%
    String lan = Lan.getUsingLang();
%><!DOCTYPE html>
<html class="">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width,minimum-scale=1,initial-scale=1">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,Chrome=1">
    <title>IOT-Tree</title>
    <link href="/favicon.ico" rel="shortcut icon" type="image/x-icon">
    <script src="/_js/jquery-1.12.0.min.js"></script>
    <script src="/_js/bootstrap/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="/_js/ajax.js"></script>
    <link rel="stylesheet" type="text/css" href="/_js/layui/css/layui.css"/>
    <script src="/_js/layui/layui.all.js"></script>
    <script src="/_js/dlg_layer.js?v="></script>
    <link href="/_js/bootstrap/css/bootstrap.min.css" rel="stylesheet" type="text/css">
    <link href="/_js/font4.7.0/css/font-awesome.css" rel="stylesheet" type="text/css">
    <link href="./inc/common.css" rel="stylesheet" type="text/css">
    <link href="./inc/index.css" rel="stylesheet" type="text/css">
    <style>
        .btn_sh {
        / / display: none;
            visibility: hidden;
        }

        .btn_sh_c:hover .btn_sh {
            visibility: visible;
        }

    </style>
</head>
<body aria-hidden="false">
<div class="iot-top-menu-wrap">

    <div class="iot-logo">
        <a style="width:300px"><img src="inc/logo1.png" width="40px" height="40px"/> IOT-Tree Server Document</a>
    </div>

    <div class="iot-top-nav navbar">
        <div class="navbar-header">
            <button class="navbar-toggle pull-left">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
        </div>
        <nav role="navigation" class="collapse navbar-collapse bs-navbar-collapse">
            <ul class="nav navbar-nav">
                <li><a href="https://github.com/bambooww/iot-tree.git" target="_blank" class=""><i
                        class="icon icon-home"></i> Github</a></li>
                <li><a href="javascript:set_lang('en')"><i class="icon icon-home"></i> English</a></li>
                <li><a href="javascript:set_lang('cn')"><i class="icon icon-topic"></i> 中文</a></li>
                <%--
                <li><a href="javascript:set_lang('jp')" ><i class="icon icon-topic"></i> 日本語</a></li>
                <li><a href="javascript:set_lang('nl')" ><i class="icon icon-topic"></i> Nederlands</a></li>
                 --%>
                <li><a href="mailto:iottree@hotmail.com"><i class="icon icon-topic"></i> Feedback</a></li>
            </ul>
        </nav>

    </div>
    <div style="position: absolute;right:30px;">Version:<%=Config.getVersion() %>
    </div>
</div>
<div style="position: absolute;width:100%;bottom:0px;top:50px;overflow:hidden;">
    <table style="height:100%;width:100%">
        <tr>
            <td style="width:20%">
                <iframe id="nav" src="en/nav.md?outline=false" style="width:100%;height:100%;overflow: auto"></iframe>
            </td>
            <td style="width:80%">
                <iframe id="main" src="en/README.md" style="width:100%;height:100%;"></iframe>
            </td>
        </tr>
    </table>
</div>
</body>
<script type="text/javascript">
    var using_lan = "<%=lan%>";

    function set_lang(lang) {
        $("#nav").attr("src", lang + "/nav.md");
        var m = $("#main");
        var p = m.attr("src");
        var b_nochg = false;
        if (p == null || p == "")
            p = lang + "/README.md";
        else {
            if (p.indexOf(lang + "/") == 0)
                b_nochg = true;
            var k = p.indexOf('/');
            if (k > 0)
                p = lang + "/" + p.substr(k + 1);
        }

        if (b_nochg)
            return;

        $("#main").attr("src", p);
    }

    function nav_to(p) {
        $("#main").attr("src", p);
    }

    set_lang(using_lan);
</script>
</html>
