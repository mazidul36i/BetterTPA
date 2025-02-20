package com.gliesestudio.mc.repository;

import com.gliesestudio.mc.model.TPARequest;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class is a repository for the Player Teleport and contains the TPA requests,
 * pending teleports, and last locations.
 *
 * @author Mazidul Islam
 * @version 1.0
 * @since 1.0
 */
public class TeleportRepository {

    private TeleportRepository() {
        throw new IllegalStateException("All variables and methods are static and directly accessible.");
    }

    public static final long REQUEST_TIMEOUT = 30L; // 30 seconds for timeout

    private static final Map<UUID, TPARequest> tpaRequests = new HashMap<>();
    private static final Map<UUID, BukkitRunnable> pendingTeleports = new HashMap<>();
    private static final Map<UUID, Location> lastLocations = new HashMap<>();

    /*============================
    |      Teleport Requests      |
     ============================*/

    public static void addTpaRequest(UUID playerUuid, TPARequest tpaRequest) {
        tpaRequests.put(playerUuid, tpaRequest);
    }

    @Nullable
    public static TPARequest getTpaRequest(UUID playerUuid) {
        return tpaRequests.getOrDefault(playerUuid, null);
    }

    public static boolean containsTpaRequest(UUID playerUuid) {
        return tpaRequests.containsKey(playerUuid);
    }

    public static void removeTpaRequest(UUID playerUuid) {
        tpaRequests.remove(playerUuid);
    }

    /*============================
    |      Pending Teleports      |
     ============================*/

    /**
     * Add teleporting task to the pending teleports.
     *
     * @param playerUuid   The player's UUID.
     * @param teleportTask The teleport task.
     */
    public static void addPendingTeleport(UUID playerUuid, BukkitRunnable teleportTask) {
        pendingTeleports.put(playerUuid, teleportTask);
    }

    public static boolean hasPendingTeleport(UUID playerUuid) {
        return pendingTeleports.containsKey(playerUuid);
    }

    public static void removePendingTeleport(UUID playerUuid) {
        pendingTeleports.remove(playerUuid);
    }

    public static void cancelPendingTeleport(UUID playerUuid) {
        BukkitRunnable bukkitRunnable = pendingTeleports.get(playerUuid);
        if (bukkitRunnable != null && !bukkitRunnable.isCancelled()) {
            bukkitRunnable.cancel();
        }
        pendingTeleports.remove(playerUuid);
    }

    /*============================
    |       Last Locations        |
     ============================*/

    public static void setLastLocation(UUID playerUuid, Location lastLocation) {
        lastLocations.put(playerUuid, lastLocation);
    }

    @Nullable
    public static Location getLastLocation(UUID playerUuid) {
        return lastLocations.getOrDefault(playerUuid, null);
    }

}
