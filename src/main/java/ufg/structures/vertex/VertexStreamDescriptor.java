package ufg.structures.vertex;

import org.joml.Vector3f;
import org.joml.Vector4f;

import ufg.io.streams.MemoryInputStream;
import ufg.io.streams.MemoryOutputStream;
import ufg.io.streams.MemoryInputStream.SeekMode;
import ufg.util.Bytes;
import ufg.util.UFGCRC;
import ufg.enums.BufferType;
import ufg.enums.VertexStreamElementType;
import ufg.enums.VertexStreamElementUsage;
import ufg.resources.Buffer;

public class VertexStreamDescriptor {
    
    public class VertexStreamElement {
        public VertexStreamElementUsage usage;
        public VertexStreamElementType type;
        public int stream;
        public int offset;
        public int size;

        public VertexStreamElement(VertexStreamElementUsage usage, VertexStreamElementType type, int stream, int offset) {
            this.usage = usage;
            this.type = type;
            this.stream = stream;
            this.offset = offset;

            switch (type) {
                case FLOAT3: this.size = 0xc; break;
                case FLOAT4: this.size = 0x10; break;
                case HALF2:
                case UBYTE4:
                case COLOR4:
                case UBYTE4N:
                case I11_11_10N:
                case SINT16_2:
                case UINT1:
                case UBYTE4_DELTA:
                case BYTE4N:
                    this.size = 4; break;
                case HALF4:
                case SHORT4:
                case SHORT4N:
                case SHORT4_FIXED4_12:
                case SHORT4_FIXED8_8:
                    this.size = 8;
                    break;
                case HALF3:
                    this.size = 6;
                    break;
                default:
                    this.size = 0;
            }
        }
    }

    private final String name;
    private final int nameUID;
    
    private VertexStreamElement[] elements = new VertexStreamElement[16];
    private int[] streamSizes = new int[4];

    public VertexStreamDescriptor(String name, int nameUID) {
        this.name = name;
        this.nameUID = nameUID;
    }

    public String getName() { return this.name; }
    public int getNameUID() { return this.nameUID; }

    public Buffer[] create(String name, int vertexCount) {
        Buffer[] buffers = new Buffer[4];
        for (int i = 0; i < 4; ++i) {
            if (this.streamSizes[i] == 0) continue;

            Buffer buffer = new Buffer();
            String bufferName = name + ".VertexBuffer." + this.name + ".Stream" + (i);

            buffer.name = bufferName;
            if (bufferName.length() > 0x23)
                buffer.name = bufferName.substring(0, 0x23);
            
            buffer.UID = UFGCRC.qStringHash32(bufferName);
            
            buffer.elementByteSize = this.streamSizes[i];
            buffer.numElements = vertexCount;
            buffer.type = BufferType.VERTEX;
            
            buffer.data = new byte[buffer.elementByteSize * buffer.numElements];

            buffers[i] = buffer;
        }

        return buffers;
    }

    public void set(Buffer[] buffers, VertexStreamElementUsage usage, Vector4f[] data) {
        VertexStreamElement element = this.elements[usage.getIndex()];
        if (element == null) return;
        Buffer buffer = buffers[element.stream];
        if (buffer == null) return;

        int streamSize = this.streamSizes[element.stream];
        MemoryOutputStream stream = new MemoryOutputStream(buffer.data);
        for (int i = 0; i < buffer.numElements; ++i) {
            stream.seek((i * streamSize) + element.offset, SeekMode.Begin);
            Vector4f value = data[i];
            switch (element.type) {
                case FLOAT3: {
                    stream.f32(value.x);
                    stream.f32(value.y);
                    stream.f32(value.z);
                    break;
                }
                case FLOAT4: {
                    stream.f32(value.x);
                    stream.f32(value.y);
                    stream.f32(value.z);
                    stream.f32(value.w);
                    break;
                }
                case HALF2: {
                    stream.f16(value.x);
                    stream.f16(value.y);
                    break;
                }
                case UBYTE4: case UBYTE4_DELTA: {
                    stream.u8(Math.round(value.x) & 0xff);
                    stream.u8(Math.round(value.y) & 0xff);
                    stream.u8(Math.round(value.z) & 0xff);
                    stream.u8(Math.round(value.w) & 0xff);
                    break;
                }
                case COLOR4: case UBYTE4N: {
                    stream.u8((int) Math.round(value.x * 0xff));
                    stream.u8((int) Math.round(value.y * 0xff));
                    stream.u8((int) Math.round(value.z * 0xff));
                    stream.u8((int) Math.round(value.w * 0xff));
                    break;
                }
                case I11_11_10N: {
                    stream.u32(Bytes.packNormal32(new Vector3f(value.x, value.y, value.z)));
                    break;
                }
                case SINT16_2: {
                    stream.i16((short) Math.round(value.x));
                    stream.i16((short) Math.round(value.y));
                    break;
                }
                case UINT1: {
                    stream.u32((int) value.x);
                    break;
                }
                case BYTE4N: {
                    stream.i8((byte) Math.round(value.x * 0x7f));
                    stream.i8((byte) Math.round(value.y * 0x7f));
                    stream.i8((byte) Math.round(value.z * 0x7f));
                    stream.i8((byte) Math.round(value.w * 0x7f));
                    break;
                }
                case HALF4: {
                    stream.f16(value.x);
                    stream.f16(value.y);
                    stream.f16(value.z);
                    stream.f16(value.w);
                    break;
                }
                case SHORT4: {
                    stream.i16((short) Math.round(value.x));
                    stream.i16((short) Math.round(value.y));
                    stream.i16((short) Math.round(value.z));
                    stream.i16((short) Math.round(value.w));
                    break;
                }
                case SHORT4N: {
                    stream.i16((short) Math.round(value.x * 0x7FFF));
                    stream.i16((short) Math.round(value.y * 0x7FFF));
                    stream.i16((short) Math.round(value.z * 0x7FFF));
                    stream.i16((short) Math.round(value.w * 0x7FFF));
                    break;
                }
                case SHORT4_FIXED4_12: { 
                    stream.i16((short) Math.round(value.x * (1 << 12)));
                    stream.i16((short) Math.round(value.y * (1 << 12)));
                    stream.i16((short) Math.round(value.z * (1 << 12)));
                    stream.i16((short) Math.round(value.w * (1 << 12)));
                    break;
                }
                case SHORT4_FIXED8_8: {
                    stream.i16((short) Math.round(value.x * (1 << 8)));
                    stream.i16((short) Math.round(value.y * (1 << 8)));
                    stream.i16((short) Math.round(value.z * (1 << 8)));
                    stream.i16((short) Math.round(value.w * (1 << 8)));
                    break;
                }
                case HALF3: {
                    stream.f16(value.x);
                    stream.f16(value.y);
                    stream.f16(value.z);
                    break;
                }
            }
        }
    }

