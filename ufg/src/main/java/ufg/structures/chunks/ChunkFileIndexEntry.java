package ufg.structures.chunks;

import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.enums.BuildType;

public class ChunkFileIndexEntry implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x14;

    public int filenameUID;
    public int byteSize;
    public int uncompressedSize;
    public int lowerPosition;
    public byte upperPosition;
    public BuildType buildStatus = BuildType.BUILD_NOT_SET;
    public byte decompressionPaddingHi;
    public byte decompressionPaddingLow;

    public ChunkFileIndexEntry() {};
    public ChunkFileIndexEntry(int UID, int offset, int size) {
        this.filenameUID = UID;
        this.lowerPosition = offset;
        this.byteSize = size;
    }
    
    @SuppressWarnings("unchecked")
    @Override public ChunkFileIndexEntry serialize(Serializer serializer, Serializable structure) {
        ChunkFileIndexEntry entry = (structure == null) ? new ChunkFileIndexEntry() : (ChunkFileIndexEntry) structure;

        entry.filenameUID = serializer.i32(entry.filenameUID);
        entry.byteSize = serializer.i32(entry.byteSize);
        entry.uncompressedSize = serializer.i32(entry.uncompressedSize);
        entry.lowerPosition = serializer.i32(entry.lowerPosition);
        entry.upperPosition = serializer.i8(entry.upperPosition);
        entry.buildStatus = serializer.enum8(entry.buildStatus);
        entry.decompressionPaddingHi = serializer.i8(entry.decompressionPaddingHi);
        entry.decompressionPaddingLow = serializer.i8(entry.decompressionPaddingLow);

        return entry;
    }

    @Override public int getAllocatedSize() { 
        return ChunkFileIndexEntry.BASE_ALLOCATION_SIZE; 
    }
}
