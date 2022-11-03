package executables;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import ufg.util.Bytes;
import ufg.util.DecompressLZ;
import ufg.util.ExecutionContext;
import ufg.util.FileIO;
import ufg.util.UFGCRC;
import ufg.enums.ResourceType;
import ufg.io.MeshExporter;
import ufg.resources.ChunkFileIndex;
import ufg.resources.Model;
import ufg.resources.Texture;
import ufg.resources.TexturePack;
import ufg.structures.chunks.Chunk;
import ufg.structures.chunks.ChunkFileIndexEntry;
import ufg.structures.chunks.ResourceData;

public class UFGModelExporter {
    public static class StringPair {
        private String str1, str2;

        public StringPair(String first, String second) {
            this.str1 = first;
            this.str2 = second;
        }

        public String first() { return this.str1; }
        public String second() { return this.str2; }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("UFG Model Exporter by ennuo\n");
            System.out.println("Usage:");
            System.out.println("\tTo export just model data:");
            System.out.println("\t\tjava -jar ufg.jar -g <game> <...model_packs> -o <output_folder>\n");

            System.out.println("\tTo export model data w/ textures:");
            System.out.println("\t\tjava -jar ufg.jar -g <game> <...model_packs> -o <output_folder> -tp <perm.bin> <temp.bin>\n");

            System.out.println("\tTo export model data w/ textures (indexed texture stream):");
            System.out.println("\t\tjava -jar ufg.jar -g <game> <...model_packs> -o <output_folder> -tps <perm.bin> <perm.idx>\n");

            System.out.println("\tTo just export a texture pack:");
            System.out.println("\t\tjava -jar ufg.jar -g <game> -o <output_folder> -tps <perm.bin> <perm.idx> --dump-textures\n");
            System.out.println("\t\tjava -jar ufg.jar -g <game> -o <output_folder> -tp <perm.bin> <temp.bin> --dump-textures\n");

            System.out.println("NOTE: You may specify multiple texture packs and model packs by repeating respective arguments.\n");
            
            System.out.println("Arguments:");
            System.out.println("\t--game, -g <game>\n\tSpecifies which game source data is from.\n\tSupported games are mnr/modnation and karting/lbpk\n");
            System.out.println("\t--output, -o <output_folder>\n\tSpecifies what folder to store output\n");
            System.out.println("\t--texture-pack, -tp <perm.bin> <temp.bin>\n\tLoads a texture pack\n");
            System.out.println("\t--texture-pack-stream, -tps <perm.bin> <.idx>\n\tLoads an indexed texture pack stream\n");
            System.out.println("\t--dump-textures, -d\n\tDumps all texture packs to PNGs");

            return;
        }

        ArrayList<String> modelPackArguments = new ArrayList<>();
        ArrayList<StringPair> textureStreamArguments = new ArrayList<>();
        ArrayList<StringPair> texturePackArguments = new ArrayList<>();

        HashMap<Integer, ResourceData> modelStreamingResources = new HashMap<>();
        HashMap<Integer, ResourceData> textureStreamingResources = new HashMap<>();

        String game = null;
        File folder = null;
        boolean shouldDumpTextures = false;

        for (int i = 0; i < args.length; ++i) {

            if (args[i].startsWith("-")) {

                args[i] = args[i].toLowerCase();
                switch (args[i]) {
                    case "-g": case "--game": {
                        game = args[++i].toLowerCase();
                        continue;
                    }
                    case "-o": case "--output": {
                        folder = new File(args[++i]);
                        continue;
                    }
                    case "-tps": case "--texture-pack-stream": {
                        textureStreamArguments.add(new StringPair(args[++i], args[++i]));
                        continue;
                    }
                    case "-tp": case "--texture-pack": {
                        texturePackArguments.add(new StringPair(args[++i], args[++i]));
                        continue;
                    }
                    case "-d": case "--dump-textures": {
                        shouldDumpTextures = true;
                        continue;
                    }
                    default: {
                        throw new RuntimeException("Unknown argument: " + args[i]);
                    }
                }
            }

            if (!new File(args[i]).exists()) {
                System.err.println(String.format("%s does not exist!", args[i]));
                return;
            }

            modelPackArguments.add(args[i]);
        }

        if (folder == null) {
            System.err.println("No output folder was specified!");
            return;
        }

        if (game == null) {
            System.err.println("No game was specified!");
            return;
        }

        if (game.equals("karting") || game.equals("lbpk")) ExecutionContext.IS_MODNATION_RACERS = false;
        else if (game.equals("modnation") || game.equals("mnr")) ExecutionContext.IS_MODNATION_RACERS = true;
        else {
            System.err.println("Invalid game was specified!");
            return;
        }