    public Vector4f[] get(Buffer[] buffers, VertexStreamElementUsage usage) {
        VertexStreamElement element = this.elements[usage.getIndex()];
        if (element == null) return null;
        Buffer buffer = buffers[element.stream];
        if (buffer == null) return null;

        int streamSize = this.streamSizes[element.stream];
        MemoryInputStream stream = new MemoryInputStream(buffer.data);
        Vector4f[] elements = new Vector4f[buffer.numElements];
        for (int i = 0; i < buffer.numElements; ++i) {
            stream.seek((i * streamSize) + element.offset, SeekMode.Begin);
            Vector4f v = new Vector4f();
            switch (element.type) {
                case FLOAT3: {
                    v.x = stream.f32();
                    v.y = stream.f32();
                    v.z = stream.f32();
                    break;
                }
                case FLOAT4: {
                    v.x = stream.f32();
                    v.y = stream.f32();
                    v.z = stream.f32();
                    v.w = stream.f32();
                    break;
                }
                case HALF2: {
                    v.x = stream.f16();
                    v.y = stream.f16();
                    break;
                }
                case UBYTE4: case UBYTE4_DELTA: {
                    v.x = stream.u8();
                    v.y = stream.u8();
                    v.z = stream.u8();
                    v.w = stream.u8();
                    break;
                }
                case COLOR4: case UBYTE4N: {
                    v.x = ((float) ((int)stream.u8() & 0xff)) / 0xFF;
                    v.y = ((float) ((int)stream.u8() & 0xff)) / 0xFF;
                    v.z = ((float) ((int)stream.u8() & 0xff)) / 0xFF;
                    v.w = ((float) ((int)stream.u8() & 0xff)) / 0xFF;
                    break;
                }
                case I11_11_10N: {
                    Vector3f normal = Bytes.unpackNormal32(stream.u32());
                    v = new Vector4f(normal, 1.0f);
                    break;
                }
                case SINT16_2: {
                    v.x = stream.i16();
                    v.y = stream.i16();
                    break;
                }
                case UINT1: {
                    v.x = stream.u32();
                    break;
                }
                case BYTE4N: {
                    v.x = ((float) (stream.i8())) / 0x7f;
                    v.y = ((float) (stream.i8())) / 0x7f;
                    v.z = ((float) (stream.i8())) / 0x7f;
                    v.w = ((float) (stream.i8())) / 0x7f;
                    break;
                }
                case HALF4: {
                    v.x = stream.f16();
                    v.y = stream.f16();
                    v.z = stream.f16();
                    v.w = stream.f16();
                    break;
                }
                case SHORT4: {
                    v.x = stream.i16();
                    v.y = stream.i16();
                    v.z = stream.i16();
                    v.w = stream.i16();
                    break;
                }
                case SHORT4N: {
                    v.x = ((float) stream.i16()) / 0x7FFF;
                    v.y = ((float) stream.i16()) / 0x7FFF;
                    v.z = ((float) stream.i16()) / 0x7FFF;
                    v.w = ((float) stream.i16()) / 0x7FFF;
                    break;
                }
                case SHORT4_FIXED4_12: { 
                    v.x = ((float) stream.i16()) / (1 << 12);
                    v.y = ((float) stream.i16()) / (1 << 12);
                    v.z = ((float) stream.i16()) / (1 << 12);
                    v.w = ((float) stream.i16()) / (1 << 12);
                    break;
                }
                case SHORT4_FIXED8_8: {
                    v.x = ((float) stream.i16()) / (1 << 8);
                    v.y = ((float) stream.i16()) / (1 << 8);
                    v.z = ((float) stream.i16()) / (1 << 8);
                    v.w = ((float) stream.i16()) / (1 << 8);
                    break;
                }
                case HALF3: {
                    v.x = stream.f16();
                    v.y = stream.f16();
                    v.z = stream.f16();
                    break;
                }
            }
            elements[i] = v;
        }

        return elements;
    }

    public boolean hasElement(VertexStreamElementUsage usage) {
        return this.elements[usage.getIndex()] != null;
    }

    public void addElement(VertexStreamElementUsage usage, VertexStreamElementType type, int stream) {
        if (stream < 0 || stream > 3)
            throw new IllegalArgumentException("Only streams 0->3 are supported!");
        if (this.elements[usage.getIndex()] != null)
            throw new IllegalArgumentException("Usage index is already in-use by this stream!");
        
        VertexStreamElement element = new VertexStreamElement(usage, type, stream, this.streamSizes[stream]);

        this.elements[usage.getIndex()] = element;
        this.streamSizes[stream] += element.size;
    }

}
