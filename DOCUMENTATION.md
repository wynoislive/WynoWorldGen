# WYNO GEN Documentation 📚

Welcome to the official documentation for WYNO GEN. This guide covers advanced configurations, architecture details, and world management logic.

---

## 🏗 Database Architecture

WYNO GEN uses a **Dual-Dialect SQL Engine**. This allows the plugin to be extremely flexible for different server environments.

### 1. SQLite (Automatic)
- **Best for**: Small to medium servers, local testing, or standalone setups.
- **Setup**: Set `database.type: "SQLITE"`. The plugin will create a `data.db` file in the plugin folder.

### 2. MySQL Enterprise
- **Best for**: Large networks, BungeeCord/Velocity environments, and high-concurrency servers.
- **Setup**: Set `database.type: "MYSQL"` and provide credentials.
- **Efficiency**: Built on **HikariCP** for high-performance connection pooling.

**Table Structure**:
- `uuid`: Primary Key (Player UUID)
- `world_id`: Primary Key (Featured World Name or "default")
- `inventory`, `armor`, `ender_chest`, `stats`, `potions`, `advancements`: Serialized Data Blobs.

---

## 🌍 World Generation Engine

### Standard Generation
Worlds are created using the default Minecraft generator, allowing for infinite exploration.

### "Tight" Generation Flag
When running `/fw create <name> <diff> tight`, the plugin uses a custom `WynoBiomeProvider`.
- **Goal**: Consolidate all major biomes into the central **3,000 x 3,000** block area.
- **Usage**: Perfect for "Speedrun" or "Challenge" style featured worlds where players need quick access to diverse resources.

---

## 🏆 Advanced Advancement Tracking

Unlike standard plugins, WYNO GEN tracks advancements at the **Criteria Level**.
- **What this means**: If an advancement requires "Visiting 10 Biomes," and a player has visited 4, WYNO GEN saves exactly which 4 biomes were visited.
- **Restoration**: When the player returns to that world, their exact progress is restored, preventing them from "cheating" or losing partial progress.

---

## 🛡 Safety Mechanisms

### Teleport Buffer (`wynogen_safety`)
To prevent "loading deaths," players receive a **3-second invulnerability** period upon joining a world. This is configurable in `config.yml`.

### Portal Protection
By default, portals are disabled in featured worlds to prevent players from entering dimensions not managed by the plugin. Use `/fw exit` for all departures.

---

## 🛠 Developer Info
- **Main Entry**: `WynoGen.java`
- **Data Manager**: Handles all Async I/O and serialization logic.
- **Language Manager**: Handles `messages.yml` lifecycle and colored string parsing.

---
*For further support, please contact the development team at the main repository.*
