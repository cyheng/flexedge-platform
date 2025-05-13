<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,
                 java.io.*,
                 cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.task.*,
                 cn.doraro.flexedge.core.basic.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.dict.*,
                 cn.doraro.flexedge.core.util.xmldata.*,
                 cn.doraro.flexedge.core.util.web.*,
                 cn.doraro.flexedge.core.comp.*,
                 cn.doraro.flexedge.core.util.logger.*" %>
<%!

%><%
    if (!Convert.checkReqEmpty(request, out, "op"))
        return;
    String logid = request.getParameter("logid");
    String op = request.getParameter("op");
    String v = request.getParameter("v");
    switch (op) {
        case "ctrl":
            if (!Convert.checkReqEmpty(request, out, "logid", "v"))
                return;
            ILogger log = LoggerManager.getLoggerExisted(logid);
            int cv = Integer.parseInt(v);
            if (log == null) {
                out.print("no log found");
                return;
            }
            log.setCtrl(cv);
            out.print("{ctrl:" + cv + "}");
            return;
        case "lvl":
            if (!Convert.checkReqEmpty(request, out, "logid", "v"))
                return;
            cv = Integer.parseInt(v);
            log = LoggerManager.getLoggerExisted(logid);
            if (log == null) {
                out.print("no log found");
                return;
            }
            log.setCurrentLogLevel(cv);
            out.print("{trace:" + log.isTraceEnabled() + ",debug:" + log.isDebugEnabled() +
                    ",info:" + log.isInfoEnabled() + ",warn:" + log.isWarnEnabled() + ",error:" + log.isErrorEnabled() + "}");
            return;
        case "def_lvl":
            if (!Convert.checkReqEmpty(request, out, "v"))
                return;
            cv = Integer.parseInt(v);
            LoggerManager.setDefaultLogLevel(cv);
            out.print("{v:" + cv + "}");
        case "save":
            try {
                LoggerManager.saveLogConfig();
                out.print("ok");
            } catch (Exception e) {
                e.printStackTrace();
                out.print(e.getMessage());
            }
            return;
        case "set_def_all":
            LoggerManager.setLogToDefaultAll();
            out.print("ok");
            return;
    }
%>