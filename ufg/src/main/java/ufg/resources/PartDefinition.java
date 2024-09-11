package ufg.resources;

import org.joml.Matrix4f;

import com.google.gson.annotations.JsonAdapter;

import ufg.gson.SlotSerializer;
import ufg.gson.TranslationSerializer;
import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.structures.chunks.ResourceData;

public class PartDefinition extends ResourceData {
    public static final int BASE_ALLOCATION_SIZE = 0x98;


    @JsonAdapter(SlotSerializer.class)
    public int name; // Slot_Char_Eyes (qStringUpperHash)
    @JsonAdapter(SlotSerializer.class)
    public int slotName; // eyes_cats_fe
    @JsonAdapter(TranslationSerializer.class)
    public Matrix4f transform;

    public int parent = -1;

    @SuppressWarnings("unchecked")
    @Override public PartDefinition serialize(Serializer serializer, Serializable structure) {
        PartDefinition part = (structure == null) ? new PartDefinition() : (PartDefinition) structure;

        super.serialize(serializer, part);

        part.name = serializer.i32(part.name);
        part.slotName = serializer.i32(part.slotName);
        part.transform = serializer.m44(part.transform);
        
        part.parent = serializer.i32(part.parent);
        serializer.i32(-1);
        serializer.i32(-1);

        serializer.i32(0); // Padding?
        
        return part;
    }

    @Override public int getAllocatedSize() { 
        return PartDefinition.BASE_ALLOCATION_SIZE; 
    }
}
