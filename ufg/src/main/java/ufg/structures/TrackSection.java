package ufg.structures;

import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.util.ExecutionContext;

public class TrackSection implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x1c;
    
    public short flags;
    public float offset;
    public int track;
    public int trackObjectUID;

    @SuppressWarnings("unchecked")
    @Override public TrackSection serialize(Serializer serializer, Serializable structure) {
        TrackSection section = structure == null ? new TrackSection() : (TrackSection) structure;

        
        serializer.u16(ExecutionContext.isModNation() ? 0x4 : 0x0); // This is always constantly set as these values
        section.flags = serializer.i16(section.flags);

        if (ExecutionContext.isModNation()) {
            section.trackObjectUID = serializer.i32(section.trackObjectUID);
            section.offset = serializer.f32(section.offset);
            serializer.pad(0x10);
        }
        else {
            serializer.i32(0); // unsure
            section.offset = serializer.f32(section.offset);
            section.track = serializer.i32(section.track);
            section.trackObjectUID = serializer.i32(section.trackObjectUID);
            section.trackObjectUID = serializer.i32(section.trackObjectUID); // This is technically supposed to be separate, but I've always seen it as the same variable
            serializer.i32(0); // Padding
        }


        return section;
    }

    @Override public int getAllocatedSize() { return BASE_ALLOCATION_SIZE; }
}
