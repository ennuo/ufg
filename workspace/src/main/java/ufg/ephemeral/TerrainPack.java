package ufg.ephemeral;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import ufg.enums.ResourceType;
import ufg.enums.TextureFormat;
import ufg.io.Serializer;
import ufg.io.streams.MemoryInputStream;
import ufg.io.streams.MemoryOutputStream;
import ufg.resources.ChunkFileIndex;
import ufg.resources.Texture;
import ufg.structures.chunks.Chunk;
import ufg.structures.chunks.ChunkFileIndexEntry;
import ufg.util.FileIO;
import ufg.util.UFGCRC;

public class TerrainPack {
    public static class TextureChunkerResult {
        public Texture texture;
        public byte[] data;

        public TextureChunkerResult(Texture texture, byte[] data) {
            this.texture = texture;
            this.data = data;
        }
    }

    private static File workingDirectory;
    static {
        try {
            workingDirectory = Files.createTempDirectory("twd").toFile();
            workingDirectory.deleteOnExit();
        } catch (IOException ex) { System.out.println("An error occurred creating temp directory."); }
    }

    public static TextureChunkerResult convertTextureData(String name, byte[] buffer, boolean isDXT5)
    {
        return convertTextureData(name, buffer, isDXT5, 0);
    }

    public static TextureChunkerResult convertTextureData(String name, byte[] buffer, boolean isDXT5, int samplerFlags) {
        try {
            File input = new File(workingDirectory, "TEXTURE.PNG");
            File output = new File(workingDirectory, "TEXTURE.DDS");

            FileIO.write(buffer, input.getAbsolutePath());


            ProcessBuilder builder = new ProcessBuilder(new String[] {
                "texconv",
                "-f",
                isDXT5 ? "DXT5" : "DXT1",
                "-y",
                "-nologo",
                "-m",
                "0",
                input.getAbsolutePath(),
                "-o",
                workingDirectory.getAbsolutePath()
            });

            builder.start().waitFor();

            byte[] imageData = null;
            if (output.exists()) {
                imageData = FileIO.read(output.getAbsolutePath());
                output.delete();
            }

            input.delete();

            if (imageData == null)
                throw new RuntimeException("Failed to convert image to DDS!");

            Texture texture = new Texture();
            texture.flags = samplerFlags;
            texture.name = name.replace(".TGA", "");
            texture.UID = UFGCRC.qStringHash32(name.toUpperCase());
            texture.imageDataPosition = 0x80;
            texture.imageDataByteSize = imageData.length - 0x80;

            MemoryInputStream header = new MemoryInputStream(imageData);
            header.setLittleEndian(true);
            header.forward(0xC);
            
            texture.height = (short) header.i32();
            texture.width = (short) header.i32();

            header.forward(0x8);

            texture.numMipMaps = (byte) header.i32();

            header.forward(0x34);

            String DXT = header.str(4);

            if (DXT.equals("DXT1"))
                texture.format = TextureFormat.DXT1;
            else if (DXT.equals("DXT5"))
                texture.format = TextureFormat.DXT5;
            else throw new RuntimeException("Unsupported DDS type!");


            return new TextureChunkerResult(texture, Arrays.copyOfRange(imageData, 0x80, imageData.length));
        } catch (Exception ex) { 

            ex.printStackTrace();
            return null;

         }
    }

