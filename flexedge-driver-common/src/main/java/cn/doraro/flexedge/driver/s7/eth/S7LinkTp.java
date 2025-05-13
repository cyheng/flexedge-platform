

package cn.doraro.flexedge.driver.s7.eth;

public enum S7LinkTp {
    PG(1),
    OP(2),
    PC(3);

    private final int val;

    private S7LinkTp(final int v) {
        this.val = v;
    }

    public int getVal() {
        return this.val;
    }
}
