# Localization Guide

WynoWorldGen is designed for global communities. Every string the player sees is fully translatable in the `messages.yml` file.

## Translating the Plugin
To add support for a new language, modify the `messages.yml` file in your plugin folder.

### Example: Spanish (Español)
```yaml
general:
  only-players: "&cSolo los jugadores pueden usar este comando."
  no-permission: "&cNo tienes permiso para hacer esto."
  world-not-found: "&cEse mundo no es un mundo destacado o no existe."
```

### Example: Japanese (日本語)
```yaml
general:
  only-players: "&cこのコマンドはプレイヤーのみが使用できます。"
  no-permission: "&cこの操作を行う権限がありません。"
  world-not-found: "&cそのワールドは特集ワールドではないか、存在しません。"
```

## Professional Tip
Always use standard Minecraft color codes (`&b`, `&f`, `&7`, etc.) to maintain a premium feel. We recommend keeping the `prefix` consistent to ensure your players recognize the plugin messages easily.

---
[[Home]] | [[Configuration Guide]]