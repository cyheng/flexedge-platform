// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common.modbus.sniffer;

public class SnifferBufferFix
{
    private transient int bufLen;
    private transient byte[] dataBuf;
    private transient int firstPos;
    private transient int lastPos;
    
    public SnifferBufferFix(final int buflen) {
        this.bufLen = 1024;
        this.dataBuf = null;
        this.firstPos = 0;
        this.lastPos = 0;
        this.bufLen = buflen;
        this.dataBuf = new byte[this.bufLen];
    }
    
    public void addData(final byte[] bs) throws Exception {
        if (bs == null || bs.length <= 0) {
            return;
        }
        final int leftlen = this.getBufEmptyLen();
        if (leftlen < bs.length) {
            throw new Exception("buffer empty space is not enough");
        }
        synchronized (this) {
            if (this.lastPos >= this.firstPos) {
                final int flowlen = this.bufLen - this.lastPos;
                if (bs.length <= flowlen) {
                    System.arraycopy(bs, 0, this.dataBuf, this.lastPos, bs.length);
                }
                else {
                    System.arraycopy(bs, 0, this.dataBuf, this.lastPos, flowlen);
                    System.arraycopy(bs, flowlen, this.dataBuf, 0, bs.length - flowlen);
                }
            }
            else {
                System.arraycopy(bs, 0, this.dataBuf, this.lastPos, bs.length);
            }
            this.lastPos += bs.length;
            if (this.lastPos >= this.bufLen) {
                this.lastPos -= this.bufLen;
            }
        }
    }
    
    private int getBufEmptyLen() {
        return this.bufLen - this.getBufLen() - 1;
    }
    
    public int getBufLen() {
        final int r = this.lastPos - this.firstPos;
        if (r >= 0) {
            return r;
        }
        return this.bufLen + r;
    }
    
    private boolean readData(final byte[] buf, final int offset, final int len, final boolean remove_readed) {
        final int buflen = this.getBufLen();
        if (buflen < len) {
            return false;
        }
        synchronized (this) {
            if (this.firstPos < this.lastPos) {
                System.arraycopy(this.dataBuf, this.firstPos, buf, offset, len);
            }
            else {
                final int flowlen = this.bufLen - this.firstPos;
                if (flowlen >= len) {
                    System.arraycopy(this.dataBuf, this.firstPos, buf, offset, len);
                }
                else {
                    System.arraycopy(this.dataBuf, this.firstPos, buf, offset, flowlen);
                    System.arraycopy(this.dataBuf, 0, buf, offset + flowlen, len - flowlen);
                }
            }
            if (remove_readed) {
                this.firstPos += len;
                if (this.firstPos >= this.bufLen) {
                    this.firstPos -= this.bufLen;
                }
            }
        }
        return true;
    }
    
    public boolean readData(final byte[] buf, final int offset, final int len) {
        return this.readData(buf, offset, len, true);
    }
    
    public synchronized boolean skipLen(final int len) {
        final int buflen = this.getBufLen();
        if (buflen < len) {
            return false;
        }
        this.firstPos += len;
        if (this.firstPos >= this.bufLen) {
            this.firstPos -= this.bufLen;
        }
        return true;
    }
    
    public synchronized int readNextChar() {
        if (this.firstPos == this.lastPos) {
            return -1;
        }
        final int r = this.dataBuf[this.firstPos] & 0xFF;
        ++this.firstPos;
        if (this.firstPos >= this.bufLen) {
            this.firstPos -= this.bufLen;
        }
        return r;
    }
    
    public boolean peekData(final byte[] buf, final int offset, final int len) {
        return this.readData(buf, offset, len, false);
    }
}
