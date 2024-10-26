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

package com.gliesestudio.mc.service.warp;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getServer;

public class WarpStorage {
    private final File file;
    private final FileConfiguration config;
    private final Map<String, Location> warps = new HashMap<>();

    private final Logger logger;

    public WarpStorage(File dataFolder, Logger logger) {
        this.logger = logger;

        if (!dataFolder.exists() && dataFolder.mkdirs()) {
            logger.info("Created data folder for BetterTPA.");
        } else if (!dataFolder.exists()) {
            logger.severe("Failed to create data folder for BetterTPA.");
        }

        this.file = new File(dataFolder, "warps.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        loadWarps();
    }

    public void setWarp(String warpName, Location location) {
        warps.put(warpName, location);
        saveWarp(warpName, location);
    }

    public Location getWarp(String warpName) {
        return warps.get(warpName);
    }

    public Set<String> getWarpNames() {
        return warps.keySet();
    }

    public boolean warpExists(String warpName) {
        return warps.containsKey(warpName);
    }

    private void saveWarp(String warpName, Location location) {
        String path = "warps." + warpName;
        config.set(path + ".desc", warpName);
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());
        saveConfig();
    }

    private void loadWarps() {
        if (config.contains("warps")) {
            for (String warpName : config.getConfigurationSection("warps").getKeys(false)) {
                String path = "warps." + warpName;
                String worldName = config.getString(path + ".world");
                String desc = config.getString(path + ".desc");
                double x = config.getDouble(path + ".x");
                double y = config.getDouble(path + ".y");
                double z = config.getDouble(path + ".z");
                float yaw = (float) config.getDouble(path + ".yaw");
                float pitch = (float) config.getDouble(path + ".pitch");

                if (worldName == null) {
                    logger.severe("World name is not found for warp " + warpName + ".");
                    continue;
                }
                Location location = new Location(
                        getServer().getWorld(worldName),
                        x, y, z, yaw, pitch);
                warps.put(warpName, location);
            }
        }
    }

    private void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
