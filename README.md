# WYNO GEN 🌍

<p align="center">
  <img src="assets/logo.png" alt="WynoWorldGen Logo" width="420">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Version-6.2.0-6C63FF.svg?style=for-the-badge" alt="Version">
  <img src="https://img.shields.io/badge/Java-21-F89820.svg?style=for-the-badge" alt="Java">
  <img src="https://img.shields.io/badge/Platform-Spigot%20%2F%20Paper-4CAF50.svg?style=for-the-badge" alt="Platform">
  <img src="https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge" alt="License">
  <a href="https://discord.gg/9WJSP4Kqg4">
    <img src="https://img.shields.io/badge/Discord-Join%20Us-7289DA.svg?style=for-the-badge" alt="Discord">
  </a>
</p>

<p align="center">
  <strong>Expertly Managed Difficulty-Based Survival Worlds with Full Nether & End Isolation.</strong><br>
  Enterprise-grade data isolation. Async database pipeline. Zero TPS impact.
</p>

---

## ✨ What is WynoWorldGen?

**WynoWorldGen** is a professional Minecraft Java plugin built for servers that run **multiple difficulty-based survival worlds**. Every Featured World created by the plugin is a completely isolated environment — inventory, XP, advancements, potion effects, and now **Nether & End dimensions** are all scoped per-world with zero data bleed between them.

---

## 🚀 Feature Highlights

### 🌍 Companion World System *(New in v6.2.0)*
When you create a Featured World, the plugin automatically creates **private Nether and End dimensions** for it:

| Featured World | Auto-Created Companions |
|:---|:---|
| `EasyWorld` | `EasyWorld-nether` + `EasyWorld-end` |
| `MediumWorld` | `MediumWorld-nether` + `MediumWorld-end` |
| `HardSMP` | `HardSMP-nether` + `HardSMP-end` |

- Players enter portals and are routed to that world's **private** Nether/End — never the server's global dimensions.
- All portal transitions perform a full **async data save + load**, keeping stats and inventory correct at every step.
- Companion worlds are automatically **loaded on startup** and **deleted alongside** their parent world.
- `/fw exit` works from inside Nether and End companion worlds too.

---

### 🔒 Data Isolation 2.0
Every managed world (parent + companions) has a fully independent player profile:
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
| `/fw join <name>` | Enter a featured world with isolated data. | `wynogen.use` |
| `/fw exit` | Save data and return to the main world. Works from Nether/End companions too. | `wynogen.use` |
| `/fw list` | List all featured worlds. `[N]`/`[E]` badges show companion status. | `wynogen.use` |
| `/fw create <name> <diff> [tight]` | Create a world (Easy/Medium/Hard) with optional tight biomes. | `wynogen.admin` |
| `/fw delete <name>` | Delete a world and all companion worlds. | `wynogen.admin` |
| `/fw reload` | Reload `config.yml` and `messages.yml` live. | `wynogen.admin` |
| `/fw update` | Check and install the latest plugin version. | `wynogen.admin` |
| `/fw rollback` | Revert the plugin to the previous backup JAR. | `wynogen.admin` |

*Aliases: `/featuredworld` · `/fw`*

---

## ⚙️ Configuration (`config.yml`)

```yaml
options:
  save_interval_ticks: 6000     # Auto-save interval (20 ticks = 1 second)
  safety_buffer_ticks: 60       # Teleport invulnerability window

  # Companion World Generation
  generate_nether: true          # Auto-create <name>-nether on world creation
  generate_end: true             # Auto-create <name>-end on world creation

  # Portal Access Control
  disable_nether_portals: false  # Block nether portal travel in featured worlds
  disable_end_portals: false     # Block end portal travel in featured worlds
  disable_portals: false         # Legacy: block ALL portals blindly

  respawn_in_same_world: true    # Respawn inside the current featured world on death
  metrics: true                  # Anonymous bStats usage data

  updater:
    auto_check: true
    notify_admins: true
```

---

## 📥 Installation

1. Download **[WynoWorldGen-6.2.0.jar](https://github.com/wynoislive/WynoWorldGen/releases/tag/v6.2.0)** from the latest release.
2. Place the JAR in your server's `plugins/` folder.
3. Restart the server — a default `config.yml` and `messages.yml` are generated automatically.
4. *(Optional)* Switch to MySQL in `config.yml` and run `/fw reload`.

### Upgrading from v5.x
Drop in the new JAR and restart. The plugin reads your existing `worlds.yml` and automatically migrates old world entries. No data loss.

---

## 📚 Wiki & Support

Advanced setup guides, developer reference, and localization documentation:
👉 **[WynoWorldGen Official Wiki](https://github.com/wynoislive/WynoWorldGen/wiki)**

Need help? Join the community:
👉 **[Discord Support Server](https://discord.gg/9WJSP4Kqg4)**

---

## 📦 Changelog

### v6.2.0 — Nether & End Companion Worlds
- Companion world auto-generation on `/fw create`
- Smart portal routing to private companion dimensions
- Async data-swap on all portal transitions
- `/fw exit` support from Nether/End companions
- `/fw list` companion status badges `[N]` `[E]`
- 4 new granular config options for portal/generation control
- Auto-save and respawn logic extended to companion worlds

### v5.1.0
- Auto-Updater & Rollback system
- MySQL support via HikariCP
- Advancement criteria isolation
- Tight biome provider (`WynoBiomeProvider`)

---

<p align="center">© 2026 <strong>WYNO</strong> — Developed for professional Minecraft environments.</p>
