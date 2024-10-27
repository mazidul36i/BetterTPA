package com.gliesestudio.mc.utility;

import org.bukkit.Location;

/**
 * This class contains utility methods for the Better TPA plugin.
 *
 * @author Mazidul Islam
 * @version 1.0
 * @since 1.0
 */
public class ApplicationUtils {

    /**
     * Check if the player has moved from one location to another.
     *
     * @param from The player's previous location.
     * @param to   The player's current location.
     * @return True if the player has moved, false otherwise.
     */
    public static boolean hasPlayerMoved(Location from, Location to) {
        return round(from.getX()) != round(to.getX()) ||
                round(from.getY()) != round(to.getY()) ||
                round(from.getZ()) != round(to.getZ());
    }

    /**
     * Round a double to 2 decimal places.
     *
     * @param value The double to round.
     * @return The rounded double.
     */
    public static double round(double value) {
        return round(value, 2);
    }

    /**
     * Round a double to a specified number of decimal places.
     *
     * @param value  The double to round.
     * @param places The number of decimal places to round to.
     * @return The rounded double.
     */
    public static double round(double value, int places) {
        if (places <= 0) throw new IllegalArgumentException("Round figure at least upto 1 decimal points.");
        int multiplier = 10;
        for (int i = 0; i < places; i++) multiplier *= 10;
        return (double) ((int) (value * multiplier)) / multiplier;
    }

}