        for (StringPair pair : textureStreamArguments) {
            if (!new File(pair.first()).exists()) {
                System.err.println(String.format("%s does not exist!", pair.first()));
                return;
            }

            if (!new File(pair.second()).exists()) {
                System.err.println(String.format("%s does not exist!", pair.second()));
                return;
            }

            ChunkFileIndex index = null;
            try { 
                Chunk chunk = Chunk.loadChunk(pair.second());
                if (chunk.UID != ResourceType.CHUNK_FILE_INDEX.getValue()) {
                    System.err.println(String.format("%s isn't an index file!", pair.second()));
                    return;
                }
                index = chunk.loadResource(ChunkFileIndex.class); 
            }
            catch (Exception ex) {
                System.err.println("An error occurred processing ChunkFileIndex (" + pair.second() + ")");
                return;
            }

            try (RandomAccessFile bin = new RandomAccessFile(new File(pair.first()), "r")) {
                for (ChunkFileIndexEntry entry : index.entries) {
                    int upperPosition = entry.lowerPosition + entry.byteSize;
                    if (upperPosition > bin.length()) {
                        System.err.println("Indexed data exceeds size of bin file! Are you sure you're loading the right file?");
                        return;
                    }
    
                    bin.seek(entry.lowerPosition);
                    byte[] section = new byte[entry.byteSize];
                    bin.readFully(section);
    
                    int magic = Bytes.toIntegerBE(section);
    
                    try {
                        // Compression magic values
                        if (magic == 1347240785 || magic == 1363365200) 
                            section = DecompressLZ.decompress(section);
                    } catch (Exception ex) {
                        System.out.println("An error occurred while decompressing data!");
                        return;
                    }
    
                    ResourceType type = ResourceType.fromValue(Bytes.toIntegerBE(section));
                    if (type == ResourceType.TEXTURE_DESCRIPTOR) {
                        try {
                            Texture texture = Chunk.loadChunk(section).loadResource(Texture.class);
                            textureStreamingResources.put(texture.UID, texture);
                            continue;
                        } catch (Exception ex) {
                            System.err.println("An error occurred processing texture data!");
                            return;
                        }
                    }
    
                    if (type != ResourceType.TEXTURE_PACK) 
                        throw new RuntimeException("Found unexpected resource in texture pack!");
    
                    TexturePack pack = null;
                    if (ExecutionContext.IS_MODNATION_RACERS) {
                        try { pack = Chunk.loadChunk(section).loadResource(TexturePack.class); }
                        catch (Exception ex) {
                            System.err.println("An error occurred processing texture pack: " + pair.second());
                            return;
                        }
                    }
                    else {
                        pack = new TexturePack();
                        pack.UID = entry.filenameUID;
                        pack.stream = section;
                    }
                    
                    textureStreamingResources.put(pack.UID, pack);
                }
            } catch (IOException ex) {
                System.err.println("An error occurred opening read stream to " + pair.first());
                return;
            }
        }

        for (StringPair pair : texturePackArguments) {
            if (!new File(pair.first()).exists()) {
                System.err.println(String.format("%s does not exist!", pair.first()));
                return;
            }

            if (!new File(pair.second()).exists()) {
                System.err.println(String.format("%s does not exist!", pair.second()));
                return;
            }

            byte[] bin = FileIO.read(pair.second());
            Chunk[] chunks = null;
            try { chunks = Chunk.loadChunks(pair.first()); }
            catch (Exception ex) {
                System.err.println("An error occurred processing " + pair.first());
                return;
            }

            // Karting offsets for textures start from the absolute start of the file
            // Modnation offsets are relative to the start of the data
            TexturePack pack = null;
            if (ExecutionContext.IS_MODNATION_RACERS) {
                try { pack = Chunk.loadChunk(bin).loadResource(TexturePack.class); }
                catch (Exception ex) {
                    System.err.println("An error occurred processing texture pack: " + pair.second());
                    return;
                }
            }
            else {
                pack = new TexturePack();
                pack.stream = bin;
            }

            // Texture packs with the same filename have the same UID
            // so just generate a new one based on the file paths.
            pack.UID = UFGCRC.qStringHash32(pair.first() + pair.second());

            textureStreamingResources.put(pack.UID, pack);

            try {
                for (Chunk chunk : chunks) {
                    Texture texture = chunk.loadResource(Texture.class);
                    texture.alphaStateSampler = pack.UID;
                    textureStreamingResources.put(texture.UID, texture);
                }
            } catch (Exception ex) {
                System.err.println("An error occurred processing texture descriptors in " + pair.first());
                return;
            }
        }

        for (String modelPack : modelPackArguments) {
            try {
                Chunk[] chunks = Chunk.loadChunks(modelPack);
                for (Chunk chunk : chunks) {
                    ResourceType type = ResourceType.fromValue(chunk.UID);
                    if (type == null || type.getSerializable() == null) continue;
                    ResourceData resource = chunk.loadResource(type.getSerializable());
                    modelStreamingResources.put(resource.UID, resource);
                }
            } catch (Exception ex) {
                System.err.println("Failed to process all resource data in " + modelPack);
                return;
            }
        }

        for (int key : modelStreamingResources.keySet()) {
            ResourceData data = modelStreamingResources.get(key);
            if (!(data instanceof Model)) continue;
            byte[] glb = MeshExporter.getGLB(key, modelStreamingResources, textureStreamingResources);
            if (glb == null) {
                System.err.print("[ERROR] Failed to export " + data.name);
                continue;
            }

            FileIO.write(glb, new File(folder, data.name + ".GLB").getAbsolutePath());
        }

        if (!shouldDumpTextures) return;

        for (int key : textureStreamingResources.keySet()) {
            ResourceData data = textureStreamingResources.get(key);
            if (!(data instanceof Texture)) continue;
            Texture texture = (Texture) data;
            TexturePack pack = (TexturePack) textureStreamingResources.get(texture.alphaStateSampler);
            try { 
                FileIO.write(texture.toPNG(pack.stream), new File(folder, texture.name + ".PNG").getAbsolutePath());
            } catch (Exception ex) {
                System.err.println("An error occurred converting " + texture.name + " to PNG!");
            }
        }
    }
}
