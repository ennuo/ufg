package ufg.resources;

import java.util.ArrayList;
import java.util.Collections;

import ufg.enums.GameVersion;
import ufg.enums.ResourceType;
import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.io.streams.MemoryInputStream;
import ufg.structures.HermiteCurve;
import ufg.structures.TrackSection;
import ufg.structures.chunks.Chunk;
import ufg.structures.chunks.ResourceData;
import ufg.util.ExecutionContext;
import ufg.util.FileIO;
import ufg.util.UFGCRC;

public class TrackDefinition extends ResourceData {
    public static final int BASE_ALLOCATION_SIZE = 0x1ac04;

    public static final int MAX_NUM_SPLINES = 5;
    public static final int MAX_NUM_CONTROL_POINTS = 256;
    public static final int MAX_NUM_TRACK_SECTIONS = 1000;
    
    public short flags;
    public short theme;
    public int numControlPoints;

    // RacelineCurve
    // RacelineSpline
    // Track
    // TrackLeftCurveData
    // TrackRightCurveData
    public final HermiteCurve[] track = new HermiteCurve[MAX_NUM_SPLINES];

    public ArrayList<TrackSection> sections = new ArrayList<>(MAX_NUM_TRACK_SECTIONS);

    public TrackDefinition() {
        name = "trackChunk";
        UID = UFGCRC.qStringHash32("trackChunk");
        typeUID = UFGCRC.qStringHash32("TrackDefinitionV7");
        flags = 0xf;
        for (int i = 0; i < MAX_NUM_SPLINES; ++i)
            track[i] = new HermiteCurve(MAX_NUM_CONTROL_POINTS);
    }

    public void reverse() {
        for (int i = 0; i < MAX_NUM_SPLINES; ++i)
            Collections.reverse(track[i].controlPoints);
    }
    
    @SuppressWarnings("unchecked")
    @Override public TrackDefinition serialize(Serializer serializer, Serializable structure) {
        TrackDefinition def = (structure == null) ? new TrackDefinition() : (TrackDefinition) structure;

        super.serialize(serializer, def);
        def.flags = serializer.i16(def.flags);
        def.theme = serializer.i16(def.theme);
        def.numControlPoints = serializer.i32(def.numControlPoints);
        for (int i = 0; i < MAX_NUM_SPLINES; ++i) 
            def.track[i].serialize(serializer, def.numControlPoints, MAX_NUM_CONTROL_POINTS);
        def.sections = serializer.arraylist(def.sections, TrackSection.class);
        serializer.pad((MAX_NUM_TRACK_SECTIONS - def.sections.size()) * TrackSection.BASE_ALLOCATION_SIZE);
        serializer.pad(0xc658); // Not sure anything in this section is even used, just padding?

        return def;
    }

    @Override public int getAllocatedSize() { return BASE_ALLOCATION_SIZE; }

    public static void main(String[] args) {
        ExecutionContext.Version = GameVersion.ModNation;
        Serializer serializer = new Serializer(new MemoryInputStream("C:/Users/Aidan/Desktop/track000.chk"));
        TrackDefinition def = serializer.struct(null, TrackDefinition.class);
        // def.reverse();

        // Y is left/right
        // Z is up/down
        // 0 is ???
        // 1 is the surface spline? for top of track?
        // 2 is also a surface spline? for bottom of track?
        // 3 is the right edge spline


        float[] scales = new float[] { 1.0f, 2.0f, 4.0f, 8.0f };
        for (int i = 0; i < MAX_NUM_SPLINES; ++i) 
            for (int j = 0; j < def.numControlPoints; ++j) {
                // def.track[i].controlPoints.get(j).position.z += 250.0f;
                //def.track[i].controlPoints.get(j).length.mul(25.0f);
                //def.track[i].controlPoints.get(j).length.z += (20.0f * (j % 2));

                def.track[i].controlPoints.get(j).position.y *= (float)scales[j % scales.length];
                def.track[i].controlPoints.get(j).length.y *= (float)scales[j % scales.length];

                // def.track[i].controlPoints.get(j).position.mul(0.1f);
            }

        // def.flags = 0x0f;
        // def.theme = 88;
        Chunk[] chunks = Chunk.loadChunks("E:\\emu\\rpcs3\\dev_hdd0\\game\\BCUS98167_UCC\\USRDIR\\1\\DATA\\LOCAL\\TRACK\\0000138C_EVENT.TRK");
        chunks[0] = Chunk.toChunk(ResourceType.TRACK_DEFINITION.getValue(), def);
        byte[] data = Chunk.saveChunks(chunks);
        FileIO.write(data, "E:\\emu\\rpcs3\\dev_hdd0\\game\\BCUS98167_UCC\\USRDIR\\1\\DATA\\LOCAL\\TRACK\\0000138C_EVENT.TRK");
        System.out.println("CRC64: " + UFGCRC.qFileHash64(data));
    }
}
