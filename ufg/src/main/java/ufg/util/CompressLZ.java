package ufg.util;

import ufg.io.streams.MemoryOutputStream;

public class CompressLZ {
    public static enum EncodeType {
        LITERAL,
        MATCH
    };

    public byte[][] htab2 = new byte[4096][32];
    public byte[] hpos2 = new byte[4096];
    public int compressedLength;
    public int outputPosition;
    public int inPlacePosition;
    public int inPlacePadding;

    public boolean compress() {
        return false;
    }

    public boolean flush() {
        return false;
    }

    public boolean finish() {
        return false;
    }

    public static byte[] getFakeCompression(byte[] decompressedData)
    {
        int literalCount = (decompressedData.length + 0x20 - 1) / 0x20;
        
        MemoryOutputStream stream = new MemoryOutputStream(literalCount + decompressedData.length + 0x40);
        stream.setLittleEndian(false);
        
        stream.str("QCMP", 4);
        stream.i32(0x00010001);
        stream.i32(0x40);
        stream.i32(1);
        stream.i64(stream.getLength());
        stream.i64(decompressedData.length);
        stream.i64(UFGCRC.qFileHash64(decompressedData));
        stream.pad(0x18);

        int offset = 0;
        while (offset < decompressedData.length)
        {
            int count = Math.min(0x20, decompressedData.length - offset);
            stream.u8(count - 1);
            stream.bytes(decompressedData, offset, count);
            offset += 0x20;
        }

        return stream.getBuffer();
    }

    public static void main(String[] args) {
        byte[] data = getFakeCompression(FileIO.read("C:/Users/Aidan/Desktop/COLIN_000013D8 - Copy.TRK"));
        FileIO.write(data, "C:/~/Users/Aidan/Desktop/COLIN.TRK");
    }
}
