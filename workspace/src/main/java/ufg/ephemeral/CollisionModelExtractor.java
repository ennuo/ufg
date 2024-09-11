package ufg.ephemeral;

import ufg.io.streams.MemoryInputStream;
import ufg.util.Bytes;
import ufg.util.FileIO;
import ufg.util.UFGCRC;

public class CollisionModelExtractor {
    public static void main(String[] args) {
        System.out.println(Bytes.toHex(UFGCRC.qStringHashUpper32("Suspension_Hover_Body_Feisar_Collision")));
        byte[] data = FileIO.read("C:/Users/Aidan/Desktop/PHYSICS.BIN");
    
        MemoryInputStream stream = new MemoryInputStream(data);
        stream.seek(0x18c);

        
        StringBuilder builder = new StringBuilder(0x10000);
        for (int i = 0; i < 0x2052; i += 3)
            builder.append("f " + (stream.u16() + 1) + " " + (stream.u16() + 1) + " " + (stream.u16() + 1) + "\n");
        // 0xc bytes for each attribute?
        for (int i = 0; i < 0x0695; ++i)
            builder.append("v " + stream.f32() + " " + stream.f32() + " " + stream.f32() + "\n");

        // System.out.println(builder.toString());
        FileIO.write(builder.toString().getBytes(), "C:/Users/Aidan/Desktop/COLLISION.OBJ");



    }
}
