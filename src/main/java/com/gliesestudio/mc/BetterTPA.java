package com.gliesestudio.mc;

import com.gliesestudio.mc.model.TPARequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
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

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public final class BetterTPA extends JavaPlugin implements Listener {

    private static final Map<UUID, TPARequest> tpaRequests = new HashMap<>();
    private static final Map<UUID, BukkitRunnable> pendingTeleports = new HashMap<>();
    private static final Map<UUID, Location> lastLocations = new HashMap<>();
    private static final long requestTimeout = 30L; // 30 seconds for timeout
    private static final long teleportDelay = 3L; // 3 seconds

    private final Logger logger = getLogger();

    @Override
    public void onEnable() {
        logger.info("BetterTPA plugin enabled");
        getServer().getPluginManager().registerEvents(this, this); // Register event listener
    }

    @Override
    public void onDisable() {
        logger.info("BetterTPA plugin disabled");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            switch (command.getName().toLowerCase()) {

                case "tpa" -> {
                    if (args.length == 0) {
                        sender.sendMessage("Usage: /tpa <player>");
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target == null || !target.isOnline()) {
                        sender.sendMessage("Player not found or offline.");
                        return true;
                    }
                    handleTPARequest(player, target);
                }

                case "tphere" -> {
                    if (args.length == 0) {
                        sender.sendMessage("Usage: /tphere <player>");
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target == null || !target.isOnline()) {
                        sender.sendMessage("Player not found or offline.");
                        return true;
                    }
                    handleTPHereRequest(player, target);
                }

                case "tpaccept" -> handleTPAccept(player);

                case "tpdeny" -> handleTPDeny(player);

                // Try to teleport the player back to their last death or teleported location.
                case "back" -> handleBackCommand(player);
            }
        } else {
            sender.sendMessage("Only players can use this command.");
        }
        return true;
    }

    private void handleTPARequest(@NotNull Player requester, @NotNull Player target) {
        tpaRequests.put(target.getUniqueId(), new TPARequest(
                requester.getUniqueId(),
                requester.getUniqueId(),
                target.getUniqueId()
        ));
        requester.sendMessage(String.format("Teleport request sent to %s.", target.getName()));
        // Send the message to the target player
        TextComponent tpaMessage = createTPAMessage(String.format("%s has requested to teleport to you.", requester.getName()));
        target.sendMessage(tpaMessage);

        // Set up a timeout for the request
        handleRequestTimeout(requester, target);
    }

    private void handleTPHereRequest(@NotNull Player requester, @NotNull Player target) {
        tpaRequests.put(target.getUniqueId(), new TPARequest(
                requester.getUniqueId(),
                target.getUniqueId(),
                requester.getUniqueId()
        ));
        requester.sendMessage(String.format("Teleport request sent to %s.", target.getName()));
        TextComponent tpaMessage = createTPAMessage(String.format("%s has requested you to teleport to them.", requester.getName()));
        target.sendMessage(tpaMessage);

        // Set up a timeout for the request
        handleRequestTimeout(requester, target);
    }

    private void handleTPAccept(@NotNull Player sender) {
        TPARequest pendingRequest = tpaRequests.get(sender.getUniqueId());
        if (pendingRequest != null) {
            Player tpaPlayer = Bukkit.getPlayer(pendingRequest.getTpaPlayer());
            Player tpaToPlayer = Bukkit.getPlayer(pendingRequest.getTpaToPlayer());
            if (tpaPlayer != null && tpaPlayer.isOnline() && tpaToPlayer != null && tpaToPlayer.isOnline()) {
                // Start the delayed teleport with movement check
                startDelayedTeleport(tpaPlayer, tpaToPlayer, null);
            } else {
                sender.sendMessage("Requester is no longer online.");
            }
            tpaRequests.remove(sender.getUniqueId());
        } else {
            sender.sendMessage("You have no pending TPA requests.");
        }
    }

    private void handleTPDeny(@NotNull Player sender) {
        TPARequest pendingRequest = tpaRequests.get(sender.getUniqueId());
        if (pendingRequest != null) {
            Player requester = Bukkit.getPlayer(pendingRequest.getRequester());
            if (requester != null && requester.isOnline()) {
                requester.sendMessage(String.format("%s has denied your TPA request.", sender.getName()));
                sender.sendMessage(String.format("You have denied the TPA request from %s.", requester.getName()));
                tpaRequests.remove(sender.getUniqueId());
            } else {
                sender.sendMessage("Requester is no longer online.");
                tpaRequests.remove(sender.getUniqueId());
            }
        } else {
            sender.sendMessage("You have no pending TPA requests.");
        }
    }

    private void handleBackCommand(@NotNull Player player) {
        UUID playerId = player.getUniqueId();

        if (lastLocations.containsKey(playerId)) {
            Location lastLocation = lastLocations.get(playerId);
            startDelayedTeleport(player, null, lastLocation);
        } else {
            player.sendMessage("No previous location found to teleport back to.");
        }
    }

    private void handleRequestTimeout(Player requester, Player target) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (tpaRequests.containsKey(target.getUniqueId())) {
                    tpaRequests.remove(target.getUniqueId());
                    requester.sendMessage(String.format("Your TPA request to %s has timed out.", target.getName()));
                    target.sendMessage(String.format("The TPA request from %s has timed out.", requester.getName()));
                }
            }
        }.runTaskLater(this, requestTimeout * 20);
    }

    private void startDelayedTeleport(@NotNull Player tpaPlayer, @Nullable Player tpaToPlayer, @Nullable Location location) {
        // Cancel any previous pending teleport for this player
        if (pendingTeleports.containsKey(tpaPlayer.getUniqueId())) {
            pendingTeleports.get(tpaPlayer.getUniqueId()).cancel();
        }

        // Show teleporting message.
        Title.Times times = Title.Times.times(Ticks.duration(10), Ticks.duration(3 * 20), Ticks.duration(10));
        tpaPlayer.showTitle(Title.title(
                Component.text("Teleporting..."),
                Component.text("Don't move!"),
                times
        ));

        // Create a new BukkitRunnable for the delayed teleport
        BukkitRunnable teleportTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Play teleport sound
                tpaPlayer.playSound(
                        tpaPlayer,
                        "minecraft:entity.creeper.primed",
                        1.2f,
                        1.0f
                );

                // Store the TPA player's previous location
                lastLocations.put(tpaPlayer.getUniqueId(), tpaPlayer.getLocation());

                if (Objects.nonNull(tpaToPlayer)) {
                    tpaPlayer.teleport(tpaToPlayer);
                    tpaToPlayer.sendMessage(String.format("%s has been teleported to you.", tpaPlayer.getName()));
                    tpaPlayer.sendMessage(String.format("You have been teleported to %s.", tpaToPlayer.getName()));
                } else if (Objects.nonNull(location)) {
                    tpaPlayer.teleport(location);
                    tpaPlayer.sendMessage("You have been teleported back to your last location.");
                } else {
                    tpaPlayer.sendMessage("No valid teleport target found.");
                }
                pendingTeleports.remove(tpaPlayer.getUniqueId());
            }
        };

        // Store the teleport task in the map
        pendingTeleports.put(tpaPlayer.getUniqueId(), teleportTask);
        // Start the 3-second delay
        teleportTask.runTaskLater(this, teleportDelay * 20);
    }

    // Send clickable TPA accept/deny messages to the target player
    private @NotNull TextComponent createTPAMessage(String text) {
        // Create the "TPA Accept" button
        Component acceptButton = Component.text("[Accept]")
                .color(net.kyori.adventure.text.format.NamedTextColor.GREEN)
                .hoverEvent(HoverEvent.showText(Component.text("Click to accept the teleport request")))
                .clickEvent(ClickEvent.runCommand("/tpaccept"));

        // Create the "TPA Deny" button
        Component denyButton = Component.text("[Deny]")
                .color(net.kyori.adventure.text.format.NamedTextColor.RED)
                .hoverEvent(HoverEvent.showText(Component.text("Click to deny the teleport request")))
                .clickEvent(ClickEvent.runCommand("/tpdeny"));

        // Combine both buttons with a message
        return Component.text(text)
                .append(Component.text(" You can "))
                .append(acceptButton)
                .append(Component.text(" or "))
                .append(denyButton)
                .append(Component.text(" the request."));
    }

    @EventHandler
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        Player player = event.getPlayer();
        // Check if the player is in the pending teleports list
        if (pendingTeleports.containsKey(player.getUniqueId())) {
            // Check if the player has actually moved
            if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ()) {
                // Cancel the teleport
                pendingTeleports.get(player.getUniqueId()).cancel();
                pendingTeleports.remove(player.getUniqueId());
                player.sendMessage("Teleport cancelled because you moved.");
            }
        }
    }

    // Handle player death events to store last death location
    @EventHandler
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        Player player = event.getEntity();
        lastLocations.put(player.getUniqueId(), player.getLocation());
    }

}
