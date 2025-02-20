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

package com.gliesestudio.mc;

import com.gliesestudio.mc.completer.BetterTabCompleter;
import com.gliesestudio.mc.model.TPARequest;
import com.gliesestudio.mc.repository.TeleportRepository;
import com.gliesestudio.mc.schedule.DelayedTeleport;
import com.gliesestudio.mc.service.warp.WarpStorage;
import com.gliesestudio.mc.utility.ApplicationUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This class is the main class for the Better TPA plugin.
 *
 * @author Mazidul Islam
 * @version 1.0
 * @since 1.0
 */
public final class BetterTPA extends JavaPlugin implements Listener {

    private final Logger logger = getLogger();

    private WarpStorage warpStorage;

    @Override
    public void onEnable() {
        displayStartingMessage();
        // Create a new instance of WarpStorage to manage warp locations
        warpStorage = new WarpStorage(getDataFolder(), logger);
        getServer().getPluginManager().registerEvents(this, this); // Register event listener
    }

    @Override
    public void onDisable() {
        logger.info("\u001B[33mBetterTPA plugin disabled\u001B[0m");
    }

    private void displayStartingMessage() {
        logger.info("\u001B[32m╔═══════════════════════════════╗\u001B[0m");
        logger.info("\u001B[32m║          Better TPA           ║\u001B[0m");
        logger.info("\u001B[32m╚═══════════════════════════════╝\u001B[0m");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            switch (command.getName().toLowerCase()) {

                case "tpa" -> {
                    if (args.length == 0) {
                        sender.sendMessage("§cInvalid command! Usage: /tpa <player>");
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target == null || !target.isOnline()) {
                        sender.sendMessage("§cPlayer not found or offline.");
                        return true;
                    }
                    handleTPARequest(player, target);
                }

                case "tphere" -> {
                    if (args.length == 0) {
                        sender.sendMessage("§cInvalid command! Usage: /tphere <player>");
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target == null || !target.isOnline()) {
                        sender.sendMessage("§cPlayer not found or offline.");
                        return true;
                    }
                    handleTPHereRequest(player, target);
                }

                case "tpaccept" -> handleTPAccept(player);

                case "tpdeny" -> handleTPDeny(player);

                // Try to teleport the player back to their last death or teleported location.
                case "back" -> handleBackCommand(player);

                case "setwarp" -> {
                    if (args.length != 1) {
                        player.sendMessage("§cInvalid command! Usage: /setwarp <name>");
                        return true;
                    }
                    handleSetWarpCommand(player, args[0]);
                }

                case "warp" -> {
                    if (args.length != 1) {
                        player.sendMessage("§cInvalid command! Usage: /warp <name>");
                        return true;
                    }
                    handleWarpCommand(player, args[0]);
                }

                case "warps" -> handleWarpsCommand(player);

                case "delwarp" -> {
                    if (args.length < 2) {
                        player.sendMessage("§cInvalid command! Usage: /delwarp <name> confirm");
                        return true;
                    }
                    handleDelWarpCommand(player, args[0]);
                }

                default -> {
                    return false;
                }
            }
        } else {
            sender.sendMessage("§cOnly players can use this command.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new BetterTabCompleter(warpStorage).onTabComplete(sender, command, alias, args);
    }

    private void handleTPARequest(@NotNull Player requester, @NotNull Player target) {
        TeleportRepository.addTpaRequest(target.getUniqueId(), new TPARequest(
                requester.getUniqueId(),
                requester.getUniqueId(),
                target.getUniqueId()
        ));
        requester.sendMessage(String.format("§aTeleport request sent to %s.", target.getName()));
        // Send the message to the target player
        TextComponent tpaMessage = createTPAMessage(String.format("§6%s has requested to teleport to you.", requester.getName()));
        target.sendMessage(tpaMessage);

        // Play teleport request received sound
        target.playSound(
                target,
                "minecraft:entity.endermite.death",
                1.0f,
                1.0f
        );

        // Set up a timeout for the request
        handleRequestTimeout(requester, target);
    }

    private void handleTPHereRequest(@NotNull Player requester, @NotNull Player target) {
        TeleportRepository.addTpaRequest(target.getUniqueId(), new TPARequest(
                requester.getUniqueId(),
                target.getUniqueId(),
                requester.getUniqueId()
        ));
        requester.sendMessage(String.format("§aTeleport request sent to %s.", target.getName()));
        TextComponent tpaMessage = createTPAMessage(String.format("§6%s has requested you to teleport to them.", requester.getName()));
        target.sendMessage(tpaMessage);

        // Play teleport request received sound
        target.playSound(
                target,
                "minecraft:entity.endermite.death",
                1.0f,
                1.0f
        );

        // Set up a timeout for the request
        handleRequestTimeout(requester, target);
    }

    private void handleTPAccept(@NotNull Player sender) {
        TPARequest pendingRequest = TeleportRepository.getTpaRequest(sender.getUniqueId());
        if (pendingRequest != null) {
            Player requester = Bukkit.getPlayer(pendingRequest.requester());
            Player tpaPlayer = Bukkit.getPlayer(pendingRequest.player());
            Player tpaToPlayer = Bukkit.getPlayer(pendingRequest.teleportToPlayer());
            if (tpaPlayer != null && tpaPlayer.isOnline() && tpaToPlayer != null && tpaToPlayer.isOnline()) {
                // Start the delayed teleport
                assert requester != null;
                requester.sendMessage(String.format("§a%s has accepted your teleport request.", sender.getName()));
                sender.sendMessage(String.format("§aYou have accepted the teleport request from %s.", requester.getName()));
                new DelayedTeleport() {
                }.playerTeleport(tpaPlayer, tpaToPlayer).start(this);
            } else {
                sender.sendMessage("§eRequester is no longer online.");
            }
            TeleportRepository.removeTpaRequest(sender.getUniqueId());
        } else {
            sender.sendMessage("§cYou have no pending teleport requests.");
        }
    }

    private void handleTPDeny(@NotNull Player sender) {
        TPARequest pendingRequest = TeleportRepository.getTpaRequest(sender.getUniqueId());
        if (pendingRequest != null) {
            Player requester = Bukkit.getPlayer(pendingRequest.requester());
            TeleportRepository.removeTpaRequest(sender.getUniqueId());
            if (requester != null) {
                sender.sendMessage(String.format("§eYou have denied the teleport request from %s.", requester.getName()));
                if (requester.isOnline()) {
                    requester.sendMessage(String.format("§c%s has denied your teleport request.", sender.getName()));
                }
            } else {
                sender.sendMessage("§eRequester is no longer online.");
            }
        } else {
            sender.sendMessage("§cYou have no pending teleport requests.");
        }
    }

    private void handleBackCommand(@NotNull Player player) {
        Location lastLocation = TeleportRepository.getLastLocation(player.getUniqueId());
        if (lastLocation != null) {
            new DelayedTeleport() {
            }.backTeleport(player, lastLocation).start(this);
        } else {
            player.sendMessage("§eNo previous location found to teleport back to.");
        }
    }

    private void handleSetWarpCommand(Player player, String warpName) {
        Location playerLocation = player.getLocation();
        warpStorage.setWarp(warpName, playerLocation);
        player.sendMessage("§aWarp §r" + warpName + "§a has been set.");
    }

    private void handleWarpCommand(Player player, String warpName) {
        if (warpStorage.warpExists(warpName)) {
            Location warpLocation = warpStorage.getWarp(warpName);
            new DelayedTeleport() {
            }.warpTeleport(player, warpLocation).start(this);
        } else {
            player.sendMessage("§cWarp '" + warpName + "' does not exist.");
        }
    }

    private void handleWarpsCommand(Player player) {
        Set<String> warpNames = warpStorage.getWarpNames();
        if (warpNames.isEmpty()) {
            player.sendMessage("§eNo warps have been set yet.");
        } else {
            player.sendMessage("§aAvailable warps: §r" + String.join(", ", warpNames));
        }
    }

    private void handleDelWarpCommand(Player player, String warpName) {
        if (warpStorage.warpExists(warpName)) {
            warpStorage.deleteWarp(warpName); // Assuming you add a deleteWarp method to WarpStorage
            player.sendMessage("§aWarp '" + warpName + "' has been deleted.");
        } else {
            player.sendMessage("§cWarp '" + warpName + "' does not exist.");
        }
    }

    private void handleRequestTimeout(Player requester, Player target) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (TeleportRepository.containsTpaRequest(target.getUniqueId())) {
                    TeleportRepository.removeTpaRequest(target.getUniqueId());
                    requester.sendMessage(String.format("§cYour teleport request to %s has timed out.", target.getName()));
                    target.sendMessage(String.format("§eThe teleport request from %s has timed out.", requester.getName()));
                }
            }
        }.runTaskLater(this, TeleportRepository.REQUEST_TIMEOUT * 20);
    }

    // Send clickable TPA accept/deny messages to the target player
    private @NotNull TextComponent createTPAMessage(String text) {
        // Create the "TPA Accept" button
        Component acceptButton = Component.text("[Accept]")
                .decorate(TextDecoration.BOLD)
                .color(net.kyori.adventure.text.format.NamedTextColor.GREEN)
                .hoverEvent(HoverEvent.showText(Component.text("Click to accept the teleport request")))
                .clickEvent(ClickEvent.runCommand("/tpaccept"));

        // Create the "TPA Deny" button
        Component denyButton = Component.text("[Deny]")
                .decorate(TextDecoration.BOLD)
                .color(net.kyori.adventure.text.format.NamedTextColor.RED)
                .hoverEvent(HoverEvent.showText(Component.text("Click to deny the teleport request")))
                .clickEvent(ClickEvent.runCommand("/tpdeny"));

        // Combine both buttons with a message
        return Component.text(text)
                .append(Component.text("§6 You can "))
                .append(acceptButton)
                .append(Component.text("§6 or "))
                .append(denyButton)
                .append(Component.text("§6 the request."));
    }

    @EventHandler
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        // Check if the player has actually moved
        if (ApplicationUtils.hasPlayerMoved(event.getFrom(), event.getTo())) {
            Player player = event.getPlayer();
            // Check if the player is in the pending teleports list
            if (TeleportRepository.hasPendingTeleport(player.getUniqueId())) {
                // Cancel the teleport
                TeleportRepository.cancelPendingTeleport(player.getUniqueId());
                player.sendMessage("§cTeleport cancelled because you moved.");
            }
        }
    }

    // Handle player death events to store last death location
    @EventHandler
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        Player player = event.getEntity();
        TeleportRepository.setLastLocation(player.getUniqueId(), player.getLocation());
    }

}
