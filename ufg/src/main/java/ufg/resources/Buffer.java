package ufg.resources;

import ufg.enums.BufferFlags;
import ufg.enums.BufferType;
import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.structures.chunks.ResourceData;
import ufg.util.Bytes;
import ufg.util.UFGCRC;

public class Buffer extends ResourceData {
    public static final int BASE_ALLOCATION_SIZE = 0x40 + 0xC0;

    public BufferType type = BufferType.VERTEX;
    public byte runtimeCreated;
    public short flags = BufferFlags.NONE;
    public byte[] data;
    public int elementByteSize;
    public int numElements;

    public Buffer() { this.typeUID = UFGCRC.qStringHash32("Illusion.Buffer"); }

    @SuppressWarnings("unchecked")
    @Override public Buffer serialize(Serializer serializer, Serializable structure) {
        Buffer buffer = (structure == null) ? new Buffer() : (Buffer) structure;

        super.serialize(serializer, buffer);
        
        buffer.type = serializer.enum8(buffer.type);
        buffer.runtimeCreated = serializer.i8(buffer.runtimeCreated);
        buffer.flags = serializer.i16(buffer.flags);
        
        int dataSize = serializer.i32(serializer.isWriting() ? buffer.data.length : 0);

        int dataOffset = serializer.getOffset();
        dataOffset += serializer.i32(0x78);

        buffer.elementByteSize = serializer.i32(buffer.elementByteSize);
        buffer.numElements = serializer.i32(buffer.numElements);

        serializer.i32(0x4c); // BufferUser*

        serializer.pad(0x8);

        int memoryPoolOffset = serializer.getOffset();
        memoryPoolOffset += serializer.i32(0x50); // qMemoryPool*

        serializer.seek(memoryPoolOffset + 3);
        serializer.str("BA0", 3);
        serializer.seek(dataOffset);

        
        
        if (serializer.isWriting()) serializer.getOutput().bytes(buffer.data);
        else buffer.data = serializer.getInput().bytes(dataSize);

        serializer.align(0x10);

        return buffer;
    }


    @Override public int getAllocatedSize() {
        return Buffer.BASE_ALLOCATION_SIZE + this.data.length + 0x10;
    }

    public static void main(String[] args) {
        System.out.println(Bytes.toHex(UFGCRC.qStringHash32("iShader")));
    }
}
