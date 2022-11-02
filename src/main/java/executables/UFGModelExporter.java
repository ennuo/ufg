package executables;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import ufg.util.ExecutionContext;
import ufg.util.FileIO;
import ufg.enums.ResourceType;
import ufg.io.MeshExporter;
import ufg.resources.Model;
import ufg.structures.chunks.Chunk;
import ufg.structures.chunks.ResourceData;

public class UFGModelExporter {
    public static void main(String[] args) {

        String game = null;
        File folder = null;

        // args = new String[] {
        //     "-g",
        //     "modnation",
        //     "C:/Users/Aidan/Desktop/CHARMODELPACKSTREAMING.BIN",
        //     "-o",
        //     "C:/Users/Aidan/Desktop/EXPORT"
        // };

        if (args.length < 5) {
            System.out.println("UFG Model Exporter by ennuo\n");
            System.out.println("Usage:\n");
            System.out.println("\tTo export just model data:");
            System.out.println("\t\tjava -jar ufg.jar -g <game> <...model_packs> -o <output_folder>\n");
            // System.out.println("\tTo export model data with textures");
            // System.out.println("\t\tjava -jar ufg.jar -g <game> <...model_packs> -o <output_folder> -t <texture_pack>\n");
            
            System.out.println("Arguments:\n");
            System.out.println("\t-g\tSpecifies which game source data is from.\n\t\tSupported games are:\n\t\t\tModnation\n\t\t\tKarting\n");
            System.out.println("\t-o\tSpecifies what folder to store output\n");
            
            return;
        }

        ArrayList<String> modelPacks = new ArrayList<>();
        HashMap<Integer, ResourceData> resources = new HashMap<>();
        for (int i = 0; i < args.length; ++i) {

            if (args[i].equals("-g")) {
                game = args[++i].toLowerCase();
                continue;
            }

            if (args[i].equals("-o")) {
                folder = new File(args[++i]);
                continue;
            }

            if (!new File(args[i]).exists()) {
                System.err.println(String.format("%s does not exist!", args[i]));
                return;
            }

            modelPacks.add(args[i]);
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
        else if (game.equals("modnation")) ExecutionContext.IS_MODNATION_RACERS = true;
        else {
            System.err.println("Invalid game was specified!");
            return;
        }

        for (String modelPack : modelPacks) {
            try {
                Chunk[] chunks = Chunk.loadChunks(modelPack);
                for (Chunk chunk : chunks) {
                    ResourceType type = ResourceType.fromValue(chunk.UID);
                    if (type == null) continue;
                    ResourceData resource = chunk.loadResource(type.getSerializable());
                    resources.put(resource.UID, resource);
                }
            } catch (Exception ex) {
                System.err.println("Failed to process all resource data in " + modelPack);
                return;
            }
        }

        for (int key : resources.keySet()) {
            ResourceData data = resources.get(key);
            if (!(data instanceof Model)) continue;
            byte[] glb = MeshExporter.getGLB(key, resources);
            if (glb == null) {
                System.err.print("[ERROR] Failed to export " + data.name);
                continue;
            }

            FileIO.write(glb, new File(folder, data.name + ".GLB").getAbsolutePath());
        }
    }
}
