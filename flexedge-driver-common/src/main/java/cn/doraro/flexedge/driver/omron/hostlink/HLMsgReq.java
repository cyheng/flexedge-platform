// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.hostlink;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public abstract class HLMsgReq extends HLMsg {
    private transient int readToCC;

    public HLMsgReq() {
        this.readToCC = 0;
    }

    public final String packToStr() {
        final StringBuilder sb = new StringBuilder();
        sb.append("@");
        sb.append(HLMsg.byte_to_bcd2(this.plcUnit / 10));
        sb.append(this.getHeadCode());
        this.packContent(sb);
        final String fcs = HLMsg.calFCS(sb.toString());
        sb.append(fcs).append("*\r");
        return sb.toString();
    }

    public final String writeTo(final OutputStream outputs) throws IOException {
        final String str = this.packToStr();
        final byte[] bs = str.getBytes();
        outputs.write(bs);
        outputs.flush();
        return str;
    }

    protected abstract void packContent(final StringBuilder p0);

    protected abstract HLMsgResp newRespInstance();

    public HLMsgResp readRespFrom(final InputStream inputs, final OutputStream outputs, final long read_to, final int retry_c) throws Exception {
        final String txt = this.readFrom(inputs, outputs, read_to, retry_c);
        return this.parseFromTxt(txt);
    }

    HLMsgResp parseFromTxt(final String txt) throws Exception {
        final HLMsgResp ret = this.newRespInstance();
        ret.parseFrom(txt);
        return ret;
    }

    private String readFrom(final InputStream inputs, final OutputStream outputs, final long read_to, final int retry_c) throws Exception {
        final String firstpk = this.readRespPk(true, inputs, read_to, retry_c, 2000);
        if (firstpk.endsWith("*")) {
            final int len = firstpk.length();
            final String fcs = HLMsg.calFCS(firstpk.substring(0, len - 3));
            if (fcs.charAt(0) != firstpk.charAt(len - 3) || fcs.charAt(1) != firstpk.charAt(len - 2)) {
                return null;
            }
            return firstpk.substring(0, len - 3);
        } else {
            final ArrayList<String> more_pks = new ArrayList<String>();
            String mstr;
            do {
                outputs.write(13);
                mstr = this.readRespPk(false, inputs, read_to, retry_c, 2000);
                more_pks.add(mstr);
            } while (!mstr.endsWith("*"));
            final StringBuilder sb = new StringBuilder();
            int len2 = firstpk.length();
            String fcs2 = HLMsg.calFCS(firstpk.substring(0, len2 - 2));
            if (fcs2.charAt(0) != firstpk.charAt(len2 - 2) || fcs2.charAt(1) != firstpk.charAt(len2 - 1)) {
                return null;
            }
            sb.append(firstpk.substring(0, len2 - 2));
            final int n = more_pks.size();
            for (int i = 0; i < n - 1; ++i) {
                final String morepk = more_pks.get(i);
                len2 = morepk.length();
                fcs2 = HLMsg.calFCS(morepk.substring(0, len2 - 2));
                if (fcs2.charAt(0) != morepk.charAt(len2 - 2) || fcs2.charAt(1) != morepk.charAt(len2 - 1)) {
                    return null;
                }
                sb.append(morepk.substring(0, len2 - 2));
            }
            final String lastpk = more_pks.get(n - 1);
            len2 = lastpk.length();
            fcs2 = HLMsg.calFCS(lastpk.substring(0, len2 - 3));
            if (fcs2.charAt(0) != lastpk.charAt(len2 - 3) || fcs2.charAt(1) != lastpk.charAt(len2 - 2)) {
                return null;
            }
            sb.append(lastpk.substring(0, len2 - 3));
            return sb.toString();
        }
    }

    private int readTo(final InputStream inputs, final long timeout, final int retry_c) throws HLException, IOException {
        final long st = System.currentTimeMillis();
        while (inputs.available() <= 0) {
            if (System.currentTimeMillis() - st > timeout) {
                ++this.readToCC;
                if (this.readToCC > retry_c) {
                    this.readToCC = 0;
                    throw new HLException(2, "time out " + timeout + "ms. may be this value is too small!");
                }
                throw new HLException(1, "time out " + timeout + "ms. may be this value is too small!");
            } else {
                try {
                    Thread.sleep(1L);
                } catch (final Exception ex) {
                }
            }
        }
        this.readToCC = 0;
        return inputs.read();
    }

    private String readRespPk(final boolean bfirst, final InputStream inputs, final long timeout, final int retry_c, final int max_len) throws Exception {
        boolean in_pk = !bfirst;
        final StringBuilder sb = new StringBuilder();
        while (true) {
            final int c = this.readTo(inputs, timeout, retry_c);
            if (!in_pk) {
                if (c != 64) {
                    continue;
                }
                in_pk = true;
                sb.append((char) c);
            } else {
                if (c == 13) {
                    return sb.toString();
                }
                sb.append((char) c);
                if (sb.length() > max_len) {
                    throw new IOException("read txt len >" + max_len);
                }
                continue;
            }
        }
    }
}
