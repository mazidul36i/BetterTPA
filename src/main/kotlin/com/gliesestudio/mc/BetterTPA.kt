package com.gliesestudio.mc

import com.gliesestudio.mc.model.TPARequest
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class BetterTPA : JavaPlugin() {

    private val tpaRequests = HashMap<UUID, TPARequest>() // Map of <Requester, Target>
    private val requestTimeout = 30L // 30 seconds for timeout

    override fun onEnable() {
        // Plugin startup logic
        logger.info("BetterTPA plugin enabled")
    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info("BetterTPA plugin disabled")
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            when (command.name.lowercase()) {
                "tpa" -> {
                    if (args.isEmpty()) {
                        sender.sendMessage("Usage: /tpa <player>")
                        return true
                    }
                    val target = Bukkit.getPlayer(args[0])
                    if (target == null || !target.isOnline) {
                        sender.sendMessage("Player not found or offline.")
                        return true
                    }
                    handleTPARequest(sender, target)
                }

                "tpahere" -> {
                    if (args.isEmpty()) {
                        sender.sendMessage("Usage: /tpahere <player>")
                        return true
                    }
                    val target = Bukkit.getPlayer(args[0])
                    if (target == null || !target.isOnline) {
                        sender.sendMessage("Player not found or offline.")
                        return true
                    }
                    handleTPAHereRequest(sender, target)
                }

                "tpaccept" -> {
                    handleTPAccept(sender)
                }

                "tpdeny" -> {
                    handleTPDeny(sender)
                }
            }
        } else {
            sender.sendMessage("Only players can use this command.")
        }
        return true
    }

    private fun handleTPARequest(requester: Player, target: Player) {
        tpaRequests[target.uniqueId] = TPARequest(
            requester = requester.uniqueId,
            tpaPlayer = requester.uniqueId,
            tpaToPlayer = target.uniqueId
        )
        requester.sendMessage("TPA request sent to ${target.name}.")
        target.sendMessage("${requester.name} has requested to teleport to you. Type /tpaccept or /tpdeny.")

        // Set up a timeout for the request
        handleRequestTimeout(requester, target)
    }

    private fun handleTPAHereRequest(requester: Player, target: Player) {
        tpaRequests[target.uniqueId] = TPARequest(
            requester = requester.uniqueId,
            tpaPlayer = target.uniqueId,
            tpaToPlayer = requester.uniqueId
        )
        requester.sendMessage("TPA request sent to ${target.name}.")
        target.sendMessage("${requester.name} has requested you to teleport to them. Type /tpaccept or /tpdeny.")

        // Set up a timeout for the request
        handleRequestTimeout(requester, target)
    }

    private fun handleTPAccept(sender: Player) {
        val pendingRequest = tpaRequests[sender.uniqueId]
        if (pendingRequest != null) {
            val tpaPlayer = Bukkit.getPlayer(pendingRequest.tpaPlayer)
            val tpaToPlayer = Bukkit.getPlayer(pendingRequest.tpaToPlayer)
            if (tpaPlayer != null && tpaPlayer.isOnline && tpaToPlayer != null && tpaToPlayer.isOnline) {
                tpaPlayer.teleport(tpaToPlayer)
                tpaToPlayer.sendMessage("${tpaPlayer.name} has been teleported to you.")
                tpaPlayer.sendMessage("You have been teleported to ${tpaToPlayer.name}.")
                tpaRequests.remove(sender.uniqueId)
            } else {
                sender.sendMessage("Requester is no longer online.")
                tpaRequests.remove(sender.uniqueId)
            }
        } else {
            sender.sendMessage("You have no pending TPA requests.")
        }
    }

    private fun handleTPDeny(sender: Player) {
        val pendingRequest = tpaRequests[sender.uniqueId]
        if (pendingRequest != null) {
            val requester = Bukkit.getPlayer(pendingRequest.requester)
            if (requester != null && requester.isOnline) {
                requester.sendMessage("${sender.name} has denied your TPA request.")
                sender.sendMessage("You have denied the TPA request from ${requester.name}.")
                tpaRequests.remove(sender.uniqueId)
            } else {
                sender.sendMessage("Requester is no longer online.")
                tpaRequests.remove(sender.uniqueId)
            }
        } else {
            sender.sendMessage("You have no pending TPA requests.")
        }
    }

    private fun handleRequestTimeout(requester: Player, target: Player) {
        object : BukkitRunnable() {
            override fun run() {
                if (tpaRequests.containsKey(target.uniqueId)) {
                    tpaRequests.remove(target.uniqueId)
                    requester.sendMessage("Your TPA request to ${target.name} has timed out.")
                    target.sendMessage("The TPA request from ${requester.name} has timed out.")
                }
            }
        }.runTaskLater(this, requestTimeout * 20) // Timeout after 30 seconds
    }
}
