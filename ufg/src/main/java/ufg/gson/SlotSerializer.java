package ufg.gson;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import ufg.util.FileIO;
import ufg.util.UFGCRC;

public class SlotSerializer implements JsonSerializer<Integer>, JsonDeserializer<Integer> {
    public static HashMap<Integer, String> NAMES = new GsonBuilder().create().fromJson(FileIO.getResourceFileAsString("/names.json"), new TypeToken<Map<Integer, String>>(){}.getType());

    @Override public Integer deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        JsonPrimitive prim = je.getAsJsonPrimitive();
        if (prim.isString()) 
            return UFGCRC.qStringHashUpper32(prim.getAsString());
        if (prim.isNumber())
            return prim.getAsInt();
        return 0;
    }

    @Override public JsonElement serialize(Integer value, Type type, JsonSerializationContext jsc) {
        if (NAMES.containsKey(value))
            return new JsonPrimitive(NAMES.get(value));
        return new JsonPrimitive(value);
    }
}
