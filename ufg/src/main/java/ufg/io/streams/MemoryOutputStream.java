package ufg.io.streams;

import java.util.Arrays;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import ufg.io.ValueEnum;
import ufg.io.streams.MemoryInputStream.SeekMode;
import ufg.util.Bytes;

/**
 * Big-endian binary output stream.
 */
public class MemoryOutputStream {
    private byte[] buffer;

    private int offset = 0;
    private int length;

    private boolean isLittleEndian = false;

    /**
     * Creates a memory output stream with specified size.
     * @param size Size of stream
     */
    public MemoryOutputStream(int size) {
        this.length = size;
        this.buffer = new byte[size];
    }

    /**
     * Creates a memory output stream from an existing buffer.
     * @param data
     */
    public MemoryOutputStream(byte[] data) {
        this.buffer = data;
        this.length = data.length;
    }

    /**
     * Writes an arbitrary number of bytes to the stream.
     * @param value Bytes to write
     * @return This output stream
     */
    public final MemoryOutputStream bytes(byte[] value) {
        System.arraycopy(value, 0, this.buffer, this.offset, value.length);
        this.offset += value.length;
        return this;
    }

    /**
     * Writes a byte array to the stream.
     * @param value Bytes to write
     * @return This output stream
     */
    public final MemoryOutputStream bytearray(byte[] value) {
        this.i32(value.length);
        return this.bytes(value);
    }

    /**
     * Writes a boolean to the stream.
     * @param value Boolean to write
     * @return This output stream
     */
    public final MemoryOutputStream bool(boolean value) {
        return this.u8(value == true ? 1 : 0);
    }
    
    /**
     * Writes a byte to the stream.
     * @param value Byte to write
     * @return This output stream
     */
    public final MemoryOutputStream i8(byte value) {
        this.buffer[this.offset++] = value;
        return this;
    }

    /**
     * Writes an integer to the stream as a byte.
     * @param value Byte to write
     * @return This output stream
     */
    public final MemoryOutputStream u8(int value) {
        this.buffer[this.offset++] = (byte) (value & 0xFF);
        return this;
    }

    /**
     * Writes a short to the stream.
     * @param value Short to write
     * @return This output stream
     */
    public final MemoryOutputStream i16(short value) {
        if (this.isLittleEndian)
            return this.bytes(Bytes.toBytesLE(value));
        return this.bytes(Bytes.toBytesBE(value));
    }

    /**
     * Writes an integer to the stream as an unsigned short.
     * @param value Short to write
     * @return This output stream
     */
    public final MemoryOutputStream u16(int value) {
        return this.i16((short) (value & 0xFFFF));
    }

    /**
     * Writes a 24-bit unsigned integer to the stream.
     * @param value Integer to write
     * @return This output stream
     */
    public final MemoryOutputStream u24(int value) {
        value &= 0xFFFFFF;
        byte[] b;
        if (this.isLittleEndian) {
            b = new byte[] {
                (byte) (value & 0xFF),
                (byte) (value >>> 8),
                (byte) (value >>> 16),
            };
        } else {
            b = new byte[] {
                (byte) (value >>> 16), 
                (byte) (value >>> 8), 
                (byte) (value & 0xFF)
            };
        }
        return this.bytes(b);
    }

    /**
     * Writes a 32-bit integer to the stream.
     * @param value Integer to write
     * @return This output stream
     */
    public final MemoryOutputStream i32(int value) {
        if (this.isLittleEndian)
            return this.bytes(Bytes.toBytesLE(value));
        return this.bytes(Bytes.toBytesBE(value));
    }

    /**
     * Writes a long as a 32-bit integer to the stream.
     * @param value Integer to write
     * @return This output stream
     */
    public final MemoryOutputStream u32(long value) {
        if (this.isLittleEndian)
            return this.bytes(Bytes.toBytesLE((int) (value & 0xFFFFFFFF)));
        return this.bytes(Bytes.toBytesBE((int) (value & 0xFFFFFFFF)));
    }

    /**
     * Writes a long to the stream.
     * @param value Long to write
     * @return This output stream
     */
    public final MemoryOutputStream i64(long value) {
        if (this.isLittleEndian) {
            return this.bytes(new byte[] {
                (byte) (value),
                (byte) (value >>> 8),
                (byte) (value >>> 16),
                (byte) (value >>> 24),
                (byte) (value >>> 32),
                (byte) (value >>> 40),
                (byte) (value >>> 48),
                (byte) (value >>> 56),
            });
        }
        return this.bytes(new byte[] {
            (byte) (value >>> 56),
            (byte) (value >>> 48),
            (byte) (value >>> 40),
            (byte) (value >>> 32),
            (byte) (value >>> 24),
            (byte) (value >>> 16),
            (byte) (value >>> 8),
            (byte) (value)
        });
    }

    /**
     * Writes a 16 bit floating point number to the stream.
     * https://stackoverflow.com/questions/6162651/half-precision-floating-point-in-java
     * @param value Float to write
     * @return This output stream
     */
    public final MemoryOutputStream f16(float value) {
        int fbits = Float.floatToIntBits(value);
        int sign = fbits >>> 16 & 0x8000;
        int val = (fbits & 0x7fffffff) + 0x1000;

        if (val >= 0x47800000) {
            if ((fbits & 0x7fffffff) >= 0x47800000) {
                if (val < 0x7f800000)
                    return this.u16(sign | 0x7c00);
                return this.u16(sign | 0x7c00 | (fbits & 0x007fffff) >>> 13);
            }
            return this.u16(sign | 0x7bff);
        }

        if (val >= 0x38800000)
            return this.u16(sign | val - 0x38000000 >>> 13);
        if (val < 0x33000000)
            return this.u16(sign);
        val = (fbits & 0x7fffffff) >>> 23;
        return this.u16(sign | ((fbits & 0x7fffff | 0x800000) + (0x800000 >>> val - 102) >>> 126 - val));
    }

