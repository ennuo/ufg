package ufg.util;

import ufg.io.streams.MemoryInputStream;

public class DecompressLZ {
    public static byte[] decompress(byte[] data) {
        MemoryInputStream stream = new MemoryInputStream(data);
        String magic = stream.str(4);
        
        if (magic.equals("PMCQ")) stream.setLittleEndian(true);
        else if (!magic.equals("QCMP"))
            return data;
        
        stream.u16(); // type
        stream.u16(); // version
        
        int dataOffset = stream.i32();
        int inPlaceExtraNumBytes = stream.i32();
        int compressedNumBytes = (int) stream.i64();
        int uncompressedNumBytes = (int) stream.i64();
        long uncompressedChecksum = stream.i64();

        int dataSize = compressedNumBytes - dataOffset;

        byte[] decompressedData = new byte[uncompressedNumBytes];
        int[] cache = new int[32];
        int current = 0;

        int i = dataOffset, j = 0;
        while (i < (dataOffset + dataSize)) {
            int b = data[i++] & 0xff;

            if (b < 0x20) {
                int count = b + 1;

                System.arraycopy(data, i, decompressedData, j, count);
                i += count;
                j += count;

                continue;
            }

            int size = 0, offset = 0;

            int c = (b >> 5);
            b &= 0x1f;

            if (c == 1) {
                offset = cache[b] & 0xffff;
                size = cache[b] >> 0x10;
            } else {
                int n = data[i++] & 0xff;
                int f = b << 8;
                offset = f | n;

                if (c == 7) c = data[i++] & 0xff;
                size = c + 1;

                cache[current++ % 0x20] = size * 0x10000 | f | n;
            }

            int start = (j - offset);
            for (int index = start; index < (start + size); ++index)
                decompressedData[j++] = decompressedData[index];
        }

        // if (uncompressedChecksum != UFGCRC.qFileHash64(decompressedData))
        //     throw new RuntimeException("Checksum doesn't match for decompressed data!");

        return decompressedData;
    }
}
