# ZPvPToggle

A lightweight Minecraft plugin that allows players to toggle their PvP status on and off. Players can only engage in PvP when both parties have PvP enabled.

![ZPvPToggle](https://img.shields.io/badge/Minecraft-PvP%20Toggle-red)
![API Version](https://img.shields.io/badge/API-1.20-blue)
![Folia Support](https://img.shields.io/badge/Folia-Supported-green)

## Features

- **Toggle PvP**: Players can enable or disable their PvP status
- **Visual Indicators**: Customizable particle rings show which players have PvP enabled
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
| `/pvp help` | Display the help message | zpvptoggle.user |
| `/pvp reload` | Reload plugin configuration | zpvptoggle.admin |
| `/pvp <player>` | Toggle PvP for another player | zpvptoggle.admin |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| zpvptoggle.user | Allows players to toggle their own PvP status | true |
| zpvptoggle.admin | Allows staff to toggle PvP status for other players | op |

## Configuration

### config.yml
```yaml
particle-indicator:
  # Interval in ticks to show the ring of particles. 20 ticks = 1 second.
  interval-ticks: 5

  # Particle type for the indicator (e.g., REDSTONE, FLAME, etc.)
  # If you use REDSTONE, color and size settings will be applied.
  type: REDSTONE
  # For a list of possible particles...
  # https://minecraft.wiki/w/Particles_(Java_Edition)

  # For REDSTONE particles, specify the color name.
  # Use standard Bukkit color names (e.g., RED, BLUE, GREEN).
  color: RED

  # The size (thickness) of the dust for REDSTONE particles.
  dust-size: 1.0

  # Number of points around the ring (higher = smoother circle).
  points: 32

  # Radius of the particle ring.
  radius: 0.8

  # Y-offset above the player's feet.
  y-offset: 0.1
  
  # Maximum distance (in blocks) at which players can see PvP indicators.
  max-view-distance: 32
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

