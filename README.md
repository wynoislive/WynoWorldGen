# WYNO GEN 🌍
![Version](https://img.shields.io/badge/Version-5.1.0-blue.svg?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21-orange.svg?style=for-the-badge)
![API](https://img.shields.io/badge/Platform-Spigot%20%2F%20Paper-green.svg?style=for-the-badge)
![Discord](https://img.shields.io/badge/Discord-Join%20Us-7289DA.svg?style=for-the-badge)

<p align="center">
  <img src="assets/logo.png" alt="WynoWorldGen Logo" width="400">
</p>

**Expertly Managed Difficulty-Based Survival Worlds with Enterprise-Grade Data Isolation.**

WYNO GEN is a high-performance Minecraft Java plugin designed for massive scale. It enables server administrators to manage infinite survival worlds with varying difficulties while ensuring that player progress (inventories, stats, and states) is 100% isolated and safe.

---

## ✨ Features

- **🚀 Professional Scaling**: Built with an asynchronous data pipeline. All database operations happen in the background, keeping your server at a consistent 20 TPS.
- **🔄 Auto-Updater & Rollback**: Built-in system to keep your plugin up-to-date with one command, featuring automatic backups for 100% safe rollbacks.
- **📁 Universal Database Support**: 
  - **SQLite (Default)**: Zero-config! Auto-creates a local database for small servers.
  - **MySQL Enterprise**: Fully compatible with MySQL/MariaDB for high-traffic environments using HikariCP.
- **🛡️ Data Isolation 2.0**: Prevents all forms of data-bleed. Inventories, XP, potion effects, and **detailed advancement criteria** are saved per-world.
- **🌍 Advanced Generation**: Supports standard all-biome generation and a specialized `tight` mode for rapid biome access.
- **🎨 100% Translatable**: Every message, prefix, and color is managed in a professional `messages.yml`.

---

## 🛠 Commands & Permissions

| Command | Description | Permission |
|:--- |:--- |:--- |
| `/fw join <name>` | Joins a featured world and isolates your data. | `wynogen.use` |
| `/fw exit` | Safely saves featured data and returns you home. | `wynogen.use` |
| `/fw list` | Lists all active featured worlds. | `wynogen.use` |
| `/fw create <n> <d> [t]`| Creates a world (Easy/Medium/Hard). | `wynogen.admin` |
| `/fw delete <name>` | Unloads and recursively deletes a world. | `wynogen.admin` |
| `/fw update` | Checks and installs the latest plugin version. | `wynogen.admin` |
| `/fw rollback` | Reverts the plugin to the previous backup JAR. | `wynogen.admin` |
| `/fw reload` | Reloads configurations and messages. | `wynogen.admin` |

*Aliases: `/featuredworld` | `/fw`*

---

## 📥 Installation

1. Download the latest **[Release](https://github.com/wynoislive/WynoWorldGen/releases)**.
2. Place the JAR in your `plugins/` folder.
3. Restart the server. 
4. Configure `config.yml` if using MySQL.

---

## 📚 Wiki & Support

For advanced setup guides, developer info, and localization tips, visit our official Wiki:
👉 **[WynoWorldGen Official Wiki](https://github.com/wynoislive/WynoWorldGen/wiki)**

Need immediate help? Join our community:
👉 **[Discord Support Server](https://discord.gg/9WJSP4Kqg4)**

---
© 2026 **WYNO**. Developed for professional Minecraft environments.
