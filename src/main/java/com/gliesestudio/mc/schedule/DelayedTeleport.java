/*
 * MIT License
 *
 * Copyright (c) 2024 Mazidul Islam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.gliesestudio.mc.schedule;

import com.gliesestudio.mc.enums.TeleportType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import static com.gliesestudio.mc.BetterTPA.lastLocations;
import static com.gliesestudio.mc.BetterTPA.pendingTeleports;

/**
 * This class provides a delayed teleport functionality for the Better TPA plugin.
 *
 * @author Mazidul Islam
 * @version 1.0
 * @implNote This class is designed to be extended by other classes to customize the behavior of {@link #beforeTeleport}
 * and {@link #afterTeleport}.
 * @see TeleportType
 * @see BukkitRunnable
 * @since 1.0
 */
public abstract class DelayedTeleport {

    private TeleportType teleportType = null;
    private long delay = 3L;
    private Player player = null;
    private Player toPlayer = null;
    private Location location = null;

    /**
     * This method sets the teleport type to {@link TeleportType#TO_PLAYER} and sets the player and the target player.
     *
     * @param player   The player who is being teleported.
     * @param toPlayer The player who is being teleported to.
     * @return This {@link DelayedTeleport} instance.
     */
    final public DelayedTeleport playerTeleport(Player player, Player toPlayer) {
        this.teleportType = TeleportType.TO_PLAYER;
        this.player = player;
        this.toPlayer = toPlayer;
        return this;
    }

    /**
     * This method sets the teleport type to {@link TeleportType#WARP} and sets the player and the warp location.
     *
     * @param player       The player who is being teleported.
     * @param warpLocation The warp location.
     * @return This {@link DelayedTeleport} instance.
     */
    final public DelayedTeleport warpTeleport(Player player, Location warpLocation) {
        this.teleportType = TeleportType.WARP;
        this.player = player;
        this.location = warpLocation;
        return this;
    }

    /**
     * This method sets the teleport type to {@link TeleportType#BACK} and sets the player and the last location.
     *
     * @param player       The player who is being teleported.
     * @param lastLocation The last location.
     * @return This {@link DelayedTeleport} instance.
     */
    final public DelayedTeleport backTeleport(Player player, Location lastLocation) {
        this.teleportType = TeleportType.BACK;
        this.player = player;
        this.location = lastLocation;
        return this;
    }

    /**
     * This method starts the delayed teleport with a custom delay.
     *
     * @param plugin The plugin instance.
     * @param delay  The delay in seconds.
     */
    final public void start(@NotNull Plugin plugin, long delay) {
        this.delay = delay;
        start(plugin);
    }

    /**
     * This method starts the delayed teleport.
     *
     * @param plugin The plugin instance.
     */
    final public void start(@NotNull Plugin plugin) {
        // Cancel any previous pending teleport for this player
        if (pendingTeleports.containsKey(player.getUniqueId())) {
            pendingTeleports.get(player.getUniqueId()).cancel();
        }

        // Show teleporting message.
        showTeleportingTitle();

        // Create a new BukkitRunnable for the delayed teleport
        BukkitRunnable teleportTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Removed pending teleport and store the player's current location.
                pendingTeleports.remove(player.getUniqueId());
                lastLocations.put(player.getUniqueId(), player.getLocation());

                // Call the beforeTeleport method.
                beforeTeleport();

                if (teleportType == TeleportType.TO_PLAYER)
                    player.teleport(toPlayer);
                else if (teleportType == TeleportType.WARP || teleportType == TeleportType.BACK)
                    player.teleport(location);
                else {
                    player.sendMessage("§4Teleport type is null!");
                    return;
                }

                // Call the afterTeleport method.
                afterTeleport(teleportType);
            }
        };

        // Store the teleport task in the map
        pendingTeleports.put(player.getUniqueId(), teleportTask);
        // Start the 3-second delay
        teleportTask.runTaskLater(plugin, delay * 20);
    }

    /**
     * This is a callback method, and it can be implemented to do something before teleportation happens.
     */
    public void beforeTeleport() {
        // Play teleport sound
        player.playSound(
                player,
                "minecraft:entity.creeper.primed",
                1.2f,
                1.0f
        );
    }

    /**
     * This is a callback method, and it can be implemented to do something after teleportation happens.
     *
     * @param teleportType the type of teleport that happened (TO_PLAYER, WARP, BACK).
     */
    public void afterTeleport(TeleportType teleportType) {
        if (teleportType == TeleportType.TO_PLAYER) {
            player.sendMessage(String.format("§aYou have been teleported to %s.", toPlayer.getName()));
        } else if (teleportType == TeleportType.WARP) {
            player.teleport(location);
            player.sendMessage("§aYou have been teleported to the warp location.");
        } else if (teleportType == TeleportType.BACK) {
            player.teleport(location);
            player.sendMessage("§aYou have been teleported back to your last location.");
        } else {
            player.sendMessage("§cNo valid teleport target found.");
        }
    }

    /**
     * This method shows the teleporting title to the player.
     */
    private void showTeleportingTitle() {
        Title.Times times = Title.Times.times(Ticks.duration(10), Ticks.duration(delay * 20), Ticks.duration(10));
        player.showTitle(Title.title(
                Component.text("§aTeleporting..."),
                Component.text("§eDon't move!"),
                times
        ));
    }

}
