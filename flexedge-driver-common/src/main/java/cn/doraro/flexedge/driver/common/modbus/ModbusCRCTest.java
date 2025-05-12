// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common.modbus;

public class ModbusCRCTest
{
    public static String byteArray2HexStr(final byte[] bs, final int offset, final int len) {
        if (bs == null) {
            return null;
        }
        if (bs.length == 0 || len <= 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; ++i) {
            int tmpi = 255;
            tmpi &= bs[i + offset];
            String s = Integer.toHexString(tmpi);
            if (s.length() == 1) {
                s = "0" + s;
            }
            sb.append(s);
        }
        return sb.toString().toUpperCase();
    }
    
    public static void main(final String[] args) throws Exception {
        System.out.println("********** \u8baf\u6e90\u79d1\u6280 Modbus CRC Creator ***********");
        if (args.length <= 0) {
            System.out.println("\u8f93\u5165\u547d\u4ee4\u7684\u683c\u5f0f\u4f8b\u5b50\uff0c16\u8fdb\u5236\u8868\u793a\u8f93\u5165\u503c\u7528\"\"\u5305\u542b\uff0c\u5e76\u4e14\u7528\u7a7a\u683c\u5206\u5f00");
            System.out.println("\u5982\u4f8b\u5b50  mcrc \"01 03 02 05\"");
            return;
        }
        final String[] ss = args[0].split(" ");
        final byte[] bs = new byte[ss.length + 3];
        for (int i = 0; i < ss.length; ++i) {
            bs[i] = (byte)Integer.parseInt(ss[i], 16);
        }
        System.out.println("\u4f60\u7684\u8f93\u5165\u503c:" + byteArray2HexStr(bs, 0, ss.length));
        ModbusCmd.addCRC(bs, ss.length);
        System.out.println("\u5e262\u5b57\u8282CRC:" + byteArray2HexStr(bs, 0, ss.length + 2).toUpperCase());
    }
}
