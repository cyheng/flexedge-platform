package cn.doraro.flexedge.core.msgnet;

import cn.doraro.flexedge.core.basic.NameTitle;

import java.util.List;

/**
 * IMNContainer 接口实现的同时，如果也实现了此接口，则
 *
 * @author jason.zhu
 */
public interface IMNContTagListMapper {
    /**
     * @return
     */
    public List<NameTitle> getMNContTagListCatTitles();

    public List<NameTitle> getMNContTagList(String cat);
}
