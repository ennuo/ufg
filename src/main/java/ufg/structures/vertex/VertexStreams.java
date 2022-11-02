package ufg.structures.vertex;

import java.util.HashMap;

import ufg.enums.VertexStreamElementType;
import ufg.enums.VertexStreamElementUsage;

public class VertexStreams {
    private static final HashMap<Integer, VertexStreamDescriptor> UID_LOOKUP = new HashMap<>();
    private static final HashMap<String, VertexStreamDescriptor> NAME_LOOKUP = new HashMap<>();

    public static VertexStreamDescriptor get(String name) { return VertexStreams.NAME_LOOKUP.get(name); }
    public static VertexStreamDescriptor get(int uid) { return VertexStreams.UID_LOOKUP.get(uid); }

    private static VertexStreamDescriptor add(String name, int uid) {
        VertexStreamDescriptor stream = new VertexStreamDescriptor(name, uid);
        VertexStreams.UID_LOOKUP.put(uid, stream);
        VertexStreams.NAME_LOOKUP.put(name, stream);
        return stream;
    }

    static {
        VertexStreamDescriptor stream = VertexStreams.add("VertexDecl.SkinnedCol", 0xc3092f93);

        stream.addElement(
            VertexStreamElementUsage.POSITION, 
            VertexStreamElementType.SHORT4_FIXED4_12, 
            0
        );

        stream.addElement(
            VertexStreamElementUsage.NORMAL, 
            VertexStreamElementType.I11_11_10N, 
            0
        );

        stream.addElement(
            VertexStreamElementUsage.TANGENT, 
            VertexStreamElementType.BYTE4N, 
            0
        );

        stream.addElement(
            VertexStreamElementUsage.BLENDINDEX, 
            VertexStreamElementType.UBYTE4, 
            1
        );
        stream.addElement(
            VertexStreamElementUsage.BLENDWEIGHT, 
            VertexStreamElementType.UBYTE4N, 
            1
        );

        stream.addElement(
            VertexStreamElementUsage.TEXCOORD0, 
            VertexStreamElementType.HALF2, 
            2
        );

        stream.addElement(
            VertexStreamElementUsage.COLOR0, 
            VertexStreamElementType.COLOR4, 
            2
        );
    }

    static {
        VertexStreamDescriptor stream = VertexStreams.add("VertexDecl.Skinned", 0x276b9567);

        stream.addElement(
            VertexStreamElementUsage.POSITION, 
            VertexStreamElementType.SHORT4_FIXED4_12, 
            0
        );

        stream.addElement(
            VertexStreamElementUsage.NORMAL, 
            VertexStreamElementType.I11_11_10N, 
            0
        );

        stream.addElement(
            VertexStreamElementUsage.TANGENT, 
            VertexStreamElementType.BYTE4N, 
            0
        );

        stream.addElement(
            VertexStreamElementUsage.BLENDINDEX, 
            VertexStreamElementType.UBYTE4, 
            1
        );
        stream.addElement(
            VertexStreamElementUsage.BLENDWEIGHT, 
            VertexStreamElementType.UBYTE4N, 
            1
        );

        stream.addElement(
            VertexStreamElementUsage.TEXCOORD0, 
            VertexStreamElementType.HALF2, 
            2
        );
    }

    static {
        VertexStreamDescriptor stream = VertexStreams.add("VertexDecl.AccurateWorld", 0x3850D39D);
    
        stream.addElement(
            VertexStreamElementUsage.POSITION, 
            VertexStreamElementType.FLOAT3, 
            1
        );

        stream.addElement(
            VertexStreamElementUsage.TEXCOORD0, 
            VertexStreamElementType.HALF2, 
            2
        );

        stream.addElement(
            VertexStreamElementUsage.TEXCOORD1, 
            VertexStreamElementType.HALF2, 
            2
        );

        stream.addElement(
            VertexStreamElementUsage.NORMAL, 
            VertexStreamElementType.I11_11_10N, 
            0
        );

        stream.addElement(
            VertexStreamElementUsage.TANGENT, 
            VertexStreamElementType.I11_11_10N, 
            0
        );

        stream.addElement(
            VertexStreamElementUsage.COLOR0, 
            VertexStreamElementType.COLOR4, 
            2
        );
    }

    static {
        VertexStreamDescriptor stream = VertexStreams.add("VertexDecl.KartWorld", 0x281cc2b5);

        stream.addElement(
            VertexStreamElementUsage.POSITION, 
            VertexStreamElementType.HALF3, 
            1
        );

        stream.addElement(
            VertexStreamElementUsage.TEXCOORD0, 
            VertexStreamElementType.HALF2, 
            2
        );

        stream.addElement(
            VertexStreamElementUsage.TEXCOORD1, 
            VertexStreamElementType.HALF2, 
            2
        );

        stream.addElement(
            VertexStreamElementUsage.NORMAL, 
            VertexStreamElementType.I11_11_10N, 
            0
        );

        stream.addElement(
            VertexStreamElementUsage.TANGENT, 
            VertexStreamElementType.I11_11_10N, 
            0
        );

        stream.addElement(
            VertexStreamElementUsage.COLOR0, 
            VertexStreamElementType.COLOR4, 
            2
        );

    }

