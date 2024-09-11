package ufg.ephemeral;

import ufg.enums.ResourceType;
import ufg.io.streams.MemoryInputStream;
import ufg.resources.ChunkFileIndex;
import ufg.structures.chunks.Chunk;
import ufg.structures.chunks.ChunkFileIndexEntry;
import ufg.structures.chunks.ResourceData;

public class CheckForFunnyIndices {
    public static void main(String[] args) {
        ChunkFileIndex index = Chunk.loadChunk("E:/emu/rpcs3/dev_hdd0/game/NPUA80848/USRDIR/DATA/CREATE/KART/KARTMODELPACKSTREAMING.IDX").loadResource(ChunkFileIndex.class);
        MemoryInputStream stream = new MemoryInputStream("E:/emu/rpcs3/dev_hdd0/game/NPUA80848/USRDIR/DATA/CREATE/KART/KARTMODELPACKSTREAMING.BIN");
        for (ChunkFileIndexEntry entry : index.entries) {
            stream.seek(entry.lowerPosition);
            Chunk[] chunks = Chunk.loadChunks(stream.bytes(entry.byteSize));
            ResourceData primaryChunk = null;
            Chunk ppChunk = null;
            for (Chunk chunk : chunks) {
                ResourceData data = chunk.loadResource(ResourceData.class);
                if (data.UID == entry.filenameUID) {
                    ppChunk = chunk;
                    primaryChunk = data;
                }
            }

            if (primaryChunk == null) {
                // System.out.println("COULD NOT FIND PRIMARY CHUNK FOR : " + entry.filenameUID);
                // System.out.println(entry.lowerPosition);
                // System.out.println(entry.byteSize);
            } else {
                System.out.println("CHUNK: " + ResourceType.fromValue(ppChunk.UID) + " : "  + primaryChunk.name);
            }



        }

    }
}
