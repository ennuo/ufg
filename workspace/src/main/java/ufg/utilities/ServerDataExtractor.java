package ufg.utilities;

import java.io.File;

import ufg.io.streams.MemoryInputStream;
import ufg.resources.CreationMetadata;
import ufg.structures.chunks.Chunk;
import ufg.util.Bytes;
import ufg.util.FileIO;

public class ServerDataExtractor {
    public static void main(int SLOT_ID) {
        // int SLOT_ID = Integer.parseInt(args[0]);
        // int SLOT_ID = 437970;

        File dataDirectory = new File("C:/Users/Aidan/Downloads/LBPKServer/Player_creations/creations/" + SLOT_ID);
        File dataFile = new File(dataDirectory, "data.bin");
        File previewFile = new File(dataDirectory, "preview_image.png");


        if (!dataFile.exists()) {
            System.out.println("File doesn't exist!");
            return;
        }

        byte[] preview = null;
        if (previewFile.exists())
            preview = FileIO.read(previewFile.getAbsolutePath());

        MemoryInputStream stream = new MemoryInputStream(dataFile.getAbsolutePath());
        stream.i32(); // Skip the header
        int trackDataSize = stream.i32();
        int metadataSize = stream.i32();
        int navDataSize = stream.i32();
        int hudDataSize = stream.i32();

        
        byte[] trackData = stream.bytes(trackDataSize);
        byte[] metadata = stream.bytes(metadataSize);
        byte[] navData = stream.bytes(navDataSize);
        byte[] hudData = stream.bytes(hudDataSize);

        CreationMetadata details = Chunk.loadChunk(metadata).loadResource(CreationMetadata.class);


        String name = details.name.replaceAll(" ", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("[.]", "").toUpperCase() + "_" + Bytes.toHex(details.creationID);
        
        String exportDirectory = "C:/~/Export";

        String base = exportDirectory + "/" + details.creator + "/" + name + "/" + name;
        FileIO.write(trackData, base + ".TRK");
        FileIO.write(metadata, base + ".CM2");
        FileIO.write(navData, base + ".NAV");
        FileIO.write(hudData, base + ".HUD");

        if (preview != null) {
            FileIO.write(preview, base + "_SMALL.PNG");
            FileIO.write(preview, base + ".PNG");
        } else {
            FileIO.write(FileIO.read("E:/KART/DEFAULT_SMALL.PNG"), base + "_SMALL.PNG");
            FileIO.write(FileIO.read("E:/KART/DEFAULT.PNG"), base + ".PNG");
        }
    }
}