    /**
     * Writes a 32 bit floating point number to the stream.
     * @param value Float to write
     * @return This output stream
     */
    public final MemoryOutputStream f32(float value) {
        return this.i32(Float.floatToIntBits(value));
    }

    /**
     * Writes a 2-dimensional floating point vector to the stream.
     * @param value Vector2f to write
     * @return This output stream
     */
    public final MemoryOutputStream v2(Vector2f value) { 
        if (value == null) value = new Vector2f().zero();
        this.f32(value.x); 
        this.f32(value.y); 
        return this;
    }
    
    /**
     * Writes a 3-dimensional floating point vector to the stream.
     * @param value Vector3f to write
     * @return This output stream
     */
    public final MemoryOutputStream v3(Vector3f value) {
        if (value == null) value = new Vector3f().zero();
        this.f32(value.x);
        this.f32(value.y);
        this.f32(value.z);
        return this;
    }

    /**
     * Writes a 4-dimensional floating point vector to the stream.
     * @param value Vector4f to write
     * @return This output stream
     */
    public final MemoryOutputStream v4(Vector4f value) {
        if (value == null) value = new Vector4f().zero();
        this.f32(value.x);
        this.f32(value.y);
        this.f32(value.z);
        this.f32(value.w);
        return this;
    }

    /**
     * Writes a Matrix4x4 to the stream.
     * @param value Matrix4x4 to write
     * @return This output stream
     */
    public final MemoryOutputStream m44(Matrix4f value) {
        if (value == null) value = new Matrix4f().identity();

        float[] values = new float[16];
        value.get(values);
        for (int i = 0; i < 16; ++i)
            this.f32(values[i]);
        
        return this;
    }

    /**
     * Aligns current stream offset to a boundary.
     * @param boundary Alignment boundary
     * @return This output stream
     */
    public final MemoryOutputStream align(int boundary) {
        if (this.offset % boundary != 0)
            this.offset += (boundary - (this.offset % boundary));
        return this;
    }

    /**
     * Wrties a null terminated c-string to the stream.
     * @param value String to write
     * @return This output stream
     */
    public final MemoryOutputStream cstr(String value) {
        if (value == null) return this.bytes(new byte[] { 0x00 });
        return this.str(value, value.length() + 1);
    }

    /**
     * Writes a string of fixed size to the stream.
     * @param value String to write
     * @param size Fixed size of string
     * @return This output stream
     */
    public final MemoryOutputStream str(String value, int size) {
        if (value == null) return this.bytes(new byte[size]);
        this.bytes(value.getBytes());
        this.pad(size - value.length());
        return this;
    }

    /**
     * Writes an 8-bit enum value to the stream.
     * @param <T> Type of enum
     * @param value Enum value
     * @return This output stream
     */
    public final <T extends Enum<T> & ValueEnum<Byte>> MemoryOutputStream enum8(T value) {
        if (value == null) return this.u8(0);
        return this.i8(value.getValue().byteValue());
    }

    /**
     * Writes an 32-bit enum value to the stream.
     * @param <T> Type of enum
     * @param value Enum value
     * @return This output stream
     */
    public final <T extends Enum<T> & ValueEnum<Integer>> MemoryOutputStream enum32(T value) {
        if (value == null) return this.i32(0);
        return this.i32(value.getValue().intValue());
    }

    /**
     * Writes a series of null characters to the stream.
     * @param size Number of bytes to write
     * @return This output stream
     */
    public final MemoryOutputStream pad(int size) {
        this.offset += size;
        return this;
    }

    /**
     * Shrinks the size of the buffer to the current offset.
     * @return This output stream
     */
    public final MemoryOutputStream shrink() {
        this.buffer = Arrays.copyOfRange(this.buffer, 0, this.offset); 
        return this;
    }

    /**
     * Seeks to position relative to seek mode.
     * @param offset Offset relative to seek position
     * @param mode Seek origin
     */
    public final void seek(int offset, SeekMode mode) {
        if (mode == null)
            throw new NullPointerException("SeekMode cannot be null!");
        if (offset < 0) throw new IllegalArgumentException("Can't seek to negative offsets.");
        switch (mode) {
            case Begin: {
                if (offset > this.length)
                    throw new IllegalArgumentException("Can't seek past stream length.");
                this.offset = offset;
                break;
            }
            case Relative: {
                int newOffset = this.offset + offset;
                if (newOffset > this.length || newOffset < 0)
                    throw new IllegalArgumentException("Can't seek outside bounds of stream.");
                this.offset = newOffset;
                break;
            }
            case End: {
                if (offset < 0 || this.length - offset < 0)
                    throw new IllegalArgumentException("Can't seek outside bounds of stream.");
                this.offset = this.length - offset;
                break;
            }
        }
    }

    /**
     * Seeks ahead in stream relative to offset.
     * @param offset Offset to go to
     */
    public final void seek(int offset) { 
        this.seek(offset, SeekMode.Relative);
    }

    public final byte[] getBuffer() { return this.buffer; }
    public final int getOffset() { return this.offset; }
    public final int getLength() { return this.length; }
    public final boolean isLittleEndian() { return this.isLittleEndian; }

    public final void setLittleEndian(boolean value) { this.isLittleEndian = value; }
}
