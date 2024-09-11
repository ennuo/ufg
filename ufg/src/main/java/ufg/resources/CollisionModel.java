package ufg.resources;

import ufg.util.Bytes;
import ufg.util.UFGCRC;

public class CollisionModel {
    // int UID [same as model]
    // int mGeometryType
    // int mNumVertices
    // int mNumIndices

    // CollisionMesh itself is 0x17c bytes

    // indexData always starts at 0x18c

    // 0x50 - Always 1?

    // I think most of the data doesn't matter
    // + 0x40 = UID
    // + 0x44 = GeometryType? (0)
    // + 0x48 = mNumVertices
    // + 0x4c = mNumIndices
    // + 0x50 = mNumParts(?) (1 generally)
        // 0x50 -> 0x11c, 8 parts maximum?
        // + 0x54 = mNumTriangles
    // + 0x11c ???
    // + 0x144 = 1.0
    // + 0x154 = 1.0
    // + 0x164 = 1.0
    
    // BufferData begins at 0x17c
        // index buffer [0x2 * mNumVertices]
        // ALIGN TO 0X10 BYTES
        // vertex buffer [ 0xc * mNumVertices ]
        // ALIGN TO 0X10 BYTES
        // ???

    // 0x695
    // 0X2052

    public static void main(String[] args) {
        System.out.println(Bytes.toHex(UFGCRC.qStringHash32("collisionLayerName")));
    }


}
