package org.cubeville.cvelvenworkshop.utils;

import org.bukkit.Chunk;
import org.bukkit.Location;

public class ChunkUtils {
    public static void loadChunks(Location min, Location max) {
        for (double x = min.getX(); x <= max.getX(); x += 16) {
            for (double z = min.getZ(); z <= max.getZ(); z += 16) {
                Chunk chunk = min.getWorld().getChunkAt(new Location(min.getWorld(), x, max.getY(), z));
                min.getWorld().setChunkForceLoaded(chunk.getX(), chunk.getZ(), true);
            }
        }
    }

    public static void unloadChunks(Location min, Location max) {
        for (double x = min.getX(); x <= max.getX(); x += 16) {
            for (double z = min.getZ(); z <= max.getZ(); z += 16) {
                Chunk chunk = min.getWorld().getChunkAt(new Location(min.getWorld(), x, max.getY(), z));
                min.getWorld().setChunkForceLoaded(chunk.getX(), chunk.getZ(), false);
            }
        }
    }
}
