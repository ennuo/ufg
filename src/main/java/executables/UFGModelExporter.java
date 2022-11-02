package executables;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import ufg.util.Bytes;
import ufg.util.ExecutionContext;
import ufg.util.FileIO;
import ufg.utilities.DecompressLZ;
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

        String game = null;
        File folder = null;

        args = new String[] {
            "-g",
            "modnation",
            "C:/Users/Aidan/Desktop/KARTMODELPACKSTREAMING.BIN",
            "-o",
            "C:/Users/Aidan/Desktop/EXPORT"
        };

        if (args.length < 5) {
            System.out.println("UFG Model Exporter by ennuo\n");
            System.out.println("Usage:");
            System.out.println("\tTo export just model data:");
            System.out.println("\t\tjava -jar ufg.jar -g <game> <...model_packs> -o <output_folder>\n");

            // System.out.println("\tTo export model data w/ textures:");
            // System.out.println("\t\tjava -jar ufg.jar -g <game> <...model_packs> -o <output_folder> -tp <perm.bin> <temp.bin>\n");

            System.out.println("\tTo export model data w/ textures (indexed texture stream):");
            System.out.println("\t\tjava -jar ufg.jar -g <game> <...model_packs> -o <output_folder> -tps <perm.bin> <.perm.idx>\n");

            System.out.println("NOTE: You may specify multiple texture packs and model packs by repeating respective arguments.\n");
            
            System.out.println("Arguments:");
            System.out.println("\t--game, -g <game>\n\tSpecifies which game source data is from.\n\tSupported games are \"mnr\" and \"karting\"\n");
            System.out.println("\t--output, -o <output_folder>\n\tSpecifies what folder to store output\n");
            // System.out.println("\t--texture-pack, -tp <perm.bin> <temp.bin>\n\tLoads a texture pack\n");
            System.out.println("\t--texture-pack-stream, -tps <perm.bin> <.idx>\n\tLoads an indexed texture pack stream");

            return;
        }

        ArrayList<String> modelPackArguments = new ArrayList<>();
        ArrayList<StringPair> textureStreamArguments = new ArrayList<>();

        HashMap<Integer, ResourceData> modelStreamingResources = new HashMap<>();
        HashMap<Integer, ResourceData> textureStreamingResources = new HashMap<>();

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

        if (game.equals("karting")) ExecutionContext.IS_MODNATION_RACERS = false;
        else if (game.equals("modnation") || game.equals("mnr")) ExecutionContext.IS_MODNATION_RACERS = true;
        else {
            System.err.println("Invalid game was specified!");
            return;
        }

        for (StringPair pair : textureStreamArguments) {
            byte[] bin = FileIO.read(pair.first());

            ChunkFileIndex index = null;
            try { index = Chunk.loadChunk(pair.second()).loadResource(ChunkFileIndex.class); }
            catch (Exception ex) {
                System.err.println("An error occurred processing ChunkFileIndex (" + pair.second() + ")");
                return;
            }

            for (ChunkFileIndexEntry entry : index.entries) {
                byte[] section = Arrays.copyOfRange(bin, entry.lowerPosition, entry.lowerPosition + entry.byteSize);
                if (entry.uncompressedSize != 0) 
                    section = DecompressLZ.decompress(section);
                
                ResourceType type = ResourceType.fromValue(Bytes.toIntegerBE(section));
                if (type == ResourceType.TEXTURE_DESCRIPTOR) {
                    Texture texture = Chunk.loadChunk(section).loadResource(Texture.class);
                    textureStreamingResources.put(texture.UID, texture);
                    continue;
                }

                if (type != ResourceType.TEXTURE_PACK)
                    throw new RuntimeException("Found unexpected resource in texture pack!");

                TexturePack pack = null;
                if (ExecutionContext.IS_MODNATION_RACERS) 
                    pack = Chunk.loadChunk(section).loadResource(TexturePack.class);
                else {
                    pack = new TexturePack();
                    pack.UID = entry.filenameUID;
                    pack.stream = section;
                }

                textureStreamingResources.put(pack.UID, pack);
            }
        }

        for (String modelPack : modelPackArguments) {
            try {
                Chunk[] chunks = Chunk.loadChunks(modelPack);
                for (Chunk chunk : chunks) {
                    ResourceType type = ResourceType.fromValue(chunk.UID);
                    if (type == null) continue;
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
    }
}
