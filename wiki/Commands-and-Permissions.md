# Commands and Permissions

WynoWorldGen uses a streamlined command system. All primary features are accessible via `/fw` (alias for `/featuredworld`).

## Player Commands
These commands are generally used by players to navigate the custom worlds.

| Command | Description | Permission |
|:--- |:--- |:--- |
| `/fw join <world>` | Teleports the player to a featured world. | `wynogen.use` |
| `/fw exit` | Returns the player to the main world. | `wynogen.use` |
| `/fw list` | Lists all currently active featured worlds. | `wynogen.use` |

## Administrative Commands
These commands are used for world creation, maintenance, and administrative tasks.

| Command | Description | Permission |
|:--- |:--- |:--- |
| `/fw create <name> <diff> [tight]` | Creates a new featured world. | `wynogen.admin` |
| `/fw delete <name>` | Deletes a featured world and its data. | `wynogen.admin` |
| `/fw reload` | Reloads all configurations and messages. | `wynogen.admin` |
| `/fw update` | Checks and installs the latest plugin version. | `wynogen.admin` |
| `/fw rollback` | Reverts the plugin to the previous backup JAR. | `wynogen.admin` |

## Permission Nodes

- `wynogen.use`: Allows joining/exiting featured worlds. (Default: Everyone)
- `wynogen.admin`: Allows world creation, deletion, and plugin management. (Default: OP)

---
[[Home]] | [[Configuration Guide]]
