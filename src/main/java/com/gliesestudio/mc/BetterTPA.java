package com.gliesestudio.mc;

import com.gliesestudio.mc.model.TPARequest;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public final class BetterTPA extends JavaPlugin {

    private static final Map<UUID, TPARequest> tpaRequests = new HashMap<>();
    private static final long requestTimeout = 30L; // 30 seconds for timeout

    private final Logger logger = getLogger();

    @Override
    public void onEnable() {
        logger.info("BetterTPA plugin enabled");
    }

    @Override
    public void onDisable() {
        logger.info("BetterTPA plugin disabled");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player requester) {
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
                    handleTPARequest(requester, target);
                }

                case "tpahere" -> {
                    if (args.length == 0) {
                        sender.sendMessage("Usage: /tpahere <player>");
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target == null || !target.isOnline()) {
                        sender.sendMessage("Player not found or offline.");
                        return true;
                    }
                    handleTPAHereRequest(requester, target);
                }

                case "tpaccept" -> {
                    handleTPAccept(requester);
                }

                case "tpdeny" -> {
                    handleTPDeny(requester);
                }
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
        requester.sendMessage(String.format("TPA request sent to %s.", target.getName()));
        target.sendMessage(String.format("%s has requested to teleport to you. Type /tpaccept or /tpdeny.", requester.getName()));

        // Set up a timeout for the request
        handleRequestTimeout(requester, target);
    }

    private void handleTPAHereRequest(@NotNull Player requester, @NotNull Player target) {
        tpaRequests.put(target.getUniqueId(), new TPARequest(
                requester.getUniqueId(),
                target.getUniqueId(),
                requester.getUniqueId()
        ));
        requester.sendMessage(String.format("TPA request sent to %s.", target.getName()));
        target.sendMessage(String.format("%s has requested you to teleport to them. Type /tpaccept or /tpdeny.", requester.getName()));

        // Set up a timeout for the request
        handleRequestTimeout(requester, target);
    }

    private void handleTPAccept(@NotNull Player sender) {
        TPARequest pendingRequest = tpaRequests.get(sender.getUniqueId());
        if (pendingRequest != null) {
            Player tpaPlayer = Bukkit.getPlayer(pendingRequest.getTpaPlayer());
            Player tpaToPlayer = Bukkit.getPlayer(pendingRequest.getTpaToPlayer());
            if (tpaPlayer != null && tpaPlayer.isOnline() && tpaToPlayer != null && tpaToPlayer.isOnline()) {
                tpaPlayer.teleport(tpaToPlayer);
                tpaToPlayer.sendMessage(String.format("%s has been teleported to you.", tpaPlayer.getName()));
                tpaPlayer.sendMessage(String.format("You have been teleported to %s.", tpaToPlayer.getName()));
                tpaRequests.remove(sender.getUniqueId());
            } else {
                sender.sendMessage("Requester is no longer online.");
                tpaRequests.remove(sender.getUniqueId());
            }
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
}
