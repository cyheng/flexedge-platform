package cn.doraro.flexedge.core.msgnet.modules;

import cn.doraro.flexedge.core.msgnet.MNModule;
import cn.doraro.flexedge.core.util.Convert;
import org.json.JSONObject;

import java.io.*;

public class FileDir_M extends MNModule {
    String dirPath = null;
    private transient FileItem curOpenFile = null;

    @Override
    public String getTP() {
        return "file_dir";
    }

    @Override
    public String getTPTitle() {
        return g("file_dir");
    }

    @Override
    public String getColor() {
        return "#e7b686";
    }

    @Override
    public String getIcon() {
        return "\\uf07b";
    }

    @Override
    public String getPmTitle() {
        if (Convert.isNullOrEmpty(this.dirPath))
            return "no dir path set";
        return this.dirPath;
    }

    @Override
    public boolean isParamReady(StringBuilder failedr) {
        if (Convert.isNullOrEmpty(this.dirPath)) {
            failedr.append("no dir path set");
            return false;
        }
        return true;
    }

    @Override
    public JSONObject getParamJO() {
        JSONObject jo = new JSONObject();
        jo.putOpt("dir_path", this.dirPath);
        return jo;
    }

    @Override
    protected void setParamJO(JSONObject jo) {
        this.dirPath = jo.optString("dir_path");
    }

    public File getDirBase() {
        if (Convert.isNullOrEmpty(this.dirPath))
            return null;
        return new File(this.dirPath);
    }

    FileItem RT_openFile(String sub_fp, StringBuilder failedr) throws IOException {
        File dirb = this.getDirBase();
        if (dirb == null) {
            failedr.append("no dir base in FileDir_M");
            return null;
        }
        File f = new File(dirb, sub_fp);
        if (curOpenFile != null)
            curOpenFile.close();

        return curOpenFile = new FileItem(f);
    }

    FileItem getOpenedFile() {
        return this.curOpenFile;
    }

    void RT_closeFile() throws IOException {
        try {
            if (curOpenFile != null)
                curOpenFile.close();
        } finally {
            curOpenFile = null;
        }
    }

    public static class FileItem implements Closeable {
        File file = null;

        FileInputStream inputs = null;

        FileOutputStream outputs = null;

        public FileItem(File f) {
            this.file = f;
        }

        private synchronized OutputStream getOutputS() throws FileNotFoundException {
            if (outputs != null)
                return outputs;
            File pdir = file.getParentFile();
            if (!pdir.exists())
                pdir.mkdirs();

            return outputs = new FileOutputStream(file);
        }

        public void write(byte[] bs) throws IOException {
            write(bs, 0, bs.length);
        }

        public void write(byte[] bs, int offset, int len) throws IOException {
            OutputStream os = getOutputS();
            os.write(bs, offset, len);
        }

        @Override
        public void close() throws IOException {
            if (inputs != null) {
                try {
                    inputs.close();
                    inputs = null;
                } catch (Exception ee) {
                }
            }

            if (outputs != null) {
                try {
                    outputs.close();
                    outputs = null;
                } catch (Exception ee) {
                }
            }
        }
    }
}
