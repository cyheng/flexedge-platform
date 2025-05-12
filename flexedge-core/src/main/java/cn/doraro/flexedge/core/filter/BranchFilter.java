package cn.doraro.flexedge.core.filter;

import cn.doraro.flexedge.core.UANodeOCTagsGCxt;

public class BranchFilter implements ICFilter {

    @Override
    public boolean acceptNodeCxt(UANodeOCTagsGCxt nodecxt) {
        return false;
    }

}
