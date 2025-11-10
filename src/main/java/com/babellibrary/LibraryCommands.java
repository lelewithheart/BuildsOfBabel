package com.babellibrary;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class LibraryCommands implements CommandExecutor {
    private final BabelLibraryPlugin plugin;

    public LibraryCommands(BabelLibraryPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may use this command.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("Usage: /library visit <x> <y> <z>  OR  /library locate");
            return true;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("visit")) {
            if (args.length != 4) {
                player.sendMessage("Usage: /library visit <x> <y> <z>");
                return true;
            }
            // Parse big integer coordinates
            try {
                BigInteger cx = new BigInteger(args[1]);
                BigInteger cy = new BigInteger(args[2]);
                BigInteger cz = new BigInteger(args[3]);

                BigInteger s = CoordinateCodec.combineChunks(cx, cy, cz);

                // regenerate room synchronously on main thread
                World room = plugin.getRoomWorld();
                Location origin = plugin.getRoomOrigin();

                Bukkit.getScheduler().runTask((JavaPlugin) plugin, () -> {
                    BuildGenerator.clearRoom(room, origin);
                    BuildGenerator.generateFromBigInteger(s, room, origin);
                    // teleport player to center
                    Location tp = origin.clone().add(BuildGenerator.SIZE / 2.0 + 0.5, 1.2, BuildGenerator.SIZE / 2.0 + 0.5);
                    tp.setWorld(room);
                    player.teleport(tp);
                    player.sendMessage("Teleported to library coordinates.");
                });

            } catch (Exception ex) {
                player.sendMessage("Invalid coordinates: must be numeric big integers.");
            }
            return true;
        } else if (sub.equals("locate")) {
            // Analyze the 16x16x16 room where the player currently is. We assume they built at the canonical room origin.
            World room = plugin.getRoomWorld();
            Location origin = plugin.getRoomOrigin();
            // For simplicity we will analyze the canonical room area anchored at plugin.getRoomOrigin()
            String[] strs = new String[CoordinateCodec.CELLS];
            int idx = 0;
            for (int y = 0; y < BuildGenerator.HEIGHT; y++) {
                for (int z = 0; z < BuildGenerator.SIZE; z++) {
                    for (int x = 0; x < BuildGenerator.SIZE; x++) {
                        org.bukkit.block.Block b = room.getBlockAt(origin.getBlockX() + x, origin.getBlockY() + y, origin.getBlockZ() + z);
                        try {
                            strs[idx++] = b.getBlockData().getAsString();
                        } catch (Throwable t) {
                            // fallback to material name
                            strs[idx++] = b.getType().name().toLowerCase();
                        }
                    }
                }
            }

            BigInteger s = CoordinateCodec.patternToBigInteger(strs);
            BigInteger[] parts = CoordinateCodec.splitToChunks(s);

            // Create a written book with the coordinates
            ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
            BookMeta meta = (BookMeta) book.getItemMeta();
            meta.setTitle("Babel Coordinates");
            meta.setAuthor("BabelLibrary");
            List<String> pages = new ArrayList<>();
            pages.add("Library coordinates (paste into /library visit):\nX: " + parts[0].toString());
            pages.add("Y: " + parts[1].toString() + "\nZ: " + parts[2].toString());
            pages.add("Notes: These are deterministic coordinates that reproduce your build in the single 16x16x16 room world.");
            meta.setPages(pages);
            book.setItemMeta(meta);

            player.getInventory().addItem(book);
            player.sendMessage("You received a book with coordinates for the current room build.");
            return true;
        }

        player.sendMessage("Unknown subcommand. Use visit or locate.");
        return true;
    }
}
