package ufg.resources;

import java.util.ArrayList;

import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.structures.chunks.ChunkFileIndexEntry;
import ufg.structures.chunks.ResourceData;
import ufg.util.UFGCRC;

public class ChunkFileIndex extends ResourceData {
    public static final int BASE_ALLOCATION_SIZE = 0x40 + 0x10;

    public ArrayList<ChunkFileIndexEntry> entries = new ArrayList<>();

    public ChunkFileIndex() { 
        this.typeUID = UFGCRC.qStringHash32("Quark.qChunkFileIndex"); 
    }
    
    @SuppressWarnings("unchecked")
    @Override public ChunkFileIndex serialize(Serializer serializer, Serializable structure) {
        ChunkFileIndex index = (structure == null) ? new ChunkFileIndex() : (ChunkFileIndex) structure;

        super.serialize(serializer, index);

        int dataOffset = serializer.getOffset();
        dataOffset += serializer.i32(0x8);

        int numEntries = serializer.i32(serializer.isWriting() ? index.entries.size() : 0);

        serializer.seek(dataOffset);

        if (serializer.isWriting()) {
            for (ChunkFileIndexEntry entry : index.entries)
                serializer.struct(entry, ChunkFileIndexEntry.class);
        } else {
            index.entries = new ArrayList<>(numEntries);
            for (int i = 0; i < numEntries; ++i)
                index.entries.add(serializer.struct(null, ChunkFileIndexEntry.class));
        }
        
        return index;
    }

    @Override public int getAllocatedSize() {
        return ChunkFileIndex.BASE_ALLOCATION_SIZE * (this.entries.size() * ChunkFileIndex.BASE_ALLOCATION_SIZE);
    }
}
