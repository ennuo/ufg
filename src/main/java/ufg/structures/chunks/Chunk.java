package ufg.structures.chunks;

import java.util.ArrayList;
import java.util.Arrays;

import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.io.streams.MemoryInputStream;
import ufg.util.FileIO;

public class Chunk implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public int UID;
    public int chunkSize;
    public int dataSize;

    public transient byte[] data;

    public Chunk() {};
    public Chunk(int type, byte[] data) {
        this.data = data;
        this.UID = type;
        this.chunkSize = data.length;
        this.dataSize = data.length;
    }

    public static byte[] saveChunks(Chunk[] chunks) {
        int dataSize = Arrays.stream(chunks)
            .mapToInt(chunk -> chunk.getAllocatedSize())
            .reduce(0, (total, element) -> total + element);

        Serializer serializer = new Serializer(dataSize);
        for (Chunk chunk : chunks)
            serializer.struct(chunk, Chunk.class);
        
        return serializer.getBuffer();
    }

    public static Chunk[] loadChunks(byte[] data, int start, int end) {
        MemoryInputStream stream = new MemoryInputStream(data);
        stream.seek(start);

        Serializer serializer = new Serializer(stream);

        ArrayList<Chunk> chunks = new ArrayList<>();
        while (serializer.getOffset() < end)
            chunks.add(serializer.struct(null, Chunk.class));
        
        return chunks.toArray(Chunk[]::new);
    }

    public static Chunk[] loadChunks(byte[] data) { return Chunk.loadChunks(data, 0, data.length); }
    public static Chunk[] loadChunks(String path) {
        byte[] data = FileIO.read(path);
        if (data == null) return null;
        return Chunk.loadChunks(data, 0, data.length);
    }

    public static Chunk loadChunk(String path) { return Chunk.loadChunks(path)[0]; }
    public static Chunk loadChunk(byte[] data) { return Chunk.loadChunks(data)[0]; }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> Chunk toChunk(int UID, T data) {
        Serializer serializer = new Serializer(data.getAllocatedSize());
        serializer.struct(data, (Class<T>) data.getClass());
        return new Chunk(UID, serializer.getBuffer());
    }

    public <T extends Serializable> T loadResource(Class<T> clazz) {
        Serializer serializer = new Serializer(new MemoryInputStream(this.data));
        return serializer.struct(null, clazz);
    }

    @SuppressWarnings("unchecked")
    @Override public Chunk serialize(Serializer serializer, Serializable structure) {
        Chunk chunk = (structure == null) ? new Chunk() : (Chunk) structure;

        int dataOffset = 0;
        if (serializer.isWriting() && ((serializer.getOffset() + 16) % 16 != 0))
            dataOffset = 16 - ((serializer.getOffset() + 16) % 16);

        chunk.UID = serializer.i32(chunk.UID);
        chunk.chunkSize = serializer.i32(chunk.chunkSize + dataOffset);
        chunk.dataSize = serializer.i32(chunk.dataSize + dataOffset);
        dataOffset = serializer.i32(dataOffset);

        serializer.pad(dataOffset);
        chunk.data = serializer.bytes(chunk.data, chunk.dataSize - dataOffset);
        serializer.pad(chunk.chunkSize - chunk.dataSize);

        return chunk;
    }

    @Override public int getAllocatedSize() {
        return Chunk.BASE_ALLOCATION_SIZE + this.data.length + 0x10;
    }


}