    public static void main(String[] args) {
        TextureChunkerResult[] results = new TextureChunkerResult[] {
            convertTextureData("TRN_THEME_1_D", FileIO.read("C:/Users/Aidan/Desktop/Terrain/TRN_THEME_1_D.png"), false, 3),
            convertTextureData("TRN_THEME_1_P", FileIO.read("C:/Users/Aidan/Desktop/Terrain/TRN_THEME_1_P.png"), false, 3),
            convertTextureData("TRN_THEME_1_N", FileIO.read("C:/Users/Aidan/Desktop/Terrain/TRN_THEME_1_N.png"), true, 3),
            convertTextureData("MATERIALIDBRUSH_1_ICON", FileIO.read("C:/Users/Aidan/Desktop/Terrain/MATERIALIDBRUSH_1_ICON.png"), true),
            convertTextureData("MATERIALIDBRUSH_2_ICON", FileIO.read("C:/Users/Aidan/Desktop/Terrain/MATERIALIDBRUSH_2_ICON.png"), true),
            convertTextureData("MATERIALIDBRUSH_3_ICON", FileIO.read("C:/Users/Aidan/Desktop/Terrain/MATERIALIDBRUSH_3_ICON.png"), true),
            convertTextureData("MATERIALIDBRUSH_4_ICON", FileIO.read("C:/Users/Aidan/Desktop/Terrain/MATERIALIDBRUSH_4_ICON.png"), true),
            convertTextureData("MATERIALIDBRUSH_5_ICON", FileIO.read("C:/Users/Aidan/Desktop/Terrain/MATERIALIDBRUSH_5_ICON.png"), true),
            convertTextureData("MATERIALIDBRUSH_6_ICON", FileIO.read("C:/Users/Aidan/Desktop/Terrain/MATERIALIDBRUSH_6_ICON.png"), true),
            convertTextureData("MATERIALIDBRUSH_7_ICON", FileIO.read("C:/Users/Aidan/Desktop/Terrain/MATERIALIDBRUSH_7_ICON.png"), true),
            convertTextureData("MATERIALIDBRUSH_8_ICON", FileIO.read("C:/Users/Aidan/Desktop/Terrain/MATERIALIDBRUSH_8_ICON.png"), true),

        };

        ChunkFileIndex texturePackIndex = new ChunkFileIndex();
        int textureCount = results.length;

        MemoryOutputStream texturePackStream = new MemoryOutputStream((4_767_119 * textureCount) + (0x80 * textureCount * 2));
        
        for (TextureChunkerResult result : results) {
            Texture texture = result.texture;
            byte[] imageData = result.data;
            int textureDataUID = UFGCRC.qStringHash32(texture.name + ".VRAM.TEX");
            int textureIndexUID = UFGCRC.qStringHash32(texture.name + ".HEAD.TEX");


            int textureDataStartOffset = texturePackStream.getOffset();

            texturePackStream.i32(0x5E73CDD7);
            int dataOffset = 0x70;

            int dataSize = imageData.length;
            dataSize += dataOffset;


            texturePackIndex.entries.add(new ChunkFileIndexEntry(textureDataUID, textureDataStartOffset, dataSize + 0x10));

            texturePackStream.i32(dataSize);
            texturePackStream.i32(dataSize);
            texturePackStream.i32(dataOffset);
            texturePackStream.pad(dataOffset);
            texturePackStream.bytes(imageData);


            
            texturePackStream.AddDeadBeef();
            texturePackStream.AddPaddedRegion();

            texture.alphaStateSampler = textureDataUID;
            texture.imageDataByteSize = dataSize + 0x10 - 0x80;

            texturePackIndex.entries.add(new ChunkFileIndexEntry(textureIndexUID, texturePackStream.getOffset(), 0x110));
        
            texturePackStream.i32(ResourceType.TEXTURE_DESCRIPTOR.getValue());
            texturePackStream.i32(0x100); texturePackStream.i32(0x100);
            texturePackStream.i32(0);

            Serializer serializer = new Serializer(texturePackStream);
            serializer.struct(texture, Texture.class);

            texturePackStream.AddDeadBeef();
            if (result != results[results.length - 1])
                texturePackStream.AddPaddedRegion();
        }

        texturePackStream.shrink();
        byte[] texturePack = texturePackStream.getBuffer();


        FileIO.write(texturePack, "C:/~/ENV_ROOMTEXTUREPACK.PERM.BIN");


        
        texturePackIndex.entries.sort((a, b) -> Integer.compareUnsigned(a.filenameUID, b.filenameUID));
        texturePackIndex.name = "ENV_RoomTexturePack.perm";
        texturePackIndex.UID = UFGCRC.qStringHashUpper32(texturePackIndex.name);


        FileIO.write(
            Chunk.saveChunks(new Chunk[] { Chunk.toChunk(ResourceType.CHUNK_FILE_INDEX.getValue(), texturePackIndex) }), "C:/~/ENV_ROOMTEXTUREPACK.PERM.IDX");
    }
}
