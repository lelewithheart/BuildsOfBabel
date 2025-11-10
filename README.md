# BabelLibrary

An illusionary "infinite" library for Minecraft: a single 16×16×16 room world is regenerated on-the-fly so every possible room (including block variants and block states) can be visited via deterministic coordinates.

This repository contains a Bukkit/Spigot plugin that:
- Uses a single world named `babel_room` as the canonical 16×16×16 room.
- Deterministically generates a room from three BigInteger coordinate parts (x,y,z). The three parts are concatenated and interpreted as a single serialized byte stream that encodes the full BlockData of every voxel.
- Allows players to capture a build with `/library locate` (gives a Written Book containing the coordinates) and to visit a coordinate triplet with `/library visit <x> <y> <z>` (regenerates the room and teleports the player).

Contents
- `src/main/java/com/babellibrary` — plugin sources
- `src/main/resources/plugin.yml` — plugin descriptor
- `pom.xml` — minimal Maven build file (targets Spigot 1.16.5 API by default)

Requirements
- Java 8 (1.8) runtime for building and server.
- Spigot / PaperMC server (tested conceptually against 1.16.x; BlockData API used works on 1.13+).

Build
1. From this repository root, build the plugin with Maven. If you have Maven installed system-wide, run (PowerShell):

```powershell
mvn package
```

2. No Maven installed? Use the included lightweight wrapper scripts. On Windows run:

```powershell
.\mvnw.cmd package
```

On macOS/Linux (or WSL):

```bash
./mvnw package
```

The wrapper downloads a small Apache Maven binary into `.mvn/wrapper/` on first run and invokes it.

3. After a successful build, the plugin JAR will be in `target/` (for example `target/BabelLibrary-1.0-SNAPSHOT.jar`).

Install
1. Copy the JAR into your server's `plugins/` directory.
2. Start the server. The plugin will create or load a world named `babel_room` and register the `/library` command.

Usage
- `/library visit <x> <y> <z>` — Regenerates the canonical 16×16×16 room from the three BigInteger parts and teleports you into the room. Example usage is to copy the values from the written book produced by `/library locate`.
- `/library locate` — Scans the canonical room anchored at the plugin origin (default `world 'babel_room'` at coordinate corner (0,64,0)), extracts each block's `BlockData#getAsString()`, encodes it into three BigInteger parts, and gives the player a written book with the three values.

Notes and behaviour details
- Full block variants supported: The plugin records exact `BlockData` strings (e.g., slab types, orientations, colors, etc.) and tries to re-create them using `Bukkit.createBlockData(String)`. If a `BlockData` string cannot be parsed on the running server version, the code falls back to the material name or `minecraft:air`.
- Coordinates are large: Because the entire 16×16×16 room is losslessly serialized, the decimal BigInteger strings are typically very long. This is expected for perfect reversibility.
- Default room origin: The plugin uses the fixed room origin at (X=0, Y=64, Z=0) inside a world named `babel_room`. The 16×16×16 cube expands +X and +Z from that corner. You can change the origin by editing `BabelLibraryPlugin.java`.
- `/library locate` currently inspects the canonical room only — if you want to build anywhere and capture the room there, I can add logic to detect the 16×16 region the player is standing in.

Performance & safety
- Regenerating a full 16×16×16 room writes 4096 block states synchronously on the main thread. That can cause a visible tick spike on low-end hosts. If you notice lag, I can:
  - spread writes across multiple ticks,
  - use a batch snapshot/NMS approach, or
  - compress and stream changes more efficiently.

Shortening coordinate strings (optional enhancements)
- Compression: Compress the serialized byte stream (gzip/deflate) before converting to BigInteger. This dramatically shortens coordinates for typical sparse builds. I can add this by default or as an option.
- Base encoding: Instead of passing decimal BigInteger parts, provide base36/base64-URL-safe strings for readability.
- Registry: Persist patterns to a database and give players a short ID instead of the full coordinates.

Troubleshooting
- If `Bukkit.createBlockData` throws on some block strings, ensure your server version supports those block states (Paper/Spigot 1.13+ required). Block state syntax may differ between Minecraft versions.
- If `/library visit` doesn't teleport you or the room looks incorrect, ensure the plugin created/loaded the `babel_room` world and that the server has permission to create worlds.

Development notes
- The source is minimal and intentionally explicit. For production use you'll likely want:
  - rate-limiting or permissions checking for regeneration commands
  - asynchronous or tick-chunked generation to avoid spikes
  - persistence for common patterns

Next steps I can implement for you (pick one):
- Make `/library locate` scan the 16×16×16 region where the player currently stands (so players may build anywhere).
- Add gzip compression to reduce coordinate size before converting to BigInteger.
- Spread block writes across ticks to avoid server lag.

If you'd like a specific improvement implemented, tell me which one and I'll update the code.

License
- This project is provided as-is. Use it and modify it as you wish.
