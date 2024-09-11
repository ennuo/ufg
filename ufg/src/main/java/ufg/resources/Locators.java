package ufg.resources;

import java.util.HashMap;
import java.util.Map.Entry;

import org.joml.Matrix4f;

import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.io.streams.MemoryInputStream;
import ufg.io.streams.MemoryOutputStream;
import ufg.structures.chunks.ResourceData;

public class Locators extends ResourceData {
    public static final int BASE_ALLOCATION_SIZE = 0x40 + 0x10;

    public HashMap<String, Matrix4f> locators = new HashMap<>();

    public Locators() { typeUID = 0x3eddeff0; }

    @SuppressWarnings("unchecked")
    @Override public Locators serialize(Serializer serializer, Serializable structure) {
        Locators loc = (structure == null) ? new Locators() : (Locators) structure;

        super.serialize(serializer, loc);

        int numLocators = serializer.i32(loc.locators.size());
        int nameTableOffset = serializer.getOffset();
        nameTableOffset += serializer.i32(0xC);
        int transformOffset = serializer.getOffset();
        transformOffset += serializer.i32((loc.locators.size() * 0x40) + 0x8);

        if (serializer.isWriting()) {
            int offset = 0;
            MemoryOutputStream stream = serializer.getOutput();
            for (Entry<String, Matrix4f> locator : loc.locators.entrySet()) {
                stream.seek(nameTableOffset + offset);
                stream.str(locator.getKey(), 0x40);
                stream.seek(transformOffset + offset);
                stream.m44(locator.getValue());
                offset += 0x40;
            }
        }
        else {
            loc.locators.clear();
            MemoryInputStream stream = serializer.getInput();
            for (int i = 0, offset = 0; i < numLocators; ++i, offset += 0x40) {
                stream.seek(nameTableOffset + offset);
                String key = stream.str(0x40);
                stream.seek(transformOffset + offset);
                Matrix4f value = stream.m44();
                loc.locators.put(key, value);
            }
        }

        return loc;
    }
    
    @Override public int getAllocatedSize() { 
        int size = Locators.BASE_ALLOCATION_SIZE;
        size += (locators.size() * (0x40 + 0x40));
        return size;
    }
}
