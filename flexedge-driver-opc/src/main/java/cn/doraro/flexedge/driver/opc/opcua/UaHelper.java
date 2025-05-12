// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.opc.opcua;

import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import cn.doraro.flexedge.core.UAVal;

public class UaHelper
{
    public static NodeId transValTp2UaTp(final UAVal.ValTP vt) {
        switch (vt) {
            case vt_bool: {
                return Identifiers.Boolean;
            }
            case vt_byte: {
                return Identifiers.SByte;
            }
            case vt_char: {
                return Identifiers.Int16;
            }
            case vt_int16: {
                return Identifiers.Int16;
            }
            case vt_int32: {
                return Identifiers.Int32;
            }
            case vt_int64: {
                return Identifiers.Int64;
            }
            case vt_float: {
                return Identifiers.Float;
            }
            case vt_double: {
                return Identifiers.Double;
            }
            case vt_str: {
                return Identifiers.String;
            }
            case vt_date: {
                return Identifiers.DateTime;
            }
            case vt_uint16: {
                return Identifiers.UInt16;
            }
            case vt_uint32: {
                return Identifiers.UInt32;
            }
            case vt_uint64: {
                return Identifiers.UInt64;
            }
            default: {
                return null;
            }
        }
    }
}
