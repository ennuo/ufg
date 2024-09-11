package ufg.resources;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.GsonBuilder;

import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.io.streams.MemoryInputStream;
import ufg.io.streams.MemoryInputStream.SeekMode;
import ufg.io.streams.MemoryOutputStream;
import ufg.structures.chunks.Chunk;
import ufg.structures.chunks.ResourceData;
import ufg.util.FileIO;
import ufg.util.UFGCRC;

public class Localization extends ResourceData {
    private HashMap<Integer, String> strings = new HashMap<>();

    public void put(String key, String value) {
        this.strings.put(UFGCRC.qStringHashUpper32(key), value);
    }

    public void put(Integer key, String value) {
        this.strings.put(key, value);
    }

    public boolean contains(String key) { 
        return this.strings.containsKey(UFGCRC.qStringHashUpper32(key));
    }

    public boolean contains(Integer key) {
        return this.strings.containsKey(key);
    }

    public String get(String key) {
        return this.strings.get(UFGCRC.qStringHashUpper32(key));
    }

    public String get(Integer key) { return this.strings.get(key); }
    
    @SuppressWarnings("unchecked")
    @Override public Localization serialize(Serializer serializer, Serializable structure) {
        Localization data = (structure == null) ? new Localization() : (Localization) structure;

        super.serialize(serializer, data);

        if (serializer.isWriting()) {
            MemoryOutputStream stream = serializer.getOutput();

            int hashTableSize = (0x4 * data.strings.size());
            int stringTableSize = data.strings.values()
                .stream()
                .mapToInt(string -> string.getBytes().length + 1)
                .reduce(0, (a, b) -> a + b);
            int dataSize = 0x8 + hashTableSize + stringTableSize;

            stream.i32(dataSize);
            stream.i32(0x4); // chunkDataOffset
            stream.i32(hashTableSize);
            stream.i32(stringTableSize);

            ArrayList<Integer> hashes = new ArrayList<>(data.strings.keySet());
            hashes.sort((a, z) -> Integer.compareUnsigned(a, z));
            for (int hash : hashes)
                stream.i32(hash);
            for (int hash : hashes)
                stream.cstr(data.strings.get(hash));
            stream.align(0x10);

            return data;
        }

        MemoryInputStream stream = serializer.getInput();

        int chunkSize = stream.i32();

        int chunkDataOffset = stream.getOffset();
        chunkDataOffset += stream.i32();

        stream.seek(chunkDataOffset, SeekMode.Begin);

        int hashTableSize = stream.i32();
        int stringTableSize = stream.i32();
        int stringCount = hashTableSize / 0x4;

        data.strings = new HashMap<>(stringCount);

        int[] hashes = new int[stringCount];
        for (int i = 0; i < stringCount; ++i)
            hashes[i] = stream.i32();
        for (int i = 0; i < stringCount; ++i)
            data.strings.put(hashes[i], stream.cstr());

        return data;
    }

    @Override public int getAllocatedSize() { return 0x50000; }
}
