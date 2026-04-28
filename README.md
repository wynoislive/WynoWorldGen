# WYNO GEN 🌍

<p align="center">
  <img src="assets/logo.png" alt="WynoWorldGen Logo" width="420">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Version-6.2.1-6C63FF.svg?style=for-the-badge" alt="Version">
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

**WynoWorldGen** is a professional Minecraft Java plugin built for servers that run **multiple difficulty-based survival worlds**. Every Featured World created by the plugin is a completely isolated environment — inventory, XP, advancements, potion effects, and now **Nether & End dimensions** are all scoped per-world with zero data bleed between them.

---

## 🚀 Feature Highlights

### 🌍 Companion World System *(v6.2+)*
When you create a Featured World, the plugin automatically creates **private Nether and End dimensions** for it:

| Featured World | Auto-Created Companions |
|:---|:---|
| `EasyWorld` | `EasyWorld-nether` + `EasyWorld-end` |
| `MediumWorld` | `MediumWorld-nether` + `MediumWorld-end` |
| `HardSMP` | `HardSMP-nether` + `HardSMP-end` |

- **Shared Dimension Profiles**: Your inventory and health are identical across the Overworld, Nether, and End of the same mode.
- **Last Location Persistence**: Join back exactly where you left. If you exit in the Nether, you join back in the Nether.
- **Overworld Respawn**: Dying in the Nether or End will correctly respawn you in the **parent overworld** (respecting your bed spawn).
- **Smooth Portals**: Seamless transition between dimensions with no data-sync lag.

---

### 🔒 Data Isolation 2.0
Every managed world mode has a fully independent player profile:
- Inventory & Armor
- Ender Chest
- Health, Food, Saturation, XP, Level, GameMode
- Active Potion Effects
- Advancement Criteria (tracked and restored per-world)

---

### ⚡ Async-First Architecture
All database reads and writes run on background threads via `CompletableFuture`. The main game thread is **never blocked**, ensuring a stable 20 TPS regardless of player count.

---

### 🗄️ Universal Database Support
| Type | Details |
|:---|:---|
| **SQLite** *(default)* | Zero-config — auto-creates a local `.db` file. Perfect for small servers. |
| **MySQL / MariaDB** | Full HikariCP connection pooling for high-traffic enterprise environments. |

---

### 🔄 Auto-Updater & Rollback
- `/fw update` — downloads and installs the latest release in one command.
- `/fw rollback` — instantly reverts to the previous backed-up JAR.
- Admins are notified on login if a new version is available.

---

### 🎨 Fully Translatable
Every message, prefix, and color code lives in `messages.yml`. No recompilation needed.

---

## 🛠 Commands & Permissions

| Command | Description | Permission |
|:---|:---|:---|
| `/fw join <name>` | Enter a featured world. Restores your last exact location. | `wynogen.use` |
| `/fw exit` | Save data and return to the main world. | `wynogen.use` |
| `/fw list` | List all featured worlds. `[N]`/`[E]` badges show companion status. | `wynogen.use` |
| `/fw create <name> <diff> [tight]` | Create a world (Easy/Medium/Hard) with optional tight biomes. | `wynogen.admin` |
| `/fw delete <name>` | Delete a world and all companion worlds. | `wynogen.admin` |
| `/fw reload` | Reload `config.yml` and `messages.yml` live. | `wynogen.admin` |
| `/fw update` | Check and install the latest plugin version. | `wynogen.admin` |
| `/fw rollback` | Revert the plugin to the previous backup JAR. | `wynogen.admin` |

*Aliases: `/featuredworld` · `/fw`*

---

## 📥 Installation

1. Download **[WynoWorldGen-6.2.1.jar](https://github.com/wynoislive/WynoWorldGen/releases/tag/v6.2.1)** from the latest release.
2. Place the JAR in your server's `plugins/` folder.
3. Restart the server — a default `config.yml` and `messages.yml` are generated automatically.
4. *(Optional)* Switch to MySQL in `config.yml` and run `/fw reload`.

---

## 📦 Changelog

### v6.2.1 — Persistence & Respawn Update
- **Shared Dimension Profiles**: Inventory and hearts are now shared between a world and its companions.
- **Last Location Persistence**: Players now join back at their exact last location (X, Y, Z).
- **Advanced Respawn**: Die in Nether/End -> Respawn in Overworld (Bed spawn supported).
- **Smooth Portals**: Direct teleportation between dimensions with no sync delay.

### v6.2.0
- Companion world auto-generation on `/fw create`.
- Smart portal routing to private companion dimensions.
- 4 new granular config options for portal/generation control.

---

<p align="center">© 2026 <strong>WYNO</strong> — Developed for professional Minecraft environments.</p>
