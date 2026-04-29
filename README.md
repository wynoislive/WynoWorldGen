# WYNO GEN 🌍

<p align="center">
  <img src="assets/logo.png" alt="WynoWorldGen Logo" width="420">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Version-6.2.2-6C63FF.svg?style=for-the-badge" alt="Version">
  <img src="https://img.shields.io/badge/Java-21-F89820.svg?style=for-the-badge" alt="Java">
  <img src="https://img.shields.io/badge/Platform-Spigot%20%2F%20Paper-4CAF50.svg?style=for-the-badge" alt="Platform">
  <img src="https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge" alt="License">
  <a href="https://discord.gg/9WJSP4Kqg4">
    <img src="https://img.shields.io/badge/Discord-Join%20Us-7289DA.svg?style=for-the-badge" alt="Discord">
  </a>
</p>

<p align="center">
  <strong>Expertly Managed Difficulty-Based Survival Worlds with Seamless Persistence.</strong><br>
  Enterprise-grade data isolation. Async database pipeline. Zero TPS impact.
</p>

---

## ✨ What is WynoWorldGen?

**WynoWorldGen** is a professional-grade world management solution for Minecraft servers. It allows you to create multiple, difficulty-specific survival environments (`EASY`, `MEDIUM`, `HARD`) that are completely isolated from one another. Each environment includes its own private Overworld, Nether, and End dimensions.

---

## 🚀 Feature Highlights

- **🚀 Professional Scaling**: Built with an asynchronous data pipeline. All database operations happen in the background, keeping your server at a consistent 20 TPS.
- **🌌 Dimensional Unity (v6.2+)**: Private Nether and End dimensions for every world. Shared inventory and health across all three dimensions of a mode.
- **📍 Native Persistence**: Last-location tracking. Join back exactly where you left off, even in the Nether or End.
- **🛡️ Data Isolation 2.0**: Prevents all forms of data-bleed. Inventories, XP, potion effects, and **detailed advancement criteria** are saved per-world.
- **🔄 Auto-Updater & Rollback**: Built-in system to keep your plugin up-to-date with one command, featuring automatic backups for 100% safe rollbacks.
- **📁 Universal Database Support**: 
  - **SQLite (Default)**: Zero-config! Auto-creates a local database for small servers.
  - **MySQL Enterprise**: Fully compatible with MySQL/MariaDB for high-traffic environments using HikariCP pooling.
- **🌍 Advanced Generation**: Supports standard all-biome generation and a specialized `tight` mode for rapid biome access.
- **🎨 100% Translatable**: Every message, prefix, and color is managed in a professional `messages.yml`.

---

## 🌍 Companion World System

When you create a Featured World, the plugin automatically creates private dimensions:

| Mode | Overworld | Nether | End |
|:---|:---|:---|:---|
| **Easy** | `EasyWorld` | `EasyWorld-nether` | `EasyWorld-end` |
| **Medium** | `MediumWorld` | `MediumWorld-nether` | `MediumWorld-end` |
| **Hard** | `HardWorld` | `HardWorld-nether` | `HardWorld-end` |

- **Smart Respawn**: Dying in the Nether or End sends you back to your Bed Spawn in the parent Overworld.
- **Smooth Portals**: Seamless transition between dimensions with zero sync lag.

---

## 🛠 Commands & Usage

| Command | Description | Permission |
|:---|:---|:---|
| `/fw join <name>` | Joins a featured world and restores last location. | `wynogen.use` |
| `/fw exit` | Safely saves featured data and returns you home. | `wynogen.use` |
| `/fw list` | Lists all active featured worlds and dimension status. | `wynogen.use` |
| `/fw create <n> <d> [t]`| Creates a world (Easy/Medium/Hard). | `wynogen.admin` |
| `/fw delete <name>` | Unloads and recursively deletes a world & companions. | `wynogen.admin` |
| `/fw reload` | Reloads configurations and messages. | `wynogen.admin` |
| `/fw update` | Checks and installs the latest plugin version. | `wynogen.admin` |
| `/fw rollback` | Reverts the plugin to the previous backup JAR. | `wynogen.admin` |

