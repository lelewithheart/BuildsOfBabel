package com.babellibrary;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class CoordinateCodec {
    // Number of cells (x*z*y)
    public static final int SIZE = BuildGenerator.SIZE;
    public static final int HEIGHT = BuildGenerator.HEIGHT;
    public static final int CELLS = SIZE * SIZE * HEIGHT; // e.g. 16*16*16 = 4096

    // We serialize each cell as: [2-byte big-endian length][UTF-8 bytes of BlockData string]
    // patternToBigInteger will produce a BigInteger from the concatenated bytes.

    public static BigInteger patternToBigInteger(String[] blockDataStrings) {
        if (blockDataStrings.length != CELLS) throw new IllegalArgumentException("strings length");
        // compute total length
        int total = 0;
        byte[][] chunks = new byte[CELLS][];
        for (int i = 0; i < CELLS; i++) {
            byte[] bs = blockDataStrings[i] == null ? new byte[0] : blockDataStrings[i].getBytes(StandardCharsets.UTF_8);
            if (bs.length > 0xFFFF) throw new IllegalArgumentException("block data string too long");
            chunks[i] = bs;
            total += 2 + bs.length;
        }

        byte[] all = new byte[total];
        int off = 0;
        for (int i = 0; i < CELLS; i++) {
            byte[] bs = chunks[i];
            int len = bs.length;
            all[off++] = (byte) ((len >>> 8) & 0xFF);
            all[off++] = (byte) (len & 0xFF);
            if (len > 0) {
                System.arraycopy(bs, 0, all, off, len);
                off += len;
            }
        }

        return new BigInteger(1, all);
    }

    public static String[] bigIntegerToPattern(BigInteger s) {
        String[] out = new String[CELLS];
        if (s == null || s.equals(BigInteger.ZERO)) {
            for (int i = 0; i < CELLS; i++) out[i] = "minecraft:air";
            return out;
        }

        byte[] all = magnitudeBytes(s);
        int off = 0;
        for (int i = 0; i < CELLS; i++) {
            if (off + 2 <= all.length) {
                int len = ((all[off] & 0xFF) << 8) | (all[off + 1] & 0xFF);
                off += 2;
                if (len >= 0 && off + len <= all.length) {
                    out[i] = new String(all, off, len, StandardCharsets.UTF_8);
                    off += len;
                } else {
                    out[i] = "minecraft:air";
                    off = all.length; // force remainder default
                }
            } else {
                out[i] = "minecraft:air";
            }
        }
        return out;
    }

    private static byte[] magnitudeBytes(BigInteger s) {
        if (s == null) return new byte[0];
        byte[] t = s.toByteArray();
        if (t.length > 0 && t[0] == 0) {
            byte[] mag = new byte[t.length - 1];
            System.arraycopy(t, 1, mag, 0, mag.length);
            return mag;
        }
        return t;
    }

    // Split by bytes into three chunks (x||y||z order). Each returned BigInteger is the magnitude integer
    public static BigInteger[] splitToChunks(BigInteger s) {
        byte[] all = magnitudeBytes(s);
        int len = all.length;
        int part = (len + 2) / 3; // ceil(len/3)
        BigInteger[] out = new BigInteger[3];
        int off = 0;
        for (int i = 0; i < 3; i++) {
            int remaining = len - off;
            int take = Math.min(part, Math.max(0, remaining));
            if (take <= 0) {
                out[i] = BigInteger.ZERO;
            } else {
                byte[] slice = new byte[take];
                System.arraycopy(all, off, slice, 0, take);
                out[i] = new BigInteger(1, slice);
            }
            off += take;
        }
        return out;
    }

    // Recombine three BigIntegers by concatenating their magnitude bytes
    public static BigInteger combineChunks(BigInteger x, BigInteger y, BigInteger z) {
        byte[] xb = magnitudeBytes(x);
        byte[] yb = magnitudeBytes(y);
        byte[] zb = magnitudeBytes(z);
        byte[] all = new byte[xb.length + yb.length + zb.length];
        int off = 0;
        System.arraycopy(xb, 0, all, off, xb.length); off += xb.length;
        System.arraycopy(yb, 0, all, off, yb.length); off += yb.length;
        System.arraycopy(zb, 0, all, off, zb.length);
        return new BigInteger(1, all);
    }
}
