package ufg.gson;

import java.lang.reflect.Type;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import ufg.enums.PropertyType;
import ufg.ex.SerializationException;
import ufg.structures.Property;


public class PropertySerializer implements JsonSerializer<Property>, JsonDeserializer<Property> {
    @Override public Property deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        Property property = new Property();
        JsonObject object = je.getAsJsonObject();

        if (object.has("type")) {
            property.type = jdc.deserialize(object.get("type"), PropertyType.class);            
            switch (property.type) {
                case BOOL:
                    property.value = jdc.deserialize(object.get("value"), Boolean.class);
                    break;
                case INT8:
                case UINT8:
                    property.value = jdc.deserialize(object.get("value"), Byte.class);
                    break;
                case INT16:
                case UINT16:
                    property.value = jdc.deserialize(object.get("value"), Short.class);
                    break;
                case INT32:
                case UINT32:
                    property.value = jdc.deserialize(object.get("value"), Integer.class);
                    break;
                case INT64:
                    property.value = jdc.deserialize(object.get("value"), Long.class);
                    break;
                case FLOAT:
                    property.value = jdc.deserialize(object.get("value"), Float.class);
                    break;
                case STRING:
                    property.value = jdc.deserialize(object.get("value"), String.class);
                    break;
                case MATRIX44:
                    property.value = jdc.deserialize(object.get("value"), Matrix4f.class);
                    break;
                case VECTOR2:
                    property.value = jdc.deserialize(object.get("value"), Vector2f.class);
                    break;
                case VECTOR3:
                    property.value = jdc.deserialize(object.get("value"), Vector3f.class);
                    break;
                case VECTOR4:
                    property.value = jdc.deserialize(object.get("value"), Vector4f.class);
                    break; 
                default:
                    throw new SerializationException("Unsupported PropertyType in Property! " + property.type.toString());
            }
        }

        return property;
    }

    @Override public JsonElement serialize(Property property, Type type, JsonSerializationContext jsc) {
        JsonObject object = new JsonObject();
        object.add("type", jsc.serialize(property.type));
        object.add("value", jsc.serialize(property.value));
        return object;
    }
}