*Aliases: `/featuredworld` | `/fw`*

---

## 📥 Installation

1.  **Download**: Get the latest **[WynoWorldGen-6.2.2.jar](https://github.com/wynoislive/WynoWorldGen/releases/tag/v6.2.2)** from the latest release.
2.  **Upload**: Place the JAR in your server's `plugins/` folder.
3.  **Start**: Restart the server — a default `config.yml` and `messages.yml` are generated automatically.
4.  **Configure**: *(Optional)* Switch to MySQL in `config.yml` and run `/fw reload`.

### Upgrading from v5.x
Drop in the new JAR and restart. The plugin reads your existing `worlds.yml` and automatically migrates old world entries. No data loss.

---

## ⚙️ Configuration (`config.yml`)

```yaml
options:
  save_interval_ticks: 6000     # How often data auto-saves (300 seconds)
  safety_buffer_ticks: 60       # Ticks of invulnerability after world change
  
  # Dimension Management
  generate_nether: true          # Auto-create Nether for new worlds
  generate_end: true             # Auto-create End for new worlds
  
  # Portal Controls
  disable_nether_portals: false  # Block Nether portal travel
  disable_end_portals: false     # Block End portal travel
  disable_portals: false         # Legacy blanket block
  
  respawn_in_same_world: true    # Keep player in the Mode world on death
  metrics: true                  # Support development with bStats

  updater:
    auto_check: true             # Notify admins of new releases
    notify_admins: true
```

---

## 🗄️ Database Setup

### Option A: SQLite (Small Servers)
Simply leave the `database.type` as `SQLITE`. The plugin will create a local `data.db` file automatically.

### Option B: MySQL (High Performance)
1.  Change `database.type` to `MYSQL`.
2.  Fill in your `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, and `DB_PASSWORD`.
3.  Run `/fw reload`. The plugin will migrate your connection instantly.

---

## 📚 Support & Wiki

For advanced setup, developer API, and detailed localization guides:
👉 **[WynoWorldGen Official Wiki](https://github.com/wynoislive/WynoWorldGen/wiki)**

Join our developer community for real-time support:
👉 **[Discord Support Server](https://discord.gg/9WJSP4Kqg4)**

---

## 📦 Changelog

### v6.2.2 — Portal & Compatibility Fixes
- **Linked Portal Generation**: Fixed return portals not spawning by using native teleportation logic.
- **Coordinate Scaling**: Accurate 1:8 Nether-to-Overworld scaling for precise portal linking.
- **Cross-Plugin Sync**: Global listener detects teleports from other plugins and swaps data instantly.
- **Custom Spawn Support**: End portals now respect `/setworldspawn` in custom End dimensions.
- **Smart Respawn**: Added Bed-Spawn support for End exit portals.
- **Message Refinement**: Fixed placeholder bugs and improved companion world feedback.

### v6.2.1 — Persistence & Respawn Update
- **Shared Dimension Profiles**: Inventory and hearts are now shared between a world and its companions.
- **Last Location Persistence**: Players now join back at their exact last location (X, Y, Z).
- **Advanced Respawn**: Die in Nether/End -> Respawn in Overworld (Bed spawn supported).
- **Smooth Portals**: Direct teleportation between dimensions with no sync delay.

### v6.2.0
- Companion world auto-generation on `/fw create`.
- Smart portal routing to private companion dimensions.
- 4 new granular config options for portal/generation control.

### v5.1.0
- Auto-Updater & Rollback system.
- MySQL support via HikariCP connection pooling.
- Advancement criteria isolation across worlds.
- Optimized `WynoBiomeProvider` for tight biome generation.

---

<p align="center">© 2026 <strong>WYNO</strong> — Professional World Generation Architecture.</p>
