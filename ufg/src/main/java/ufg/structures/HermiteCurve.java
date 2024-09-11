package ufg.structures;

import java.util.ArrayList;

import ufg.io.Serializer;
import ufg.io.streams.MemoryInputStream;
import ufg.io.streams.MemoryOutputStream;

public class HermiteCurve {
    public static int BASE_ALLOCATION_SIZE = 0x10;
    public ArrayList<HermiteControlPoint> controlPoints;
    
    public HermiteCurve(int maxNumControlPoints) {
        controlPoints = new ArrayList<>(maxNumControlPoints);
    }

    public void serialize(Serializer serializer, int numControlPoints, int maxNumControlPoints) {
        int sectionPadSize = (maxNumControlPoints - numControlPoints) * 0xc;
        if (serializer.isWriting()) {
            MemoryOutputStream stream = serializer.getOutput();
            for (HermiteControlPoint point : controlPoints)
                stream.v3(point.position);
            serializer.pad(sectionPadSize);
            for (HermiteControlPoint point : controlPoints)
                stream.v3(point.length);
            serializer.pad(sectionPadSize);
            return;
        }

        MemoryInputStream stream = serializer.getInput();
        controlPoints = new ArrayList<>(maxNumControlPoints);
        for (int i = 0; i < numControlPoints; ++i)
            controlPoints.add(new HermiteControlPoint(stream.v3()));
        stream.forward(sectionPadSize);
        for (int i = 0; i < numControlPoints; ++i)
            controlPoints.get(i).length = stream.v3();
        stream.forward(sectionPadSize);
    }
}
