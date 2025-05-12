// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.opc.opcua.server.types;

import org.eclipse.milo.opcua.stack.core.UaSerializationException;
import org.eclipse.milo.opcua.stack.core.serialization.UaEncoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaDecoder;
import org.eclipse.milo.opcua.stack.core.serialization.SerializationContext;
import org.eclipse.milo.opcua.stack.core.serialization.codecs.GenericDataTypeCodec;
import javax.annotation.Nullable;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.serialization.UaEnumeration;

public enum CustomEnumType implements UaEnumeration
{
    Field0("Field0", 0, 0), 
    Field1("Field1", 1, 1), 
    Field2("Field2", 2, 2);
    
    public static final ExpandedNodeId TYPE_ID;
    private final int value;
    
    static {
        TYPE_ID = ExpandedNodeId.parse(String.format("nsu=%s;s=%s", "urn:eclipse:milo:hello-world", "DataType.CustomEnumType"));
    }
    
    private CustomEnumType(final String name, final int ordinal, final int value) {
        this.value = value;
    }
    
    public int getValue() {
        return this.value;
    }
    
    @Nullable
    public static CustomEnumType from(final int value) {
        switch (value) {
            case 0: {
                return CustomEnumType.Field0;
            }
            case 1: {
                return CustomEnumType.Field1;
            }
            case 2: {
                return CustomEnumType.Field2;
            }
            default: {
                return null;
            }
        }
    }
    
    public static class Codec extends GenericDataTypeCodec<CustomEnumType>
    {
        public Class<CustomEnumType> getType() {
            return CustomEnumType.class;
        }
        
        public CustomEnumType decode(final SerializationContext context, final UaDecoder decoder) {
            return CustomEnumType.from(decoder.readInt32((String)null));
        }
        
        public void encode(final SerializationContext context, final UaEncoder encoder, final CustomEnumType value) {
            encoder.writeInt32((String)null, Integer.valueOf(value.getValue()));
        }
    }
}
