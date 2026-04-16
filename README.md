# WYNO GEN 🌍
![Version](https://img.shields.io/badge/Version-5.0.0-blue.svg?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21-orange.svg?style=for-the-badge)
![API](https://img.shields.io/badge/Platform-Spigot%20%2F%20Paper-green.svg?style=for-the-badge)

**Expertly Managed Difficulty-Based Survival Worlds with Enterprise-Grade Data Isolation.**

WYNO GEN is a high-performance Minecraft Java plugin designed for massive scale. It enables server administrators to manage infinite survival worlds with varying difficulties while ensuring that player progress (inventories, stats, and states) is 100% isolated and safe.

---

## ✨ Features

- **🚀 Professional Scaling**: Built with an asynchronous data pipeline. All database operations happen in the background, keeping your server at a consistent 20 TPS.
- **📁 Universal Database Support**: 
  - **SQLite (Default)**: Zero-config! Auto-creates a local database for small servers.
  - **MySQL Enterprise**: Fully compatible with MySQL/MariaDB for high-traffic environments.
- **🛡️ Data Isolation 2.0**: Prevents all forms of data-bleed. Inventories, XP, potion effects, and **detailed advancement criteria** are saved per-world.
- **🌍 Advanced Generation**: Supports standard all-biome generation and a specialized `tight` mode for rapid biome access.
- **🎨 100% Translatable**: Every message, prefix, and color is managed in a professional `messages.yml`.
- **⚡ Management Suite**: Includes world deletion, automated safety buffers (invulnerability), and on-the-fly configuration reloading.

---

## 🛠 Commands & Permissions

| Command | Description | Permission |
|:--- |:--- |:--- |
| `/fw join <name>` | Joins a featured world and isolates your data. | `wynogen.use` |
| `/fw exit` | Safely saves featured data and returns you home. | `wynogen.use` |
| `/fw list` | Lists all active featured worlds. | `wynogen.use` |
| `/fw create <n> <d> [t]`| Creates a world (Easy/Medium/Hard). | `wynogen.admin` |
| `/fw delete <name>` | Unloads and recursively deletes a world. | `wynogen.admin` |
| `/fw reload` | Reloads configurations and messages. | `wynogen.admin` |

*Aliases: `/featuredworld`*

---

## 📊 Data & State Tracking

The following data is strictly isolated per world:
- **Inventories**: Main Inventory, Armor, Off-hand, and Ender Chest.
- **Vitals**: Health, Hunger, Saturation, and XP.
- **Progression**: Advanced Advancement tracking (individual criteria level).
- **Status**: All active potion effects and current game mode.

---

## ⚙️ Configuration

### Database Setup
```yaml
database:
  type: "SQLITE" # Use SQLITE or MYSQL
  DB_HOST: "localhost"
  DB_USER: "root"
  # ... detailed credentials
```

---

## 📥 Installation

1. Download the latest **[Release](https://github.com/wynoislive/WynoWorldGen/releases)**.
2. Place the JAR in your `plugins/` folder.
3. Restart the server. 
   - **SQLite**: No further steps.
   - **MySQL**: Configure `config.yml` and run `/fw reload`.

---

## 📚 Wiki & Documentation
Looking for advanced setup guides or developer info? Check out the **[DOCUMENTATION.md](DOCUMENTATION.md)**.

---
© 2026 **WYNO**. Developed for professional Minecraft environments.
