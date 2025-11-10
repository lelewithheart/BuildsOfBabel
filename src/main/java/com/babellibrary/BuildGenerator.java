package com.babellibrary;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.math.BigInteger;

public class BuildGenerator {
    public static final int SIZE = 16; // X/Z
    public static final int HEIGHT = 16; // Y

    // Note: We now support every block variant by serializing BlockData strings.

    // Clear the 16x16xHEIGHT room around origin (origin is the corner at lowest x,y,z)
    public static void clearRoom(World world, Location origin) {
        int baseX = origin.getBlockX();
        int baseY = origin.getBlockY();
        int baseZ = origin.getBlockZ();

        // Clear interior and ceiling
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                for (int y = 0; y < HEIGHT + 2; y++) {
                    Block b = world.getBlockAt(baseX + x, baseY + y, baseZ + z);
                    b.setType(Material.AIR);
                }
            }
        }

        // Floor and walls (one-block thick)
        for (int x = -1; x <= SIZE; x++) {
            for (int z = -1; z <= SIZE; z++) {
                // floor
                Block floor = world.getBlockAt(baseX + x, baseY - 1, baseZ + z);
                floor.setType(Material.STONE);
                // ceiling
                Block ceil = world.getBlockAt(baseX + x, baseY + HEIGHT + 1, baseZ + z);
                ceil.setType(Material.STONE);
            }
        }
        // Walls vertical
        for (int y = 0; y < HEIGHT + 2; y++) {
            for (int x = -1; x <= SIZE; x++) {
                Block w1 = world.getBlockAt(baseX + x, baseY + y, baseZ - 1);
                Block w2 = world.getBlockAt(baseX + x, baseY + y, baseZ + SIZE);
                w1.setType(Material.STONE);
                w2.setType(Material.STONE);
            }
            for (int z = -1; z <= SIZE; z++) {
                Block w1 = world.getBlockAt(baseX - 1, baseY + y, baseZ + z);
                Block w2 = world.getBlockAt(baseX + SIZE, baseY + y, baseZ + z);
                w1.setType(Material.STONE);
                w2.setType(Material.STONE);
            }
        }
    }

    // Generate a build from a BigInteger S that encodes full BlockData strings for each cell.
    // origin: corner location where x/z loop starts
    public static void generateFromBigInteger(BigInteger s, World world, Location origin) {
        int baseX = origin.getBlockX();
        int baseY = origin.getBlockY();
        int baseZ = origin.getBlockZ();

        // Convert BigInteger to magnitude bytes
        byte[] all = toMagnitudeBytes(s);

        int offset = 0;
        for (int y = 0; y < HEIGHT; y++) {
            for (int z = 0; z < SIZE; z++) {
                for (int x = 0; x < SIZE; x++) {
                    // Read 2-byte length
                    String dataStr = null;
                    if (offset + 2 <= all.length) {
                        int len = ((all[offset] & 0xFF) << 8) | (all[offset + 1] & 0xFF);
                        offset += 2;
                        if (len >= 0 && offset + len <= all.length) {
                            try {
                                dataStr = new String(all, offset, len, java.nio.charset.StandardCharsets.UTF_8);
                            } catch (Exception ex) {
                                dataStr = null;
                            }
                            offset += len;
                        } else {
                            // not enough bytes, fallback to air
                            dataStr = null;
                            offset = all.length; // force remainder default
                        }
                    }

                    Block b = world.getBlockAt(baseX + x, baseY + y, baseZ + z);
                    if (dataStr == null || dataStr.isEmpty()) {
                        b.setType(Material.AIR);
                    } else {
                        try {
                            BlockData bd = Bukkit.createBlockData(dataStr);
                            b.setBlockData(bd, false);
                        } catch (Exception ex) {
                            // If parsing fails, try material only
                            try {
                                Material m = Material.matchMaterial(dataStr);
                                if (m != null) b.setType(m);
                                else b.setType(Material.AIR);
                            } catch (Exception ex2) {
                                b.setType(Material.AIR);
                            }
                        }
                    }
                }
            }
        }
    }

    private static byte[] toMagnitudeBytes(BigInteger s) {
        if (s == null) return new byte[0];
        byte[] t = s.toByteArray();
        if (t.length > 0 && t[0] == 0) {
            // strip sign byte
            byte[] mag = new byte[t.length - 1];
            System.arraycopy(t, 1, mag, 0, mag.length);
            return mag;
        }
        return t;
    }
}
