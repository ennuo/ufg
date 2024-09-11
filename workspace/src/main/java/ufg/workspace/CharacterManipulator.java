package ufg.workspace;

import java.io.File;
import java.util.ArrayList;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import ufg.gson.Matrix4fSerializer;
import ufg.gson.Vector3fSerializer;
import ufg.gson.Vector4fSerializer;
import ufg.resources.PartDefinition;
import ufg.structures.chunks.Chunk;
import ufg.util.CompressLZ;
import ufg.util.FileIO;

public class CharacterManipulator implements Launchable 
{
    public boolean validate(String[] args)
    {
        if (args.length > 2 || args.length < 0)
        {
            System.out.println("java -jar sav.jar <.sav> <.json>");
            System.out.println("java -jar sav.jar <.json> <.jsav>");
            return false;
        }

        if (!(new File(args[0]).exists()))
        {
            System.out.println(args[0] + " does not exist!");
            return false;
        }

        return true;
    }

    public void launch(String[] args) 
    {
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeSpecialFloatingPointValues()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(Vector4f.class, new Vector4fSerializer())
            .registerTypeAdapter(Vector3f.class, new Vector3fSerializer())
            .registerTypeAdapter(Matrix4f.class, new Matrix4fSerializer())
            .create();

        if (args[0].toLowerCase().endsWith(".json"))
        {
            ArrayList<PartDefinition> parts = gson.fromJson(new String(FileIO.read(args[0])), new TypeToken<ArrayList<PartDefinition>>(){}.getType());
            ArrayList<Chunk> chunks = new ArrayList<>();

            for (PartDefinition part : parts)
            {
                part.typeUID = 0x8509D307;
                chunks.add(Chunk.toChunk(0x8509D307, part));
            }

            for (Chunk chunk : Chunk.loadChunks(args[1]))
            {
                if (chunk.UID != 0x8509D307)
                    chunks.add(chunk);
            }

            byte[] data = CompressLZ.getFakeCompression(Chunk.saveChunks(chunks.toArray(Chunk[]::new)));
            FileIO.write(data, args[1]);
        }
        else
        {
            ArrayList<PartDefinition> parts = new ArrayList<>();
            Chunk[] chunks = Chunk.loadChunks(args[0]);
            for (Chunk chunk : chunks)
            {
                if (chunk.UID == 0x8509D307)
                    parts.add(chunk.loadResource(PartDefinition.class));
            }
            String json = gson.toJson(parts);
            FileIO.write(json.getBytes(), args[1]);
        }
    }
}
