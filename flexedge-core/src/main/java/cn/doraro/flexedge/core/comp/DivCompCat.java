package cn.doraro.flexedge.core.comp;

import cn.doraro.flexedge.core.dict.DataClass;

import java.util.ArrayList;
import java.util.List;

public class DivCompCat {
    String name = null;

    //String title = null ;

    DataClass dc = null;

    ArrayList<DivCompItem> items = new ArrayList<>();

    public DivCompCat(String name, DataClass dc) {
        this.name = name;
        this.dc = dc;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return this.dc.getNameBySysLan(this.name);
    }

    public List<DivCompItem> getItems() {
        return items;
    }

    public DivCompItem getItem(String name) {
        for (DivCompItem item : items) {
            if (item.getName().equals(name))
                return item;
        }
        return null;
    }
}
