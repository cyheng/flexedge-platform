// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.modbuss;

import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.CompressUUID;
import cn.doraro.flexedge.core.util.xmldata.data_val;
import cn.doraro.flexedge.core.util.xmldata.data_class;

@data_class
public class SlaveNode
{
    @data_val
    String id;
    @data_val
    String name;
    @data_val
    String title;
    
    public SlaveNode() {
        this.id = null;
        this.name = null;
        this.title = null;
        this.id = CompressUUID.createNewId();
    }
    
    public String getId() {
        return this.id;
    }
    
    public SlaveNode withId(final String id) {
        this.id = id;
        return this;
    }
    
    public String getName() {
        return this.name;
    }
    
    public SlaveNode withName(final String n) throws Exception {
        final StringBuilder chkf = new StringBuilder();
        if (!Convert.checkVarName(n, true, chkf)) {
            throw new Exception(chkf.toString());
        }
        this.name = n;
        return this;
    }
    
    public String getTitle() {
        if (Convert.isNotNullEmpty(this.title)) {
            return this.title;
        }
        return this.name;
    }
    
    public SlaveNode withTitle(final String t) {
        this.title = t;
        return this;
    }
}
