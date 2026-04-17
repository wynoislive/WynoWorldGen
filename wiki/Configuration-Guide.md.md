# Configuration Guide

WynoWorldGen provides a highly customizable environment through its two primary configuration files.

## config.yml
This file handles the technical core of the plugin, including database connectivity and world management options.

### Notable Settings
- `save_interval_ticks`: Frequency of automatic asynchronous data saves for players.
- `safety_buffer_ticks`: Duration of invulnerability after teleporting to prevent "spawn killing" or accidental damage.
- `updater.auto_check`: Enable/Disable background version checking.

## messages.yml
Everything the player sees in-game can be translated here. This file supports standard Bukkit color codes (`&`).

### Placeholder Support
- `{name}`: Used in world-related messages.
- `{version}`: Used in update alerts.
- `{difficulty}`: Displays the world difficulty level.

> [!TIP]
> Always reload using `/fw reload` after making changes to these files to see them apply in-game without a restart.

---
[[Home]] | [[Localization Guide]]