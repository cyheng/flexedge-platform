<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="
	cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.res.*,
                 cn.doraro.flexedge.core.basic.*,
                 java.io.*,
                 java.util.*,
                 java.net.*,
                 java.util.*" %>
<%!
    private static void addOrEditDev(HttpServletRequest request, JspWriter out) throws Exception {
        String chpath = request.getParameter("ch_path");
        String devpath = request.getParameter("dev_path");
        if (!Convert.checkReqEmpty(request, out, "name"))//,"devdef_id"))
            return;
        UACh ch = null;
        UADev dev = null;
        if (Convert.isNotNullEmpty(chpath)) {
            ch = (UACh) UAUtil.findNodeByPath(chpath);
            if (ch == null) {
                out.print("no ch node found");
                return;
            }
        } else if (Convert.isNotNullEmpty(devpath)) {
            dev = (UADev) UAUtil.findNodeByPath(devpath);
            if (dev == null) {
                out.print("no dev node found");
                return;
            }
        } else {
            out.print("no ch_path or dev_path input");
            return;
        }
        //String chid = request.getParameter("chid");
        String libid = request.getParameter("libid");
        String catid = request.getParameter("catid");
        String devid = request.getParameter("devid");
        String devmodel = request.getParameter("dev_model");

        String name = request.getParameter("name");
        String title = request.getParameter("title");
        String desc = request.getParameter("desc");
        float x = Convert.parseToFloat(request.getParameter("x"), 0.0f);
        float y = Convert.parseToFloat(request.getParameter("y"), 0.0f);

        //StringBuilder errsb = new StringBuilder() ;

        //HashMap<String,Object> uips = new HashMap<>() ;

        try {
            if (dev == null)
                dev = ch.addDev(name, title, desc, libid, devid, devmodel);
            else {
                UACh ch0 = dev.getBelongTo();
                ch0.updateDev(dev, name, title, desc, libid, devid, devmodel);
            }

            if (Convert.isNotNullEmpty(libid) && Convert.isNotNullEmpty(devid)) {
                UAPrj prj = ch.getBelongTo();
                String ref_lib_id = prj.getResLibId();
                DevDef dd = DevManager.getInstance().getDevDefById(libid, devid);
                ResManager.getInstance().getDevDef(ref_lib_id, dd.getResLibId(), devid, true);
            }
            out.print("succ=" + dev.getId());
            return;
        } catch (Exception e) {
            e.printStackTrace();
            out.print(e.getMessage());
            return;
        }

    }

    private static void delDev(HttpServletRequest request, JspWriter out) throws Exception {
        if (!Convert.checkReqEmpty(request, out, "dev_path"))
            return;
        String devpath = request.getParameter("dev_path");
        UADev dev = (UADev) UAUtil.findNodeByPath(devpath);
        if (dev == null) {
            out.print("no dev node found");
            return;
        }

        boolean b = dev.delFromParent();
        if (!b) {
            out.print("del err");
            return;
        } else {
            out.print("succ=" + dev.getId());
            return;
        }
    }

    private static void refreshDev(HttpServletRequest request, JspWriter out) throws Exception {
        if (!Convert.checkReqEmpty(request, out, "dev_path"))
            return;
        String devpath = request.getParameter("dev_path");
        UADev dev = (UADev) UAUtil.findNodeByPath(devpath);
        if (dev == null) {
            out.print("no dev node found");
            return;
        }
        //dev.getBelongTo().refreshDev(dev) ;

        out.print("succ=" + dev.getId());
        return;

    }
%><%
    if (!Convert.checkReqEmpty(request, out, "op"))
        return;
    String op = request.getParameter("op");
    switch (op) {
        case "add":
        case "edit":
            addOrEditDev(request, out);
            break;
        case "del":
            delDev(request, out);
            break;
        case "refresh":
            refreshDev(request, out);
            break;
    }
%>