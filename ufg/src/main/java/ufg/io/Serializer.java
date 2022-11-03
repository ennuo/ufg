package ufg.io;

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import ufg.ex.SerializationException;
import ufg.io.streams.MemoryInputStream;
import ufg.io.streams.MemoryOutputStream;
import ufg.io.streams.MemoryInputStream.SeekMode;
import ufg.util.Bytes;

/**
 * Reversible serializer for assets.
 */
public class Serializer {
    private final boolean isWriting;

    private final MemoryInputStream input;
    private final MemoryOutputStream output;

    /**
     * Constructs a deserializer with stream and revision.
     * @param stream Input stream to use for serializer
     */
    public Serializer(MemoryInputStream stream) {
        this.input = stream;
        this.output = null;
        this.isWriting = false;
    }

    /**
     * Constructs a serializer with stream and revision.
     * @param stream Output stream to use for serializer
     */
    public Serializer(MemoryOutputStream stream) {
        this.output = stream;
        this.input = null;
        this.isWriting = true;
    }

    /**
     * Constructs a new serializer.
     * @param size Size of output stream
     */
    public Serializer(int size) {
        this.output = new MemoryOutputStream(size);
        this.input = null;
        this.isWriting = true;
    }

    /**
     * Constructs a new deserializer.
     * @param data Buffer to use in deserializer
     */
    public Serializer(byte[] data) {
        this.input = new MemoryInputStream(data);
        this.output = null;
        this.isWriting = false;
    }

    /**
     * Pads a selected number of bytes in the stream.
     * @param size Number of bytes to pad
     */
    public final void pad(int size) {
        if (this.isWriting) this.output.pad(size);
        else this.input.bytes(size);
    }

    /**
     * (De)serailizes a byte array to/from the stream.
     * @param value Bytes to write
     * @return Bytes serialized
     */
    public final byte[] bytearray(byte[] value) {
        if (this.isWriting) {
            this.output.bytearray(value);
            return value;
        }
        return this.input.bytearray();
    }

    /**
     * (De)serializes bytes to/from the stream.
     * @param value Bytes to write
     * @param size Number of bytes to read
     * @return Bytes serialized
     */
    public final byte[] bytes(byte[] value, int size) {
        if (this.isWriting) {
            this.output.bytes(value);
            return value;
        }
        return this.input.bytes(size);
    }

    /**
     * (De)serializes a boolean to/from the stream.
     * @param value Boolean to write
     * @return Boolean (de)serialized
     */
    public final boolean bool(boolean value) {
        if (this.isWriting) {
            this.output.bool(value);
            return value;
        }
        return this.input.bool();
    }

    /**
     * (De)serializes a byte to/from the stream.
     * @param value Byte to write
     * @return Byte (de)serialized
     */
    public final byte i8(byte value) {
        if (this.isWriting) {
            this.output.i8(value);
            return value;
        }
        return this.input.i8();
    }

    /**
     * (De)serializes an integer as a byte to/from the stream.
     * @param value Byte to write
     * @return Byte (de)serialized
     */
    public final int u8(int value) {
        if (this.isWriting) {
            this.output.u8(value);
            return value;
        }
        return this.input.u8();
    }

    /**
     * (De)serializes a short to/from the stream.
     * @param value Short to write
     * @return Short (de)serialized
     */
    public final short i16(short value) {
        if (this.isWriting) {
            this.output.i16(value);
            return value;
        }
        return this.input.i16();
    }

    /**
     * (De)serializes an integer to/from the stream as a short.
     * @param value Short to write
     * @return Short (de)serialized
     */
    public final int u16(int value) {
        if (this.isWriting) {
            this.output.u16(value);
            return value;
        }
        return this.input.u16();
    }

    /**
     * (De)serializes a 24-bit unsigned integer to/from the stream.
     * @param value Integer to write
     * @return Integer (de)serialized
     */
    public final int u24(int value) {
        if (this.isWriting) {
            this.output.u24(value);
            return value;
        }
        return this.input.u24();
    }

    /**
     * (De)serializes a 32-bit integer to/from the stream
     * @param value Integer to write
     * @return Integer (de)serialized
     */
    public final int i32(int value) {
        if (this.isWriting) {
            this.output.i32(value);
            return value;
        }
        return this.input.i32();
    }

    /**
     * (De)serializes an unsigned 32-bit integer to/from the stream as a long
     * @param value Integer to write
     * @return Integer (de)serialized
     */
    public final long u32(long value) {
        if (this.isWriting) {
            this.output.u32(value);
            return value;
        }
        return this.input.u32();
    }


    /**
     * (De)serializes a long to/from the stream
     * @param value Long to write
     * @return Long (de)serialized
     */
    public final long i64(long value) {
        if (this.isWriting) {
            this.output.i64(value);
            return value;
        }
        return this.input.i64();
    }


    /**
     * (De)serializes a 16-bit floating point number to/from the stream.
     * @param value Float to write
     * @return Float (de)serialized
     */
    public final float f16(float value) {
        if (this.isWriting) {
            this.output.f16(value);
            return value;
        }
        return this.input.f16();
    }

    /**
     * (De)serializes a 32-bit floating point number to/from the stream.
     * @param value Float to write
     * @return Float (de)serialized
     */
    public final float f32(float value) {
        if (this.isWriting) {
            this.output.f32(value);
            return value;
        }
        return this.input.f32();
    }

    /**
     * (De)serializes a 2-dimensional floating point vector to/from the stream.
     * @param value Vector2f to write
     * @return Vector2f (de)serialized
     */
    public final Vector2f v2(Vector2f value) {
        if (this.isWriting) {
            this.output.v2(value);
            return value;
        }
        return this.input.v2();
    }

