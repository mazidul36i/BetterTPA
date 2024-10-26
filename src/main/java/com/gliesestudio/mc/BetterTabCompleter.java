package com.gliesestudio.mc;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BetterTabCompleter implements TabCompleter {

    private final WarpStorage warpStorage;

    public BetterTabCompleter(WarpStorage warpStorage) {
        this.warpStorage = warpStorage;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return switch (command.getName().toLowerCase()) {
            // Handle tab completion for /tpa and /tphere commands.
            case "tpa", "tphere" -> tpaSuggestions(sender, args);

            // Handle tab completion for /warp command.
            case "warp" -> warpSuggestions(sender, args);

            // Return empty list to prevent any uncontrolled suggestions.
            default -> new ArrayList<>();
        };
    }

    private List<String> tpaSuggestions(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1 && sender instanceof Player player) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.getName().equalsIgnoreCase(player.getName())) {
                    if (onlinePlayer.getName().toLowerCase().contains(args[0].toLowerCase())) {
                        suggestions.add(onlinePlayer.getName());
                    }
                }
            }
        }
        return suggestions;
    }

    private List<String> warpSuggestions(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1 && sender instanceof Player) {
            // Provide autocompletion for warp names.
            Set<String> warps = warpStorage.getWarpNames();
            for (String warp : warps) {
                if (warp.toLowerCase().contains(args[0].toLowerCase())) {
                    suggestions.add(warp);
                }
            }
        }
        return suggestions;
    }


}

