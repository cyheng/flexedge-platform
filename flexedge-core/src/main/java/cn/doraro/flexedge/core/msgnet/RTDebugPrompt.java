package cn.doraro.flexedge.core.msgnet;

import cn.doraro.flexedge.core.util.Convert;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;

public class RTDebugPrompt implements Comparable<RTDebugPrompt> {
    String tp = null;
    private long dt = -1;
    private String msg = null;
    private String content = null;
    private Throwable exception = null;

    public RTDebugPrompt(String tp, String msg, String content, Throwable excep) {
        this.tp = tp;
        this.dt = System.currentTimeMillis();
        this.msg = msg;
        this.content = content;
        this.exception = excep;
        if (Convert.isNullOrEmpty(msg)) {
            if (Convert.isNotNullEmpty(content)) {
                this.msg = content;
                if (this.msg.length() > 80)
                    this.msg = content.substring(0, 80) + "...";
            } else if (excep != null)
                this.msg = excep.getMessage();
            else
                throw new IllegalArgumentException("invalid prompt input");
        }
    }

    public static String transToStr(Throwable t) {
        if (t == null)
            return "";

        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw, true));
        return sw.getBuffer().toString();
    }

    public String getTP() {
        return this.tp;
    }

    public long getDT() {
        return dt;
    }

    public String getDTGapToNow() {
        return Convert.calcDateGapToNow(this.dt);
    }

    public String getMsg() {
        return msg;
    }

    public String getContent() {
        return this.content;
    }

    public Throwable getException() {
        return this.exception;
    }

    public boolean hasDetail() {
        return Convert.isNotNullEmpty(this.content) || this.exception != null;
    }

    public JSONObject toListJO() {
        JSONObject jo = new JSONObject();
        jo.put("dt", dt);
        jo.put("gap_now", this.getDTGapToNow());
        jo.put("msg", msg);
        return jo;
    }

    public String getListTitle() {
        return this.getDTGapToNow() + " " + this.msg;
    }

    //public

    public JSONObject toDetailJO() {
        JSONObject jo = toListJO();
        jo.putOpt("content", content);
        if (this.exception != null)
            jo.put("exception", transToStr(this.exception));
        return jo;
    }

    @Override
    public int compareTo(RTDebugPrompt o) {
        return (int) (this.dt - o.dt);
    }
}
