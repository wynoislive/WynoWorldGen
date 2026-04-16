# Auto-Updater and Rollback

As a senior-developed plugin, **WynoWorldGen** includes a sophisticated maintenance system to ensure your server always has the latest security and feature updates.

## How it Works
The plugin periodically checks the [latest GitHub Release](https://github.com/wynoislive/WynoWorldGen/releases). If a newer version is found, administrators are notified in the console and upon joining the server.

## Performing an Update
1. Run `/fw update`.
2. The plugin will check for updates and, if one is available, begin an asynchronous download.
3. During the download, the plugin is fully functional.
4. Once completed, a restart is required to apply the new JAR.

## Safe Rollback
Updates can sometimes introduce conflicts with other plugins. This is why we include a **Safe Rollback** system.
- Before every update, the current JAR is backed up to `plugins/WYNO_GEN/backups/`.
- If you encounter issues, simply run `/fw rollback`.
- The plugin will restore the previous known-good version instantly.

---
[[Home]] | [[Installation Guide]]
