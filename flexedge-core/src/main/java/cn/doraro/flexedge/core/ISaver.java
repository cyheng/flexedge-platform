package cn.doraro.flexedge.core;

import java.io.File;

public interface ISaver {
    public void save() throws Exception;

    public File getSaverDir();
}
