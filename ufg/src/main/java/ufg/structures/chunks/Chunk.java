package ufg.structures.chunks;

import java.util.ArrayList;
import java.util.Arrays;

import ufg.enums.ResourceType;
import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.io.streams.MemoryInputStream;
import ufg.io.streams.MemoryOutputStream;
import ufg.util.DecompressLZ;
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

    public static byte[] saveChunksPadded(Chunk[] chunks) {
        int dataSize = Arrays.stream(chunks)
            .mapToInt(chunk -> chunk.getAllocatedSize())
            .reduce(0, (total, element) -> total + element);

        dataSize += (0x10 * chunks.length);

        Serializer serializer = new Serializer(dataSize);
        for (Chunk chunk : chunks)
        {
            serializer.getOutput().AddPaddedRegion(0x10);
            serializer.struct(chunk, Chunk.class);
        }

        return serializer.getBuffer();


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
        stream.forward(start);

        Serializer serializer = new Serializer(stream);

        ArrayList<Chunk> chunks = new ArrayList<>();
        while (serializer.getOffset() < end)
            chunks.add(serializer.struct(null, Chunk.class));
        
        return chunks.toArray(Chunk[]::new);
    }

    public static int findChunkWithUID(Chunk[] chunks, int uid)
    {
        for (int i = 0; i < chunks.length; ++i)
        {
            Chunk chunk = chunks[i];
            int chunkUid = (int) (
                (chunk.data[0x0c] & 0xFF) << 24 | 
                (chunk.data[0x0d] & 0xFF) << 16 | 
                (chunk.data[0x0e] & 0xFF) << 8 | 
                (chunk.data[0x0f] & 0xFF) << 0
            );

            if (chunkUid == uid)
                return i;
        }
        return -1;
    }
    
    public static Chunk createTerminatorChunk() {
        MemoryOutputStream stream = new MemoryOutputStream(0x3f0);
        for (int i = 0; i < 0x3f0 / 0x4; ++i)
            stream.i32(0xBFBFBFBF);
        Chunk chunk = new Chunk(0xDEADB0FF, stream.getBuffer());
        return chunk;
    }

    public static Chunk[] loadChunks(byte[] data) { 
        data = DecompressLZ.decompress(data);
        return Chunk.loadChunks(data, 0, data.length); 
    }
    public static Chunk[] loadChunks(String path) {
        byte[] data = FileIO.read(path);
        if (data == null) return null;
        data = DecompressLZ.decompress(data);
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

        int endOffset = chunk.chunkSize + 0x10;

        int paddingNeeded = 0;
        if (serializer.isWriting() && endOffset % 16 != 0)
            paddingNeeded = 16 - (endOffset % 16);

        chunk.UID = serializer.i32(chunk.UID);
        chunk.chunkSize = serializer.i32(chunk.chunkSize + paddingNeeded);
        chunk.dataSize = serializer.i32(chunk.dataSize + paddingNeeded);
        int dataOffset = serializer.i32(paddingNeeded);

        serializer.pad(dataOffset);
        chunk.data = serializer.bytes(chunk.data, chunk.dataSize - dataOffset);
        serializer.pad(chunk.chunkSize - chunk.dataSize);

        return chunk;
    }

    @Override public int getAllocatedSize() {
        return Chunk.BASE_ALLOCATION_SIZE + this.data.length + 0x10;
    }


}
