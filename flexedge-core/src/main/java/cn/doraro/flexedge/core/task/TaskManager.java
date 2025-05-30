package cn.doraro.flexedge.core.task;

import cn.doraro.flexedge.core.UAManager;
import cn.doraro.flexedge.core.util.xmldata.DataTranserXml;
import cn.doraro.flexedge.core.util.xmldata.XmlData;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {
    private static TaskManager instance = null;
    private HashMap<String, List<Task>> prj2tasks = new HashMap<>();


    private TaskManager() {
    }

    public static TaskManager getInstance() {
        if (instance != null)
            return instance;

        synchronized (TaskManager.class) {
            if (instance != null)
                return instance;

            instance = new TaskManager();
            return instance;
        }
    }

    public List<Task> getTasks(String prjid) {
        List<Task> ts = prj2tasks.get(prjid);
        if (ts != null)
            return ts;

        synchronized (Task.class) {
            ts = prj2tasks.get(prjid);
            if (ts != null)
                return ts;

            try {
                ts = loadJsTasks(prjid);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (ts == null)
                ts = new ArrayList<>(0);
            prj2tasks.put(prjid, ts);
            return ts;
        }

    }

    public Task getTask(String prjid, String id) {
        List<Task> ts = getTasks(prjid);
        if (ts == null)
            return null;
        for (Task t : ts) {
            if (t.getId().equals(id))
                return t;
        }
        return null;
    }

    private File getTaskFile(String prjid, String taskid) {
        File prjdir = UAManager.getPrjFileSubDir(prjid);
        return new File(prjdir, "task_" + taskid + ".xml");
    }

    public File findTaskFile(String prjid, String taskid) {
        File tf = this.getTaskFile(prjid, taskid);
        if (!tf.exists())
            return null;
        return tf;
    }

    private List<Task> loadJsTasks(String prjid) throws Exception {
        File prjdir = UAManager.getPrjFileSubDir(prjid);
        if (!prjdir.exists())
            return null;
        File[] tfs = prjdir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory())
                    return false;
                String fn = f.getName().toLowerCase();
                return fn.startsWith("task_") && fn.endsWith(".xml");
            }
        });
        if (tfs == null || tfs.length <= 0)
            return null;

        ArrayList<Task> rets = new ArrayList<>();
        for (File tf : tfs) {
            XmlData xd = XmlData.readFromFile(tf);
            if (xd == null)
                continue;
            Task jst = new Task(prjid);
            if (!DataTranserXml.injectXmDataToObj(jst, xd))
                continue;
            jst.refreshActions();
            rets.add(jst);
        }
        return rets;
    }

    void saveTask(String prjid, Task t) throws Exception {
        XmlData xd = DataTranserXml.extractXmlDataFromObj(t);
        //XmlData xd = rep.toUAXmlData();
        File tf = getTaskFile(prjid, t.getId());
        XmlData.writeToFile(xd, tf);
    }

    synchronized public Task setTask(String prjid, Task jst) throws Exception {
        List<Task> ts = getTasks(prjid);
        int s = ts.size();
        int i;
        for (i = 0; i < s; i++) {
            Task t = ts.get(i);
            if (t.getId().equals(jst.getId())) {
                jst.actions = t.actions;
                ts.set(i, jst);
                break;
            }
        }
        if (i >= s)
            ts.add(jst);
        saveTask(prjid, jst);
        return jst;
    }

    synchronized public boolean delTask(String prjid, String id) {
        Task t = getTask(prjid, id);
        if (t == null)
            return false;
        File tf = getTaskFile(prjid, id);
        if (!tf.delete())
            return false;
        List<Task> ts = getTasks(prjid);
        ts.remove(t);
        return true;
    }

    public TaskAction getTaskAction(String prjid, String taskid, String actid) {
        Task t = this.getTask(prjid, taskid);
        if (t == null)
            return null;
        return t.getActionById(actid);
    }

    public void setTaskActionBasic(String prjid, String taskid, TaskAction ta) throws Exception {
        Task t = getTask(prjid, taskid);
        if (t == null)
            throw new Exception("no task found");

        t.setActionBasic(ta);
        this.saveTask(prjid, t);
    }


    public boolean delTaskAction(String prjid, String taskid, String actid) throws Exception {
        Task t = getTask(prjid, taskid);
        if (t == null)
            return false;
        if (t.delAction(actid) != null) {
            this.saveTask(prjid, t);
            return true;
        }
        return false;
    }
}
