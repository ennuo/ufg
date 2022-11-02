package ufg.io.streams;

import java.util.Arrays;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import ufg.io.ValueEnum;
import ufg.util.Bytes;
import ufg.util.FileIO;

/**
 * Big-endian binary input stream.
 */
public class MemoryInputStream {
    public static enum SeekMode {
        Begin,
        Relative,
        End
    }
    
    private final byte[] buffer;

    private int offset = 0;
    private final int length;

    private boolean isLittleEndian = false;

    /**
     * Creates a memory input stream from byte array.
     * @param buffer Byte array to use as source
     */
    public MemoryInputStream(byte[] buffer) {
        if (buffer == null)
            throw new NullPointerException("Buffer supplied to MemoryInputStream cannot be null!");
        this.buffer = buffer;
        this.length = buffer.length;
    }

    /**
     * Creates a memory input stream from file at path.
     * @param path Location to read data from
     */
    public MemoryInputStream(String path) {
        if (path == null)
            throw new NullPointerException("Path supplied to MemoryInputStream cannot be null!");
        final byte[] data = FileIO.read(path);
        if (data == null)
            throw new IllegalArgumentException("File provided could not be read!");
        this.buffer = data;
        this.length = data.length;
    }

    /**
     * Reads an arbitrary number of bytes from the stream.
     * @param size Number of bytes to read from the stream
     * @return Bytes read from the stream
     */
    public final byte[] bytes(int size) {
        this.offset += size;
        return Arrays.copyOfRange(this.buffer, this.offset - size, this.offset);
    }

    /**
     * Reads a byte array from the stream.
     * @return Bytes read from the stream
     */
    public final byte[] bytearray() {
        int size = this.i32();
        return this.bytes(size);
    }

    /**
     * Reads a boolean from the stream.
     * @return Boolean read from the stream
     */
    public final boolean bool() { return (this.i8() != 0); }

    /**
     * Reads a byte from the stream.
     * @return Byte read from the stream
     */
    public final byte i8() { return this.buffer[this.offset++]; }

    /**
     * Reads an unsigned byte from the stream as an integer.
     * @return Byte read from the stream
     */
    public final int u8() { return this.buffer[this.offset++] & 0xFF; }

    /**
     * Reads a short from the stream.
     * @return Short read from the stream
     */
    public final short i16() { 
        byte[] bytes = this.bytes(2);
        if (this.isLittleEndian) return Bytes.toShortLE(bytes);
        return Bytes.toShortBE(bytes);
    }

    /**
     * Reads an unsigned short from the stream as an integer.
     * @return Short read from the stream
     */
    public final int u16() { return (this.i16() & 0xFFFF); } 

    /**
     * Reads an unsigned 24-bit integer from the stream.
     * @return Integer read from the stream
     */
    public final int u24() {
        final byte[] b = this.bytes(3);
        if (this.isLittleEndian)
            return (b[2] & 0xFF) << 16 | (b[1] & 0xFF) << 8 | b[0] & 0xFF;
        return (b[0] & 0xFF) << 16 | (b[1] & 0xFF) << 8 | b[2] & 0xFF;
    }

    /**
     * Reads a 32-bit integer from the stream
     * @return Integer read from the stream
     */
    public final int i32() {
        byte[] bytes = this.bytes(4);
        if (this.isLittleEndian) return Bytes.toIntegerLE(bytes);
        return Bytes.toIntegerBE(bytes);
    }

    /**
     * Reads a long as an unsigned integer from the stream.
     * @return Unsigned integer read from the stream
     */
    public final long u32() {
        return this.i32() & 0xFFFFFFFFl;
    }

    /**
     * Reads a long from the stream.
     * @return Long read from the stream
     */
    public final long i64() {
        final byte[] b = this.bytes(8);
        if (this.isLittleEndian) {
            return	(b[7] & 0xFFL) << 56L |
                    (b[6] & 0xFFL) << 48L |
                    (b[5] & 0xFFL) << 40L |
                    (b[4] & 0xFFL) << 32L |
                    (b[3] & 0xFFL) << 24L |
                    (b[2] & 0xFFL) << 16L |
                    (b[1] & 0xFFL) << 8L |
                    (b[0] & 0xFFL) << 0L;
        }
        return	(b[0] & 0xFFL) << 56L |
                (b[1] & 0xFFL) << 48L |
                (b[2] & 0xFFL) << 40L |
                (b[3] & 0xFFL) << 32L |
                (b[4] & 0xFFL) << 24L |
                (b[5] & 0xFFL) << 16L |
                (b[6] & 0xFFL) << 8L |
                (b[7] & 0xFFL) << 0L;
    }

