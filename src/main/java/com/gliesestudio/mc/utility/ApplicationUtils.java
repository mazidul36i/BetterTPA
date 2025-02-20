package com.gliesestudio.mc.utility;

import org.bukkit.Location;

/**
 * This class contains utility methods for the Better TPA plugin.
 *
 * @author Mazidul Islam
 * @version 1.0
 * @since 1.0
 */
public final class ApplicationUtils {

    private ApplicationUtils() {
    }

    /**
     * Check if the player has moved from one location to another.
     *
     * @param from The player's previous location.
     * @param to   The player's current location.
     * @return True if the player has moved, false otherwise.
     */
    public static boolean hasPlayerMoved(Location from, Location to) {
        return from.getBlockX() != to.getBlockX() ||
                from.getBlockY() != to.getBlockY() ||
                from.getBlockZ() != to.getBlockZ();
    }

}
