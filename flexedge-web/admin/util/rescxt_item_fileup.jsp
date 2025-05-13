<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,
                 java.io.*,
                 java.net.*,
                 cn.doraro.flexedge.core.util.*,
                 cn.doraro.flexedge.core.util.web.*,
                 cn.doraro.flexedge.core.*,
                 cn.doraro.flexedge.core.res.*,
                 cn.doraro.flexedge.core.util.xmldata.*,
                 org.apache.commons.fileupload.*,
                 org.apache.commons.fileupload.servlet.*,
                 org.apache.commons.fileupload.disk.*" %>
<%!
    private static final int MEMORY_THRESHOLD = 1024 * 1024 * 3;  // 3MB
    private static final int MAX_FILE_SIZE = 1024 * 1024 * 40; // 40MB
    private static final int MAX_REQUEST_SIZE = 1024 * 1024 * 50; // 50MB

    public static HashMap<String, String> getReqParams(List<FileItem> fitems) throws Exception {
        HashMap<String, String> ret = new HashMap<>();
        if (fitems == null || fitems.size() <= 0)
            return ret;
        for (FileItem fi : fitems) {
            if (!fi.isFormField())
                continue;

            String fn = fi.getFieldName();
            String v = fi.getString("UTF-8");
            ret.put(fn, v);
        }
        return ret;
    }
%><%

    if (!ServletFileUpload.isMultipartContent(request)) {
        PrintWriter writer = response.getWriter();
        writer.println("Error: form must has enctype=multipart/form-data");
        writer.flush();
        return;
    }

// 配置上传参数
    DiskFileItemFactory factory = new DiskFileItemFactory();

    factory.setSizeThreshold(MEMORY_THRESHOLD);
// 设置临时存储目录
    factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

    ServletFileUpload upload = new ServletFileUpload(factory);

// 设置最大文件上传值
    upload.setFileSizeMax(MAX_FILE_SIZE);

// 设置最大请求值 (包含文件和表单数据)
    upload.setSizeMax(MAX_REQUEST_SIZE);

// 中文处理
    upload.setHeaderEncoding("UTF-8");

    File dirb = null;

    List<FileItem> formItems = upload.parseRequest(request);
    HashMap<String, String> pms = getReqParams(formItems);
    String reslibid = pms.get("res_lib_id");
    String res_id = pms.get("res_id");
    String name = pms.get("name");

    if (Convert.isNullOrEmpty(reslibid)) {
        out.print("no res_lib_id input");
        return;
    }
    if (Convert.isNullOrEmpty(name)) {
        out.print("no name input");
        return;
    }

    ResDir rdir = ResManager.getInstance().getResDir(reslibid, res_id); //

    //ResLib reslib = ResManager.getInstance().getResLibByLibId(res_lib_id) ;
    if (rdir == null) {
        out.print("no ResDir found");
        return;
    }


    FileItem fi = null;

    if (formItems != null && formItems.size() > 0) {

        for (FileItem item : formItems) {
            if (item.isFormField())
                continue;

            long flen = item.getSize();
            if (flen <= 0)
                continue;
            String tmpn = item.getName();
            int k = tmpn.indexOf('.');
            if (k <= 0)
                continue;
            fi = item;
            break;
        }
    }

    rdir.setResItem(name, fi);%>succ