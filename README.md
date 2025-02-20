# Better TPA

It is a simple plugin to manage player teleportation (TPA) and set warps with ease. Just install the plugin into your
`plugins` directory (folder) and you are good to go. No need to configure anything.

## Features

- **TPA:** Request to teleport to another player.
- **TPHere:** Request to teleport another player to you.
- **TPAccept:** Accept a teleport request.
- **TPDeny:** Deny a teleport request.
- **Back:** Teleport back to your last death or TPA location.
- **SetWarp:** Set a warp location.
- **Warp:** Teleport to a warp.
- **Warps:** List available warps.
- **DelWarp:** Delete a warp location.

## Installation

1. Download the latest release of BetterTPA from the releases page.
2. Place the BetterTPA.jar file in your server's `plugins` directory.
3. Restart your server.

## Commands

| Command            | Description                                            | Usage                |
|--------------------|--------------------------------------------------------|----------------------|
| `/tpa <player>`    | Request to teleport to another player.                 | `/tpa PlayerName`    |
| `/tphere <player>` | Request to teleport another player to you.             | `/tphere PlayerName` |
| `/tpaccept`        | Accept a teleport request.                             | `/tpaccept`          |
| `/tpdeny`          | Deny a teleport request.                               | `/tpdeny`            |
| `/back`            | Teleport back to your last death or teleport location. | `/back`              |
| `/setwarp <name>`  | Set a warp location.                                   | `/setwarp MyWarp`    |
| `/warp <name>`     | Teleport to a warp.                                    | `/warp MyWarp`       |
| `/warps`           | List available warps.                                  | `/warps`             |
| `/delwarp`         | Delete a warp location.                                | `/delwarp MyWarp`    |

## Permissions

| Permission          | Description                                       | Default |
|---------------------|---------------------------------------------------|---------|
| `bettertpa.tpa`     | Allows players to use the teleportation commands. | ✅       |
| `bettertpa.back`    | Allows a player to use the `/back` command.       | ✅       |
| `bettertpa.warp`    | Allows a player to use the warp command.          | ✅       |
| `bettertpa.setwarp` | Allows a player to create new warp locations.     | ❌       |
| `bettertpa.delwarp` | Allows a player to delete warp locations.         | ❌       |

## Contribution

If you are a developer and want to help this plugin grow, please raise a PR implementing your amazing ideas. Once it is
reviewed and tested well, it may get approved and will become a honored contributor on this plugin.

If you are a player and have good ideas around teleportation (TPA), you can raise an issue explain your wonderful ideas.

## License

This plugin is licensed under the MIT License. See the [LICENSE](/LICENSE) file for more information.

## Credits

- Mazidul Islam (Owner)
