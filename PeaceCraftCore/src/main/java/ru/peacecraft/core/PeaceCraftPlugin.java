package ru.peacecraft.core;

import org.bukkit.plugin.java.JavaPlugin;

public final class PeaceCraftPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("PeaceCraftCore enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("PeaceCraftCore disabled.");
    }
}