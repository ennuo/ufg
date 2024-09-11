package ufg.workspace;

import java.io.File;

import ufg.enums.GameVersion;
import ufg.resources.ChunkFileIndex;
import ufg.resources.Model;
import ufg.structures.chunks.Chunk;
import ufg.structures.chunks.ChunkFileIndexEntry;
import ufg.util.Bytes;
import ufg.util.ExecutionContext;
import ufg.util.FileIO;

public class Chunkinator implements Launchable
{
    @Override public boolean validate(String[] args)
    {
        if (args.length != 2)
        {
            System.out.println("java -jar bgrebuilder.jar <background> <output>");
            return false;
        }

        File directory = new File(args[0]);
        if (!directory.exists() || !directory.isDirectory()) 
        {
            System.out.println(args[0] + " is not a valid background directory!");
            return false;
        }

        return true;
    }

    private static int findChunkWithUID(Chunk[] chunks, int uid)
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

    @Override public void launch(String[] args) 
    {
        File directory = new File(args[0]);
        String name = directory.getName();

        // Upgrade all the files in each PANO
        for (int i = 0; i < 3; ++i)
        {
            File panoDirectory = new File(directory, "PANO" + i);
            if (!panoDirectory.exists()) continue;

            byte[] bin = FileIO.read(panoDirectory.getAbsolutePath() + "/MODELPACKSTREAMING.BIN");
            ChunkFileIndex index = Chunk.loadChunk(panoDirectory.getAbsolutePath() + "/MODELPACKSTREAMING.IDX").loadResource(ChunkFileIndex.class);
            byte[][] indices = new byte[index.entries.size()][];
            int offset = 0;
            for (int j = 0; j < indices.length; ++j)
            {
                ChunkFileIndexEntry chunk = index.entries.get(j);
                Chunk[] chunks = Chunk.loadChunks(bin, chunk.lowerPosition, chunk.lowerPosition + chunk.byteSize);

                int rootIndex = findChunkWithUID(chunks, chunk.filenameUID);
                
                // If the root index exists, then it's likely a model file, upgrade the asset!!
                if (rootIndex != -1)
                {
                    ExecutionContext.Version = GameVersion.KartingMilestone;
                    Model model = chunks[rootIndex].loadResource(Model.class);
                    ExecutionContext.Version = GameVersion.Karting;
                    chunks[rootIndex] = Chunk.toChunk(0x6DF963B3, model);
                }

                // Add the chunk and update the indices
                indices[j] = Chunk.saveChunks(chunks);
                int size = indices[j].length;
                chunk.byteSize = size;
                chunk.lowerPosition = offset;
                offset += size;
            }

            FileIO.write(Bytes.combine(indices), "E:/work/MODELPACKSTREAMING.BIN");
            FileIO.write(Chunk.saveChunks(new Chunk[] { Chunk.toChunk(0x7040F7D2, index) }), "E:/work/MODELPACKSTREAMING.IDX");
        }



    }
}
