package ufg.resources;

import java.util.ArrayList;

import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.structures.chunks.ResourceData;

public class SceneObject extends ResourceData {
    public static final int BASE_ALLOCATION_SIZE = 0x40 + 0x10;

    public SceneObject() {
        typeUID = 0x9B17FEDE;
    }

    public ArrayList<Integer> propertySets = new ArrayList<>();
    public boolean isMemImage = true;

    @SuppressWarnings("unchecked")
    @Override public SceneObject serialize(Serializer serializer, Serializable structure) {
        SceneObject object = (structure == null) ? new SceneObject() : (SceneObject) structure;

        super.serialize(serializer, object);
        if (!serializer.isWriting()) propertySets.clear();

        int objectOffset = serializer.getOffset();
        objectOffset += serializer.i32(0xc);

        int objectCount = serializer.i32(object.propertySets.size());
        object.isMemImage = serializer.bool(isMemImage);

        serializer.seek(objectOffset);
        for (int i = 0; i < objectCount; ++i) {
            serializer.pad(0xc);
            if (serializer.isWriting())
                serializer.getOutput().i32(object.propertySets.get(i));
            else
                object.propertySets.add(serializer.getInput().i32());
        }

        return object;
    }

    @Override public int getAllocatedSize() {
        int size = SceneObject.BASE_ALLOCATION_SIZE;
        size += 0x10 * propertySets.size();
        return size;
    }
}