    /**
     * Reads a 16 bit floating point number from the stream.
     * https://stackoverflow.com/questions/6162651/half-precision-floating-point-in-java
     * @return Float read from the stream
     */
    public final float f16() {
        int half = this.u16();
        int mant = half & 0x03ff;
        int exp = half & 0x7c00;
        if (exp == 0x7c00) exp = 0x3fc00;
        else if (exp != 0) {
            exp += 0x1c000;
            if (mant == 0 && exp > 0x1c400)
                return Float.intBitsToFloat((half & 0x8000) << 16 | exp << 13 | 0x3ff);
        } else if (mant != 0) {
            exp = 0x1c400;
            do {
                mant <<= 1;
                exp -= 0x400;
            } while ((mant & 0x400) != 0);
            mant &= 0x3ff;
        }
        return Float.intBitsToFloat((half & 0x8000) << 16 | (exp | mant) << 13);
    }

    /**
     * Reads a 32 bit floating point number from the stream.
     * @return Float read from the stream
     */
    public final float f32() { return Float.intBitsToFloat(this.i32()); }

    /**
     * Reads a 2-dimensional floating point vector from the stream.
     * @return Vector2f read from the stream
     */
    public final Vector2f v2() { return new Vector2f(this.f32(), this.f32()); }

    /**
     * Reads a 3-dimensional floating point vector from the stream.
     * @return Vector3f read from the stream
     */
    public final Vector3f v3() { return new Vector3f(this.f32(), this.f32(), this.f32()); }

    /**
     * Reads a 4-dimensional floating point vector from the stream.
     * @return Vector4f read from the stream
     */
    public final Vector4f v4() { return new Vector4f(this.f32(), this.f32(), this.f32(), this.f32()); }

    /**
     * Reads a Matrix4x4 from the stream, compressed depending on flags.
     * @return Matrix4x4 read from the stream
     */
    public Matrix4f m44() {
        float[] matrix = new float[16];
        for (int i = 0; i < 16; ++i)
            matrix[i] = this.f32();
        
        final Matrix4f mat = new Matrix4f();
        mat.set(matrix);
        return mat;
    }

    /**
     * Aligns current stream offset to a boundary.
     * @param boundary Alignment boundary
     */
    public final void align(int boundary) {
        if (this.offset % boundary != 0)
            this.offset += (boundary - (this.offset % boundary));
    }

    /**
     * Reads a null terminated c-string from the stream.
     * @return String read from the stream.
     */
    public final String cstr() {
        int start = this.offset;
        int end = start;

        while (this.buffer[end] != '\0') end++;

        return this.str(end - start + 1);
    }

    /**
     * Reads a string of specified size from the stream.
     * @param size Size of string to read
     * @return String value read from the stream
     */
    public final String str(int size) {
        if (size == 0) return "";
        return new String(this.bytes(size)).replace("\0", "");
    }

    /**
     * Reads an 8-bit integer from the stream and resolves the enum value.
     * @param <T> Type of enum
     * @param enumeration Enum class
     * @return Resolved enum constant
     */
    public final <T extends Enum<T> & ValueEnum<Byte>> T enum8(Class<T> enumeration) {
        byte number = this.i8();
        List<T> constants = Arrays.asList(enumeration.getEnumConstants());
        for (T constant : constants)
            if (constant.getValue().equals(number))
                return constant;
        return null;
    }

    /**
     * Reads an 32-bit integer from the stream and resolves the enum value.
     * @param <T> Type of enum
     * @param enumeration Enum class
     * @return Resolved enum constant
     */
    public final <T extends Enum<T> & ValueEnum<Integer>> T enum32(Class<T> enumeration) {
        int number = this.i32();
        List<T> constants = Arrays.asList(enumeration.getEnumConstants());
        for (T constant : constants)
            if (constant.getValue().equals(number))
                return constant;
        return null;
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

    public final boolean isLittleEndian() { return this.isLittleEndian; }
    public final byte[] getBuffer() { return this.buffer; }
    public final int getOffset() { return this.offset; }
    public final int getLength() { return this.length; }
    public void setLittleEndian(boolean value) { this.isLittleEndian = value; }
}
