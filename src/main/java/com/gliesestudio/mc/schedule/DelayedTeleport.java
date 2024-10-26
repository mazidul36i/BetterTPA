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

public abstract class DelayedTeleport {

    private TeleportType teleportType = null;
    private long delay = 3L;
    private Player player = null;
    private Player toPlayer = null;
    private Location location = null;

    final public DelayedTeleport playerTeleport(Player player, Player toPlayer) {
        this.teleportType = TeleportType.TO_PLAYER;
        this.player = player;
        this.toPlayer = toPlayer;
        return this;
    }

    final public DelayedTeleport warpTeleport(Player player, Location warpLocation) {
        this.teleportType = TeleportType.WARP;
        this.player = player;
        this.location = warpLocation;
        return this;
    }

    final public DelayedTeleport backTeleport(Player player, Location lastLocation) {
        this.teleportType = TeleportType.BACK;
        this.player = player;
        this.location = lastLocation;
        return this;
    }

    final public void start(@NotNull Plugin plugin, long delay) {
        this.delay = delay;
        start(plugin);
    }

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
                pendingTeleports.remove(player.getUniqueId());
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

                // Store the TPA player's previous location
                lastLocations.put(player.getUniqueId(), player.getLocation());

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

    private void showTeleportingTitle() {
        Title.Times times = Title.Times.times(Ticks.duration(10), Ticks.duration(delay * 20), Ticks.duration(10));
        player.showTitle(Title.title(
                Component.text("§aTeleporting..."),
                Component.text("§eDon't move!"),
                times
        ));
    }

}