    /**
     * (De)serializes a 3-dimensional floating point vector to/from the stream.
     * @param value Vector3f to write
     * @return Vector3f (de)serialized
     */
    public final Vector3f v3(Vector3f value) {
        if (this.isWriting) {
            this.output.v3(value);
            return value;
        }
        return this.input.v3();
    }

    /**
     * (De)serializes a 4-dimensional floating point vector to/from the stream.
     * @param value Vector4f to write
     * @return Vector4f (de)serialized
     */
    public final Vector4f v4(Vector4f value) {
        if (this.isWriting) {
            this.output.v4(value);
            return value;
        }
        return this.input.v4();
    }

    /**
     * (De)serializes a Matrix4x4 to/from the stream.
     * @param value Matrix4x4 to write
     * @return Matrix4x4 (de)serialized
     */
    public final Matrix4f m44(Matrix4f value) {
        if (this.isWriting) {
            this.output.m44(value);
            return value;
        }
        return this.input.m44();
    }

    public final String cstr(String value) {
        if (this.isWriting) {
            this.output.cstr(value);
            return value;
        }
        return this.input.cstr();
    }

    public final void align(int boundary) {
        if (this.isWriting) {
            this.output.align(boundary);
            return;
        }
        this.input.align(boundary);
    }

    /**
     * (De)serializes a fixed length string to/from the stream.
     * @param value String to write
     * @param size Fixed length of string to write
     * @return String (de)serialized
     */
    public final String str(String value, int size) {
        if (this.isWriting) {
            this.output.str(value, size);
            return value;
        }
        return this.input.str(size);
    }

    /**
     * (De)serializes a 8-bit enum value to/from the stream.
     * @param <T> Enum class
     * @param value Enum value
     * @return (De)serialized enum value
     */
    @SuppressWarnings("unchecked")
    public final <T extends Enum<T> & ValueEnum<Byte>> T enum8(T value) {
        if (this.isWriting) {
            this.output.enum8(value);
            return value;
        }
        return this.input.enum8((Class<T>) value.getClass());
    }

    /**
     * (De)serializes a 32-bit enum value to/from the stream.
     * @param <T> Enum class
     * @param value Enum value
     * @return (De)serialized enum value
     */
    @SuppressWarnings("unchecked")
    public final <T extends Enum<T> & ValueEnum<Integer>> T enum32(T value) {
        if (this.isWriting) {
            this.output.enum32(value);
            return value;
        }
        return this.input.enum32((Class<T>) value.getClass());
    }

    /**
     * (De)serializes a structure to/from the stream.
     * @param <T> Generic serializable structure
     * @param value Structure to serialize
     * @param clazz Serializable class type
     * @return (De)serialized structure
     */
    public final <T extends Serializable> T struct(T value, Class<T> clazz) {
        if (this.isWriting) {
            Serializable.serialize(this, value, clazz);
            return value;
        }
        return clazz.cast(Serializable.serialize(this, null, clazz));
    }

    /**
     * (De)serializes an arraylist to/from the stream.
     * @param <T> Generic serializable structure
     * @param values Array to serialize
     * @param clazz Array base serializable type
     * @return (De)serialized array
     */
    public final <T extends Serializable> ArrayList<T> arraylist(ArrayList<T> values, Class<T> clazz) {
        if (this.isWriting) {
            if (values == null) {
                this.output.i32(0);
                return values;
            }
            this.output.i32(values.size());
            for (T serializable : values)
                Serializable.serialize(this, serializable, clazz);
            return values;
        }
        int count = this.input.i32();
        ArrayList<T> output = new ArrayList<T>(count);
        for (int i = 0; i < count; ++i)
            output.add(clazz.cast(Serializable.serialize(this, null, clazz)));
        return output;
    }
    
    /**
     * (De)serializes an array to/from the stream.
     * @param <T> Generic serializable structure
     * @param values Array to serialize
     * @param clazz Array base serializable type
     * @return (De)serialized array
     */
    @SuppressWarnings("unchecked")
    public final <T extends Serializable> T[] array(T[] values, Class<T> clazz) {
        if (this.isWriting) {
            if (values == null) {
                this.output.i32(0);
                return values;
            }
            this.output.i32(values.length);
            for (T serializable : values)
                Serializable.serialize(this, serializable, clazz);
            return values;
        }
        int count = this.input.i32();
        T[] output = (T[]) Array.newInstance(clazz, count);
        try {
            for (int i = 0; i < count; ++i)
                output[i] = clazz.cast(Serializable.serialize(this, null, clazz));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new SerializationException("There was an error (de)serializing an array!");
        }
        return output;
    }

    /**
     * Shrinks the buffer to current offset and returns the buffer.
     * @return The shrinked buffer
     */
    public final byte[] getBuffer() {
        if (!this.isWriting) return null;
        return this.output.shrink().getBuffer();
    }

    public final MemoryInputStream getInput() { return this.input; }
    public final MemoryOutputStream getOutput() { return this.output; }
    public final int getOffset() {
        if (this.isWriting) return this.output.getOffset();
        return this.input.getOffset();
    }
    public final int getLength() {
        if (this.isWriting) return this.output.getLength();
        return this.input.getLength();
    }

    public void log(String message) {
        if (this.isWriting) {
            System.out.println("[WRITING] @ 0x" + Bytes.toHex(Bytes.toBytesBE(this.getOffset())) + " -> " + message);
        } else
        System.out.println("[READING] @ 0x" + Bytes.toHex(Bytes.toBytesBE(this.getOffset())) + " -> " + message);

    }

    public void seek(int offset) {
        if (this.isWriting) this.output.seek(offset, SeekMode.Begin);
        else this.input.seek(offset, SeekMode.Begin);
    }

    public final boolean isWriting() { return this.isWriting; }
}
