package ufg.workspace;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ufg.gson.Matrix4fSerializer;
import ufg.gson.Vector3fSerializer;
import ufg.gson.Vector4fSerializer;
import ufg.resources.PropertySet;
import ufg.resources.SceneObject;
import ufg.structures.Property;
import ufg.structures.chunks.Chunk;
import ufg.util.FileIO;

public class SceneExtractor implements Launchable 
{
    public boolean validate(String[] args)
    {
        if (args.length > 2 || args.length < 0)
        {
            System.out.println("java -jar scene.jar <.bin> <.json>");
            System.out.println("java -jar scene.jar <.json> <.bin>");
            return false;
        }

        if (!(new File(args[0]).exists()))
        {
            System.out.println(args[0] + " does not exist!");
            return false;
        }

        return true;
    }

    public static class tProperty {
        public String name;
        public int uid;
        public int parentUID;
        public HashMap<Integer, Property> properties = new HashMap<>();

        public tProperty(PropertySet set) {
            this.name = set.name;
            this.uid = set.UID;
            this.parentUID = set.parentHandle;
            this.properties = set.properties;
        }
    }

    public static class tSceneObject {
        public String name;
        public int uid;
        public ArrayList<tProperty> properties = new ArrayList<>();

        public tSceneObject(Chunk[] chunks, Chunk chunk) {
            SceneObject object = chunk.loadResource(SceneObject.class);
            this.name = object.name;
            this.uid = object.UID;
            for (int uid : object.propertySets)  {

                Chunk propChunk = chunks[Chunk.findChunkWithUID(chunks, uid)];
                properties.add(new tProperty(propChunk.loadResource(PropertySet.class)));
            }
        }
    }

    public void launch(String[] args) 
    {
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeSpecialFloatingPointValues()
            .registerTypeAdapter(Vector4f.class, new Vector4fSerializer())
            .registerTypeAdapter(Vector3f.class, new Vector3fSerializer())
            .registerTypeAdapter(Matrix4f.class, new Matrix4fSerializer())
            .create();

        ArrayList<tSceneObject> objects = new ArrayList<>();
        Chunk[] chunks = Chunk.loadChunks(args[0]);
        for (int i = 0; i < chunks.length; ++i) {
            Chunk chunk = chunks[i];
            if (chunk.UID != 0x9B17FEDE) continue;
            objects.add(new tSceneObject(chunks, chunk));
        }


        String json = gson.toJson(objects);
        FileIO.write(json.getBytes(), args[1]);
    }
}
