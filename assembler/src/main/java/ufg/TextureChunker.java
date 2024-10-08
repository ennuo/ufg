package ufg;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;

import ufg.enums.TextureFormat;
import ufg.io.streams.MemoryInputStream;
import ufg.resources.Texture;
import ufg.util.FileIO;
import ufg.util.UFGCRC;

public class TextureChunker {
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

    public static TextureChunkerResult convertTextureData(String name, byte[] buffer, boolean isDXT5) {
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
                "1",
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
            texture.name = name.replace(".TGA", "");
            texture.UID = UFGCRC.qStringHash32(name.toUpperCase());
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
        } catch (Exception ex) { return null; }
    }

    
    public static TextureChunkerResult convertTextureData(String name, ByteBuffer buffer) {
        try {
            File input = new File(workingDirectory, "TEXTURE.PNG");
            File output = new File(workingDirectory, "TEXTURE.DDS");


            try (FileOutputStream stream = new FileOutputStream(input)) {
                stream.getChannel().write(buffer);
            }

            ProcessBuilder builder = new ProcessBuilder(new String[] {
                "texconv",
                "-f",
                "DXT5",
                "-y",
                "-nologo",
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
        } catch (Exception ex) { return null; }
    }

    public static TextureChunkerResult convertTextureData(String name, ByteBuffer buffer, String size) {
        try {
            File input = new File(workingDirectory, "TEXTURE.PNG");
            File output = new File(workingDirectory, "TEXTURE.DDS");


            try (FileOutputStream stream = new FileOutputStream(input)) {
                stream.getChannel().write(buffer);
            }

            ProcessBuilder builder = new ProcessBuilder(new String[] {
                "texconv",
                "-f",
                "DXT5",
                "-y",
                "-nologo",
                "-w",
                size,
                "-h",
                size,
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
        } catch (Exception ex) { return null; }
    }
}
