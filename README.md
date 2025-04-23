# ZPvPToggle

A lightweight Minecraft plugin that allows players to toggle their PvP status on and off. Players can only engage in PvP when both parties have PvP enabled.

![ZPvPToggle](https://img.shields.io/badge/Minecraft-PvP%20Toggle-red)
![API Version](https://img.shields.io/badge/API-1.20-blue)
![Folia Support](https://img.shields.io/badge/Folia-Supported-green)

## Features

- **Toggle PvP**: Players can enable or disable their PvP status
- **Visual Indicators**: Customizable particle rings show which players have PvP enabled
- **Custom Indicators:** Players can choose their own indicators. Access to indicators is permission-based.
- **Admin Controls**: Staff can toggle PvP for other players
- **Fully Customizable**: All messages and particle effects can be configured
- **Folia Support**: Works with Folia server software
- **Permissions-Based**: Granular permission system for different commands

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/pvp` | Toggle your PvP status | zpvptoggle.user |
| `/pvp toggle` | Toggle your PvP status | zpvptoggle.user |
| `/pvp on` | Enable your PvP status | zpvptoggle.user |
| `/pvp off` | Disable your PvP status | zpvptoggle.user |
| `/pvp hide` | Hide PvP particle indicators | zpvptoggle.user |
| `/pvp show` | Show PvP particle indicators | zpvptoggle.user |
| `/pvp indicator <indicator>` | Change your PvP indicator | zpvptoggle.indicator.<indicator> |
| `/pvp help` | Display the help message | zpvptoggle.user |
| `/pvp reload` | Reload plugin configuration | zpvptoggle.admin |
| `/pvp <player>` | Toggle PvP for another player | zpvptoggle.admin |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| zpvptoggle.user | Allows players to toggle their own PvP status | true |
| zpvptoggle.indicator.<indicator> | Give access to specific indicators. | multiple |
| zpvptoggle.admin | Allows staff to toggle PvP status for other players | op |

## Configuration

### config.yml
```yaml
# Whether to disable PvP when a player dies
disable-pvp-on-death: true
# Whether to show a warning message when a player first toggles PvP
warning-message-enabled: true

particle-indicator:
  # Interval in ticks to update PvP indicator status (different from interval in per-indicator settings)
  interval-ticks: 20
  # Maximum distance (in blocks) at which players can see PvP indicators.
  max-view-distance: 32
  # The default indicator to use (must match one of the keys in the indicators section)
  default-indicator: red-ring
  # Define multiple particle ring indicators
  indicators:
    # Default ring - red circle around feet
    red-ring: 
      type: REDSTONE
      points: 32
      random-particle-positions: false
      radius: 0.8
      y-offset: 0.1
      interval: 5
      random-offset-vertical: 0.0
      random-offset-horizontal: 0.0
      speed: 0.01
      color: RED
      dust-size: 1.0
```

## Compatibility

- Paper 1.20.1 -> 1.21.4
- Folia-compatible
- Java 17+

## Dependencies

- [MCKotlin](https://modrinth.com/plugin/mckotlin)

## Installation

1. Download the latest version of ZPvPToggle from [Modrinth](https://modrinth.com/plugin/zpvptoggle/versions).
2. Download MCKotlin from [Modrinth](https://modrinth.com/plugin/mckotlin)
3. Place the downloaded `.jar` files into your server's `plugins/` folder.
4. Restart your server.
5. Configure the plugin to your liking by editing the `.yml` files in the `plugins/ZPvPtoggle/` folder.

