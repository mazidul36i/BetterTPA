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

package com.gliesestudio.mc.completer;

import com.gliesestudio.mc.service.warp.WarpStorage;
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

