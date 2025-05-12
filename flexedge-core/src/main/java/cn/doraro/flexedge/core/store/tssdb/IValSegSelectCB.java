package cn.doraro.flexedge.core.store.tssdb;

public interface IValSegSelectCB<T> {
    public void onFindValSeg(int idx, TSSValSeg<T> vs);
}

