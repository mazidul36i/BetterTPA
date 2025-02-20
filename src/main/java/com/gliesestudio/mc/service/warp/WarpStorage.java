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
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getServer;

/**
 * This class manages the storage and retrieval of warp locations.
 *
 * @author Mazidul Islam
 * @version 1.0
 * @since 1.0
 */
public class WarpStorage {

    private static final String CONFIG_FILE = "warps.yml";
    public static final String WARP_PATH_DESC = ".desc";
    public static final String WARP_PATH_WORLD = ".world";
    public static final String WARP_PATH_X = ".x";
    public static final String WARP_PATH_Y = ".y";
    public static final String WARP_PATH_Z = ".z";
    public static final String WARP_PATH_YAW = ".yaw";
    public static final String WARP_PATH_PITCH = ".pitch";

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

        this.file = new File(dataFolder, CONFIG_FILE);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    logger.info("Created config file 'warps.yml'");
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to create config file 'warps.yml'", e);
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

    public void deleteWarp(String warpName) {
        warps.remove(warpName);
        config.set(warpName, null);
        saveConfig();
    }

    private void saveWarp(String warpName, Location location) {
        config.set(warpName + WARP_PATH_DESC, warpName);
        config.set(warpName + WARP_PATH_WORLD, location.getWorld().getName());
        config.set(warpName + WARP_PATH_X, location.getX());
        config.set(warpName + WARP_PATH_Y, location.getY());
        config.set(warpName + WARP_PATH_Z, location.getZ());
        config.set(warpName + WARP_PATH_YAW, location.getYaw());
        config.set(warpName + WARP_PATH_PITCH, location.getPitch());
        saveConfig();
    }

    private void loadWarps() {
        Configuration configRoot = config.getRoot();
        if (configRoot != null) {
            logger.info("Loading warps...");
            for (String warpName : configRoot.getKeys(false)) {
                String worldName = config.getString(warpName + WARP_PATH_WORLD);
                double x = config.getDouble(warpName + WARP_PATH_X);
                double y = config.getDouble(warpName + WARP_PATH_Y);
                double z = config.getDouble(warpName + WARP_PATH_Z);
                float yaw = (float) config.getDouble(warpName + WARP_PATH_YAW);
                float pitch = (float) config.getDouble(warpName + WARP_PATH_PITCH);

                if (worldName == null) {
                    logger.log(Level.WARNING, "Missing world name for warp '{}'", warpName);
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
            logger.log(Level.SEVERE, "Failed to save config file 'warps.yml'", e);
        }
    }
}
