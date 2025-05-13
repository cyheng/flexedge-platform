

package cn.doraro.flexedge.driver.aromat.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public abstract class AMMsgReq extends AMMsg {
    private transient int readToCC;

    public AMMsgReq() {
        this.readToCC = 0;
    }

    public abstract String getCmdCode();

    public final String packToStr() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.head);
        sb.append(AMMsg.byte_to_bcd2(this.plcAddr));
        sb.append('#');
        sb.append(this.getCmdCode());
        this.packContent(sb);
        final String fcs = AMMsg.calBCC(sb.toString());
        sb.append(fcs).append("\r");
        return sb.toString();
    }

    private String packAckStr() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.head);
        sb.append(AMMsg.byte_to_bcd2(this.plcAddr));
        final String fcs = AMMsg.calBCC(sb.toString());
        sb.append(fcs).append("&\r");
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

    protected abstract AMMsgResp newRespInstance();

    public AMMsgResp readRespFrom(final InputStream inputs, final OutputStream outputs, final long read_to, final int retry_c) throws Exception {
        final String txt = this.readFrom(inputs, outputs, read_to, retry_c);
        return this.parseFromTxt(txt);
    }

    AMMsgResp parseFromTxt(final String txt) throws Exception {
        final AMMsgResp ret = this.newRespInstance();
        ret.parseFrom(txt);
        return ret;
    }

    private String readFrom(final InputStream inputs, final OutputStream outputs, final long read_to, final int retry_c) throws Exception {
        final String firstpk = this.readRespPk(true, inputs, read_to, retry_c);
        if (!firstpk.endsWith("&")) {
            final int len = firstpk.length();
            final String fcs = AMMsg.calBCC(firstpk.substring(0, len - 2));
            if (fcs.charAt(0) != firstpk.charAt(len - 2) || fcs.charAt(1) != firstpk.charAt(len - 1)) {
                return null;
            }
            return firstpk.substring(0, len - 2);
        } else {
            final byte[] ackpk = this.packAckStr().getBytes();
            final ArrayList<String> more_pks = new ArrayList<String>();
            more_pks.add(firstpk);
            String mstr;
            do {
                outputs.write(ackpk);
                mstr = this.readRespPk(false, inputs, read_to, retry_c);
                more_pks.add(mstr);
            } while (mstr.endsWith("&"));
            final StringBuilder sb = new StringBuilder();
            final int n = more_pks.size();
            for (int i = 0; i < n - 1; ++i) {
                final String morepk = more_pks.get(i);
                final int len2 = morepk.length();
                final String fcs2 = AMMsg.calBCC(morepk.substring(0, len2 - 3));
                if (fcs2.charAt(0) != morepk.charAt(len2 - 3) || fcs2.charAt(1) != morepk.charAt(len2 - 2)) {
                    return null;
                }
                sb.append(morepk.substring(0, len2 - 3));
            }
            final String lastpk = more_pks.get(n - 1);
            final int len2 = lastpk.length();
            final String fcs2 = AMMsg.calBCC(lastpk.substring(0, len2 - 2));
            if (fcs2.charAt(0) != lastpk.charAt(len2 - 2) || fcs2.charAt(1) != lastpk.charAt(len2 - 1)) {
                return null;
            }
            sb.append(lastpk.substring(0, len2 - 2));
            return sb.toString();
        }
    }

    private int readTo(final InputStream inputs, final long timeout, final int retry_c) throws AMException, IOException {
        final long st = System.currentTimeMillis();
        while (inputs.available() <= 0) {
            if (System.currentTimeMillis() - st > timeout) {
                ++this.readToCC;
                if (this.readToCC > retry_c) {
                    this.readToCC = 0;
                    throw new AMException(2, "time out " + timeout + "ms. may be this value is too small!");
                }
                throw new AMException(1, "time out " + timeout + "ms. may be this value is too small!");
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

    private String readRespPk(final boolean bfirst, final InputStream inputs, final long timeout, final int retry_c) throws Exception {
        boolean in_pk = !bfirst;
        int max_len = 2048;
        final StringBuilder sb = new StringBuilder();
        while (true) {
            final int c = this.readTo(inputs, timeout, retry_c);
            if (!in_pk) {
                if (c != 37 && c != 60) {
                    continue;
                }
                in_pk = true;
                final char tmph = (char) c;
                max_len = ((tmph == '%') ? 118 : 2048);
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
