// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

import cn.doraro.flexedge.core.dict.DataNode;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.Lan;
import java.util.Iterator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MCCode
{
    String title;
    String symbol;
    TP tp;
    Sign sign;
    String asciiVal_Q_L;
    Integer binVal_Q_L;
    String asciiVal_iQ_R;
    Integer binVal_iQ_R;
    boolean supportBit;
    public static final List<String> SYMBOLS;
    private static HashMap<String, MCCode> SYM2CODE;
    
    static {
        SYMBOLS = Arrays.asList("SM", "SD", "X", "Y", "M", "L", "F", "V", "B", "D", "W", "TS", "TC", "TN", "LTS", "LTC", "LTN", "STS", "STC", "STN", "LSTS", "LSTC", "LSTN", "CS", "CC", "CN", "LCS", "LCC", "LCN", "SB", "SW", "DX", "DY", "Z", "LZ", "R", "ZR", "D", "W", "RD");
        MCCode.SYM2CODE = new HashMap<String, MCCode>();
        for (final String sym : MCCode.SYMBOLS) {
            final MCCode cc = readCode(sym);
            if (cc == null) {
                continue;
            }
            MCCode.SYM2CODE.put(sym, cc);
        }
    }
    
    private MCCode() {
        this.supportBit = false;
    }
    
    public boolean isBitTp() {
        return this.tp == TP.bit;
    }
    
    private static MCCode readCode(final String n) {
        final Lan lan = Lan.getLangInPk((Class)MCCode.class);
        final DataNode dn = lan.gn("deftp_" + n);
        if (dn == null) {
            return null;
        }
        final MCCode ret = new MCCode();
        ret.title = dn.getNameByLang(Lan.getUsingLang());
        ret.symbol = dn.getAttr("symbol");
        ret.tp = TP.valueOf(dn.getAttr("tp"));
        ret.sign = Sign.valueOf(dn.getAttr("sign"));
        ret.asciiVal_Q_L = dn.getAttr("q_l_ascii");
        String str = dn.getAttr("q_l_bin");
        if (Convert.isNotNullEmpty(str)) {
            if (str.startsWith("0x")) {
                str = str.substring(2);
            }
            ret.binVal_Q_L = Integer.parseInt(str, 16);
        }
        ret.asciiVal_iQ_R = dn.getAttr("iq_r_ascii");
        str = dn.getAttr("iq_r_bin");
        if (str.startsWith("0x")) {
            str = str.substring(2);
        }
        ret.binVal_iQ_R = Integer.parseInt(str, 16);
        ret.supportBit = "true".equals(dn.getAttr("bit_pos"));
        return ret;
    }
    
    public static MCCode getCodeBySymbol(final String sym) {
        return MCCode.SYM2CODE.get(sym);
    }
    
    @Override
    public String toString() {
        return String.valueOf(this.title) + " " + this.symbol + ":" + this.tp;
    }
    
    public enum Sign
    {
        dec("dec", 0), 
        hex("hex", 1);
        
        private Sign(final String name, final int ordinal) {
        }
    }
    
    public enum TP
    {
        bit("bit", 0), 
        word("word", 1);
        
        private TP(final String name, final int ordinal) {
        }
    }
}
