package ufg.resources;

import java.util.HashMap;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import ufg.enums.PropertyType;
import ufg.ex.SerializationException;
import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.io.streams.MemoryInputStream;
import ufg.io.streams.MemoryOutputStream;
import ufg.structures.Property;
import ufg.structures.chunks.ResourceData;

public class PropertySet extends ResourceData {
    public static final int BASE_ALLOCATION_SIZE = 0x40 + 0x30;

    public int parentHandle;
    public int flags = 0x01000000;
    public HashMap<Integer, Property> properties = new HashMap<>();

    public PropertySet() {
        typeUID = 0x5B9BF81E;
    }

    @SuppressWarnings("unchecked")
    @Override public PropertySet serialize(Serializer serializer, Serializable structure) {
        PropertySet set = (structure == null) ? new PropertySet() : (PropertySet) structure;

        super.serialize(serializer, set);
        serializer.pad(0xC);
        set.parentHandle = serializer.i32(set.parentHandle);

        int propertyOffset = serializer.getOffset();
        propertyOffset += serializer.i32(0x14);

        int dataOffset = serializer.getOffset();
        dataOffset += serializer.i32(0x10 + (set.properties.size() * 0xc));

        int numProperties = serializer.i32(set.properties.size());
        int numDataBytes = serializer.i32(set.getDataSize());
        set.flags = serializer.i32(set.flags);
        
        serializer.seek(propertyOffset);
        if (serializer.isWriting()) {
            MemoryOutputStream dataStream = new MemoryOutputStream(numDataBytes);
            for (int propertyName : set.properties.keySet()) {
                Property property = set.properties.get(propertyName);

                serializer.enum32(property.type);
                serializer.i32(propertyName);
                serializer.i32(dataStream.getOffset());

                switch (property.type) {
                    case BOOL: dataStream.bool((boolean)property.value); break;
                    case UINT8:
                    case INT8: dataStream.i8((byte)property.value); break;
                    case UINT16:
                    case INT16: dataStream.i16((short)property.value); break;
                    case UINT32:
                    case INT32: dataStream.i32((int)property.value); break;
                    case UINT64:
                    case INT64: dataStream.i64((long)property.value); break;
                    case FLOAT: dataStream.f32((float)property.value); break;
                    case STRING: dataStream.cstr((String)property.value); break;
                    case MATRIX44: dataStream.m44((Matrix4f)property.value); break;
                    case VECTOR2: dataStream.v2((Vector2f)property.value); break;
                    case VECTOR3: dataStream.v3((Vector3f)property.value); break;
                    case VECTOR4: dataStream.v4((Vector4f)property.value); break;
                    default:
                        throw new SerializationException("Unsupported PropertyType in Property! " + property.type.toString());
                }
            }

            serializer.seek(dataOffset);
            serializer.bytes(dataStream.getBuffer(), numDataBytes);
            return set;
        } else {
            serializer.seek(dataOffset);
            MemoryInputStream dataStream = new MemoryInputStream(serializer.getInput().bytes(numDataBytes));
            serializer.seek(propertyOffset);
            MemoryInputStream stream = serializer.getInput();
            for (int i = 0; i < numProperties; ++i) {
                Property property = new Property();
                int v = stream.i32();
                property.type = PropertyType.fromValue(v);
                int name = stream.i32();
                dataStream.seek(stream.i32());
                switch (property.type) {
                    case BOOL: property.value = dataStream.bool(); break;
                    case UINT8:
                    case INT8: property.value = dataStream.i8(); break;
                    case UINT16:
                    case INT16: property.value = dataStream.i16(); break;
                    case UINT32:
                    case INT32: property.value = dataStream.i32(); break;
                    case UINT64:
                    case INT64: property.value = dataStream.i64(); break;
                    case FLOAT: property.value = dataStream.f32(); break;
                    case STRING: property.value = dataStream.cstr(); break;
                    case MATRIX44: property.value = dataStream.m44(); break;
                    case VECTOR2: property.value = dataStream.v2(); break;
                    case VECTOR3: property.value = dataStream.v3(); break;
                    case VECTOR4: property.value = dataStream.v4(); break;
                    default:
                        throw new SerializationException("Unsupported PropertyType in Property! " + property.type.toString());
                }

                set.properties.put(name, property);
            }
        }

        serializer.seek(dataOffset + numDataBytes);

        return set;
    }

    @Override public int getAllocatedSize() {
        int size = PropertySet.BASE_ALLOCATION_SIZE;
        size += getDataSize();
        size += properties.size() * 0xc;
        return size;
    }

    private int getDataSize() {
        int size = 0;
        for (Property property : properties.values()) {
            size += property.type.getSize();
            if (property.type == PropertyType.STRING) {
                if (property.value == null) size += 1;
                else size += (((String)property.value).length());
            }
        }
        return size;
    }
}
