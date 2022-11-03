package ufg.resources;

import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.enums.PartAttrib;
import ufg.structures.chunks.ResourceData;
import ufg.util.UFGCRC;

public class PartDB extends ResourceData {
    public static final int BASE_ALLOCATION_SIZE = 
        ResourceData.BASE_ALLOCATION_SIZE + 0x10;

    public static class PartValue implements Serializable {
        public static final int BASE_ALLOCATION_SIZE = 0x4;

        public PartAttrib type = PartAttrib.INT;
        public Object value = 0;

        public PartValue() {};
        public PartValue(PartAttrib type, Object value) {
            this.type = type;
            this.value = value;
        }

        @SuppressWarnings("unchecked")
        @Override public PartValue serialize(Serializer serializer, Serializable structure) {
            PartValue part = (structure == null) ? new PartValue() : (PartValue) structure;

            part.type = serializer.enum32(part.type);
            switch (part.type) {
                case INT: case UID: 
                    part.value = serializer.i32(serializer.isWriting() ? (int) part.value : 0); 
                    break;
                case FLOAT:
                    part.value = serializer.f32(serializer.isWriting() ? (float) part.value : 0.0f);
                    break;
                case VEC2:
                    part.value = serializer.v2(serializer.isWriting() ? (Vector2f) part.value : null);
                    break;
                case VEC3:
                    part.value = serializer.v3(serializer.isWriting() ? (Vector3f) part.value : null);
                    break;
                case VEC4:
                    part.value = serializer.v4(serializer.isWriting() ? (Vector4f) part.value : null);
                    break;
                case MATRIX:
                    part.value = serializer.m44(serializer.isWriting() ? (Matrix4f) part.value : null);
                    break;
                case STRING:
                    part.value = serializer.cstr(serializer.isWriting() ? (String) part.value : null);
                    serializer.align(0x4);
                    break;
                default:
                    throw new RuntimeException("Unhandled PartAttrib!");
            }

            return part;
        }

        @Override public int getAllocatedSize() {
            int size = PartValue.BASE_ALLOCATION_SIZE;
            switch (this.type) {
                case INT: case UID: case FLOAT: return size + 0x4;
                case VEC2: return size + 0x8;
                case VEC3: return size + 0xC;
                case VEC4: return size + 0x10;
                case MATRIX: return size + 0x40;
                case STRING: return size + (((String)this.value).length()) + 0x10;
                default: return size + 0x4;
            }
        }


    }

    public static class Part implements Serializable {
        public static final int BASE_ALLOCATION_SIZE = 0x10;

        public String name;
        private HashMap<Integer, PartValue> properties = new HashMap<>();

        public PartValue get(String property) { return this.properties.get(UFGCRC.qStringHash32(property.toUpperCase())); }
        public PartValue get(int property) { return this.properties.get(property); }

        public void set(String property, PartValue value) { this.properties.put(UFGCRC.qStringHash32(property.toUpperCase()), value); }
        public void set(int property, PartValue value) { this.properties.put(property, value); }

        @SuppressWarnings("unchecked")
        @Override public Part serialize(Serializer serializer, Serializable structure) {
            Part part = (structure == null) ? new Part() : (Part) structure;

            int propertyCount = serializer.i32(serializer.isWriting() ? part.properties.size() : 0);
            part.name = serializer.cstr(part.name);
            serializer.align(0x4);


            if (serializer.isWriting()) {
                for (Integer key : part.properties.keySet()) {
                    serializer.getOutput().i32(key);
                    serializer.struct(part.properties.get(key), PartValue.class);
                }

                return part;
            }

            this.properties = new HashMap<>(propertyCount);
            for (int i = 0; i < propertyCount; ++i) 
                this.properties.put(serializer.getInput().i32(), serializer.struct(null, PartValue.class));

            return part;
        }
    
        @Override public int getAllocatedSize() { 
            int size = Part.BASE_ALLOCATION_SIZE; 

            size += (this.name.length() + 0x10);
            for (PartValue value : this.properties.values())
                size += value.getAllocatedSize() + 0x4;

            return size;
        }


    }

    public ArrayList<Part> parts = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Override public PartDB serialize(Serializer serializer, Serializable structure) {
        PartDB database = (structure == null) ? new PartDB() : (PartDB) structure;

        super.serialize(serializer, database);

        database.parts = serializer.arraylist(database.parts, Part.class);
        serializer.align(0x10);

        return database;
    }

    @Override public int getAllocatedSize() { 
        int size = PartDB.BASE_ALLOCATION_SIZE;
        for (Part part : this.parts)
            size += part.getAllocatedSize();
        return size;
    }

    public static class SlotDB extends ResourceData {
        public static final int BASE_ALLOCATION_SIZE = 0x40;

        public static class SlotEntry implements Serializable {
            public static final int BASE_ALLOCATION_SIZE = 0x40;
            
            public int uid;
            public int type;
            public String name;
            public ArrayList<Integer> uids = new ArrayList<>();

            @SuppressWarnings("unchecked")
            @Override public SlotEntry serialize(Serializer serializer, Serializable structure) {
                SlotEntry entry = (structure == null) ? new SlotEntry() : (SlotEntry) structure;
    
                entry.uid = serializer.i32(entry.uid);
                entry.type = serializer.i32(entry.type);
                int count = serializer.i32(serializer.isWriting() ? entry.uids.size() : 0);
                entry.name = serializer.cstr(entry.name);
                serializer.align(0x4);
                
                if (!serializer.isWriting()) {

                    entry.uids = new ArrayList<>(count);
                    for (int i = 0; i < count; ++i)
                        entry.uids.add(serializer.getInput().i32());

                } else {

                    for (int i = 0; i < count; ++i)
                        serializer.getOutput().i32(entry.uids.get(i));

                }
                
                return entry;
            }
    
            @Override public int getAllocatedSize() { 
                int size = SlotEntry.BASE_ALLOCATION_SIZE;
                return size;
            }


        }

        public SlotEntry[] entries;

        @SuppressWarnings("unchecked")
        @Override public SlotDB serialize(Serializer serializer, Serializable structure) {
            SlotDB db = (structure == null) ? new SlotDB() : (SlotDB) structure;

            super.serialize(serializer, db);
            db.entries = serializer.array(db.entries, SlotEntry.class);
            serializer.align(0x10);

            return db;
        }

        @Override public int getAllocatedSize() { 
            int size = SlotDB.BASE_ALLOCATION_SIZE + 0xFFFFFF;
            return size;
        }

    }
}