    static {
        VertexStreamDescriptor stream = VertexStreams.add("VertexDecl.KartWorldSoft", 0xb9ee1f83);

        stream.addElement(
            VertexStreamElementUsage.POSITION, 
            VertexStreamElementType.HALF4, 
            1
        );

        stream.addElement(
            VertexStreamElementUsage.TEXCOORD0, 
            VertexStreamElementType.HALF2, 
            2
        );

        stream.addElement(
            VertexStreamElementUsage.TEXCOORD1, 
            VertexStreamElementType.HALF2, 
            2
        );

        stream.addElement(
            VertexStreamElementUsage.NORMAL, 
            VertexStreamElementType.I11_11_10N, 
            0
        );

        stream.addElement(
            VertexStreamElementUsage.TANGENT, 
            VertexStreamElementType.I11_11_10N, 
            0
        );

        stream.addElement(
            VertexStreamElementUsage.COLOR0, 
            VertexStreamElementType.COLOR4, 
            2
        );
    }

    static {
        VertexStreamDescriptor stream = VertexStreams.add("VertexDecl.KartWorld1UV", 0x8899a34f);

        stream.addElement(
            VertexStreamElementUsage.POSITION, 
            VertexStreamElementType.HALF3, 
            1
        );

        stream.addElement(
            VertexStreamElementUsage.TEXCOORD0, 
            VertexStreamElementType.HALF2, 
            2
        );

        stream.addElement(
            VertexStreamElementUsage.NORMAL, 
            VertexStreamElementType.I11_11_10N, 
            0
        );

        stream.addElement(
            VertexStreamElementUsage.TANGENT, 
            VertexStreamElementType.I11_11_10N, 
            0
        );

        stream.addElement(
            VertexStreamElementUsage.COLOR0, 
            VertexStreamElementType.COLOR4, 
            2
        );
    }

    static {
        VertexStreamDescriptor stream = VertexStreams.add("VertexDecl.KartWorld1UVSoft", 0xac3891d);

        stream.addElement(
            VertexStreamElementUsage.POSITION, 
            VertexStreamElementType.HALF4, 
            1
        );

        stream.addElement(
            VertexStreamElementUsage.TEXCOORD0, 
            VertexStreamElementType.HALF2, 
            2
        );

        stream.addElement(
            VertexStreamElementUsage.NORMAL, 
            VertexStreamElementType.I11_11_10N, 
            0
        );

        stream.addElement(
            VertexStreamElementUsage.TANGENT, 
            VertexStreamElementType.I11_11_10N, 
            0
        );

        stream.addElement(
            VertexStreamElementUsage.COLOR0, 
            VertexStreamElementType.COLOR4, 
            2
        );
    }

    static {
        VertexStreamDescriptor stream = VertexStreams.add("VertexDecl.Terrain", 0xccb11bc6);

        stream.addElement(
            VertexStreamElementUsage.POSITION, 
            VertexStreamElementType.HALF4, 
            0
        );

        stream.addElement(
            VertexStreamElementUsage.TEXCOORD0, 
            VertexStreamElementType.HALF2, 
            1
        );
    }

    static {
        VertexStreamDescriptor stream = VertexStreams.add("VertexDecl.MorphTarget", 0x7029f5ba);

        stream.addElement(
            VertexStreamElementUsage.POSITION, 
            VertexStreamElementType.SHORT4_FIXED4_12, 
            0
        );

        stream.addElement(
            VertexStreamElementUsage.NORMAL, 
            VertexStreamElementType.I11_11_10N, 
            0
        );
    }

    static {
        VertexStreamDescriptor stream = VertexStreams.add("VertexDecl.UVNTC", 0xe234ef7a);

        stream.addElement(
            VertexStreamElementUsage.POSITION, 
            VertexStreamElementType.SHORT4_FIXED4_12, 
            0
        );

        stream.addElement(
            VertexStreamElementUsage.NORMAL, 
            VertexStreamElementType.I11_11_10N, 
            0
        );

        stream.addElement(
            VertexStreamElementUsage.TANGENT, 
            VertexStreamElementType.UBYTE4, 
            0
        );

        stream.addElement(
            VertexStreamElementUsage.BLENDINDEX, 
            VertexStreamElementType.UBYTE4, 
            1
        );

        stream.addElement(
            VertexStreamElementUsage.BLENDWEIGHT, 
            VertexStreamElementType.UBYTE4N, 
            1
        );

        stream.addElement(
            VertexStreamElementUsage.TEXCOORD0, 
            VertexStreamElementType.HALF2, 
            2
        );

    }

}
