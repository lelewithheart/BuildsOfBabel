package com.babellibrary;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

public class BabelLibraryPlugin extends JavaPlugin {
    public static final String ROOM_WORLD_NAME = "babel_room";
    private World roomWorld;
    private Location roomOrigin;

    @Override
    public void onEnable() {
        getLogger().info("BabelLibrary starting...");

        // Load or create the single room world
        WorldCreator wc = new WorldCreator(ROOM_WORLD_NAME);
        roomWorld = Bukkit.getWorld(ROOM_WORLD_NAME);
        if (roomWorld == null) {
            roomWorld = wc.createWorld();
            getLogger().info("Created world: " + ROOM_WORLD_NAME);
        } else {
            getLogger().info("Loaded world: " + ROOM_WORLD_NAME);
        }

        // Room origin (corner) inside the room world. Use a safe fixed Y.
        roomOrigin = new Location(roomWorld, 0, 64, 0);

        // Register commands
        LibraryCommands commands = new LibraryCommands(this);
        if (getCommand("library") != null) getCommand("library").setExecutor(commands);

        getLogger().info("BabelLibrary enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("BabelLibrary disabled.");
    }

    public World getRoomWorld() {
        return roomWorld;
    }

    public Location getRoomOrigin() {
        return roomOrigin;
    }
}
