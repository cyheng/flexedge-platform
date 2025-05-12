// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.eth;

import cn.doraro.flexedge.core.util.logger.LoggerManager;
import java.io.IOException;
import cn.doraro.flexedge.core.util.logger.ILogger;

public abstract class S7Msg
{
    static final ILogger log;
    public static final int RESULT_ADDRESS_OUT_OF_RANGE = 5;
    public static final int RESULT_CANNOT_EVALUATE_PDU = -123;
    public static final int RESULT_CPU_RETURNED_NO_DATA = -124;
    public static final int RESULT_EMPTY_RESULT_ERROR = -126;
    public static final int RESULT_EMPTY_RESULT_SET_ERROR = -127;
    public static final int RESULT_ITEM_NOT_AVAILABLE = 10;
    public static final int RESULT_ITEM_NOT_AVAILABLE200 = 3;
    public static final int RESULT_MULTIPLE_BITS_NOT_SUPPORTED = 6;
    public static final int RESULT_NO_PERIPHERAL_AT_ADDRESS = 1;
    public static final int RESULT_OK = 0;
    public static final int RESULT_SHORT_PACKET = -1024;
    public static final int RESULT_TIMEOUT = -1025;
    public static final int RESULT_UNEXPECTED_FUNC = -128;
    public static final int RESULT_UNKNOWN_DATA_UNIT_SIZE = -129;
    public static final int RESULT_UNKNOWN_ERROR = -125;
    public static final int RESULT_WRITE_DATA_SIZE_MISMATCH = 7;
    protected static final int ISO_HEAD_LEN = 7;
    protected static final int PDU_MIN_LEN = 16;
    protected static final int PDU_DEFAULT_LEN = 480;
    protected static final int PDU_MAX_LEN = 487;
    protected static final byte WL_BYTE = 2;
    protected static final byte WL_COUNTER = 28;
    protected static final byte WL_TIMER = 29;
    protected static final byte[] RW35;
    protected static final int RW_LEN = 35;
    protected S7EthDriver driver;
    protected long scanIntervalMS;
    private transient long lastRunT;
    
    public S7Msg() {
        this.driver = null;
        this.scanIntervalMS = 100L;
        this.lastRunT = -1L;
    }
    
    public static String getResultDesc(final int code) {
        switch (code) {
            case 0: {
                return "ok";
            }
            case 6: {
                return "the CPU does not support reading a bit block of length<>1";
            }
            case 10: {
                return "the desired item is not available in the PLC";
            }
            case 3: {
                return "the desired item is not available in the PLC (200 family)";
            }
            case 5: {
                return "the desired address is beyond limit for this PLC";
            }
            case -124: {
                return "the PLC returned a packet with no result data";
            }
            case -125: {
                return "the PLC returned an error code not understood by this library";
            }
            case -126: {
                return "this result contains no data";
            }
            case -127: {
                return "cannot work with an undefined result set";
            }
            case -123: {
                return "cannot evaluate the received PDU";
            }
            case 7: {
                return "Write data size error";
            }
            case 1: {
                return "No data from I/O module";
            }
            case -128: {
                return "Unexpected function code in answer";
            }
            case -129: {
                return "PLC responds wit an unknown data type";
            }
            case -1024: {
                return "Short packet from PLC";
            }
            case -1025: {
                return "Timeout when waiting for PLC response";
            }
            case 32768: {
                return "function already occupied.";
            }
            case 32769: {
                return "not allowed in current operating status.";
            }
            case 33025: {
                return "hardware fault.";
            }
            case 33027: {
                return "object access not allowed.";
            }
            case 33028: {
                return "context is not supported.";
            }
            case 33029: {
                return "invalid address.";
            }
            case 33030: {
                return "data type not supported.";
            }
            case 33031: {
                return "data type not consistent.";
            }
            case 33034: {
                return "object does not exist.";
            }
            case 34048: {
                return "incorrect PDU size.";
            }
            case 34562: {
                return "address invalid.";
            }
            case 53761: {
                return "block name syntax error.";
            }
            case 53762: {
                return "syntax error function parameter.";
            }
            case 53763: {
                return "syntax error block type.";
            }
            case 53764: {
                return "no linked block in storage medium.";
            }
            case 53765: {
                return "object already exists.";
            }
            case 53766: {
                return "object already exists.";
            }
            case 53767: {
                return "block exists in EPROM.";
            }
            case 53769: {
                return "block does not exist.";
            }
            case 53774: {
                return "no block does not exist.";
            }
            case 53776: {
                return "block number too big.";
            }
            case 53824: {
                return "unfinished block transfer in progress?";
            }
            case 53825: {
                return "protected by password.";
            }
            default: {
                return "no message defined for code: " + code + "!";
            }
        }
    }
    
    protected static int recvIsoPacket(final S7TcpConn conn) throws IOException, S7Exception {
        int size = 0;
        while (true) {
            conn.recv(conn.PDU, 0, 4);
            size = S7Util.getUInt16(conn.PDU, 2);
            if (size != 7) {
                break;
            }
            conn.recv(conn.PDU, 4, 3);
        }
        if (size > 487 || size < 16) {
            throw new S7Exception("invalid conn.PDU");
        }
        conn.recv(conn.PDU, 4, 3);
        conn.pduType = conn.PDU[5];
        conn.recv(conn.PDU, 7, size - 7);
        return size;
    }
    
    public abstract void processByConn(final S7TcpConn p0) throws S7Exception, IOException;
    
    void init(final S7EthDriver drv) {
        this.driver = drv;
    }
    
    public S7Msg withScanIntervalMS(final long ms) {
        this.scanIntervalMS = ms;
        return this;
    }
    
    public boolean tickCanRun() {
        final long ct = System.currentTimeMillis();
        if (ct - this.lastRunT > this.scanIntervalMS) {
            this.lastRunT = ct;
            return true;
        }
        return false;
    }
    
    static {
        log = LoggerManager.getLogger((Class)S7Msg.class);
        RW35 = new byte[] { 3, 0, 0, 31, 2, -16, -128, 50, 1, 0, 0, 5, 0, 0, 14, 0, 0, 4, 1, 18, 10, 16, 2, 0, 0, 0, 0, -124, 0, 0, 0, 0, 4, 0, 0 };
    }
}
