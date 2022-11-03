package ufg.structures.chunks;

import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.resources.TexturePack;
import ufg.util.Bytes;
import ufg.util.DecompressLZ;
import ufg.util.ExecutionContext;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Arrays;

import ufg.enums.BuildType;

public class ChunkFileIndexEntry implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x14;

    public File handle;

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

    public byte[] getData() {
        try (RandomAccessFile file = new RandomAccessFile(this.handle , "r")) {
            file.seek(this.lowerPosition);
            byte[] section = new byte[this.byteSize];
            file.readFully(section);
            
            int magic = Bytes.toIntegerBE(section);
            if (magic == 1347240785 || magic == 1363365200) 
                section = DecompressLZ.decompress(section);
            
            return section;

        } catch (Exception ex) { return null; }
    }

    public <T extends ResourceData> T loadData(Class<T> clazz) {
        byte[] data = this.getData(); 
        if (data == null) return null;
        try { return Chunk.loadChunk(data).loadResource(clazz); }
        catch (Exception ex) { return null; }
    }

    public byte[] getTexturePackData() {
        if (ExecutionContext.IS_MODNATION_RACERS) {
            TexturePack pack = this.loadData(TexturePack.class);
            if (pack != null)
                return pack.stream;
            return null;
        }

        return this.getData();
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
