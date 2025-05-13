

package cn.doraro.flexedge.driver.common.modbus.sniffer;

import java.util.LinkedList;

public class SnifferBuffer {
    private transient LinkedList<byte[]> bsList;
    private transient int firstPos;

    public SnifferBuffer() {
        this.bsList = new LinkedList<byte[]>();
        this.firstPos = 0;
    }

    public void addData(final byte[] bs) {
        if (bs == null || bs.length <= 0) {
            return;
        }
        synchronized (this) {
            this.bsList.addLast(bs);
        }
    }

    public int getBufLen() {
        int r = 0;
        for (final byte[] bs : this.bsList) {
            r += bs.length;
        }
        return r - this.firstPos;
    }

    private boolean readData(final byte[] buf, int offset, int len, final boolean remove_readed) {
        final int buflen = this.getBufLen();
        if (buflen < len) {
            return false;
        }
        int fpos = this.firstPos;
        int ln;
        int i;
        for (ln = this.bsList.size(), i = 0; i < ln; ++i) {
            if (len == 0) {
                break;
            }
            final byte[] bs = this.bsList.get(i);
            final int rlen = bs.length - fpos;
            if (rlen > len) {
                if (buf != null) {
                    System.arraycopy(bs, fpos, buf, offset, len);
                }
                fpos += len;
                break;
            }
            if (buf != null) {
                System.arraycopy(bs, fpos, buf, offset, rlen);
            }
            fpos = 0;
            offset += rlen;
            len -= rlen;
        }
        if (remove_readed) {
            synchronized (this) {
                for (int j = 0; j < i; ++j) {
                    this.bsList.removeFirst();
                }
                this.firstPos = fpos;
            }
        }
        return true;
    }

    public boolean readData(final byte[] buf, final int offset, final int len) {
        return this.readData(buf, offset, len, true);
    }

    public boolean skipLen(final int len) {
        return this.readData(null, 0, len, true);
    }

    public int readNextChar() {
        if (this.bsList.size() <= 0) {
            return -1;
        }
        final byte[] bs = this.bsList.getFirst();
        final byte b = bs[this.firstPos];
        synchronized (this) {
            ++this.firstPos;
            if (this.firstPos == bs.length) {
                this.bsList.removeFirst();
                this.firstPos = 0;
            }
        }
        return b & 0xFF;
    }

    public boolean peekData(final byte[] buf, final int offset, final int len) {
        return this.readData(buf, offset, len, false);
    }
}
