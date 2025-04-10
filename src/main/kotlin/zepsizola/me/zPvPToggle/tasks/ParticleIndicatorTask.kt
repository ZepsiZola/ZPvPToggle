package zepsizola.me.zPvPToggle.tasks

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.entity.Player
import zepsizola.me.zPvPToggle.ZPvPToggle
import java.util.function.Consumer
import kotlin.math.cos
import kotlin.math.sin

object ParticleIndicatorTask {

    private var task: ScheduledTask? = null

    fun start(plugin: ZPvPToggle) {
        // Cancel any existing task to prevent duplicates
        stop()

        // Initialize the PvP enabled players cache in PvpManager
        plugin.pvpManager.initializePvpEnabledCache()

        // Read interval from config.yml; default to 5 ticks
        val interval = plugin.config.getLong("particle-indicator.interval-ticks", 5L)
        
        // Get the max view distance from config
        val maxDistance = plugin.config.getDouble("particle-indicator.max-view-distance", 64.0)
        val unlimitedDistance = maxDistance <= 0

        // Schedule the global task to run at a fixed rate
        task = plugin.server.globalRegionScheduler.runAtFixedRate(
            plugin,
            Consumer { _: ScheduledTask ->
                // Get all players with PvP enabled from the PvpManager
                val pvpEnabledPlayers = plugin.pvpManager.getPvpEnabledPlayers()
                
                // Only process if there are players with PvP enabled
                if (pvpEnabledPlayers.isNotEmpty()) {
                    
                    // For each player with PvP enabled
                    for (pvpPlayer in pvpEnabledPlayers) {
                        // Skip if player is no longer valid or online
                        if (!pvpPlayer.isValid || !pvpPlayer.isOnline) {
                            continue
                        }
                        
                        // Skip if player is in spectator mode
                        if (pvpPlayer.gameMode == org.bukkit.GameMode.SPECTATOR) {
                            continue
                        }
                        
                        // Check for vanish status using common metadata keys used by vanish plugins
                        // Check various vanish metadata keys and their values
                        val vanishKeys = listOf("vanished", "isVanished", "vanish")
                        var isVanished = false
                        for (key in vanishKeys) {
                            if (pvpPlayer.hasMetadata(key)) {
                                val metaValue = pvpPlayer.getMetadata(key).firstOrNull()?.asBoolean()
                                if (metaValue == true) {
                                    isVanished = true
                                    break
                                }
                            }
                        }
                        if (isVanished) {
                            continue
                        }
                        
                        // // Skip if player has invisibility effect and has permission typically used by vanish plugins
                        // if (pvpPlayer.isInvisible && 
                        //     (pvpPlayer.hasPermission("vanish.use") || 
                        //      pvpPlayer.hasPermission("essentials.vanish") || 
                        //      pvpPlayer.hasPermission("v.vanish"))) {
                        //     continue
                        // }
                        
                        // Get the world of the PvP-enabled player
                        val world = pvpPlayer.world
                        
                        // For each observer who can see indicators
                        for (observer in plugin.server.onlinePlayers) {
                            // Skip if observer can't see indicators
                            if (!plugin.pvpManager.getState(observer).canSeeIndicators) continue
                            
                            // Skip if they're in different worlds
                            if (observer.world != world) continue
                            
                            // Check distance if distance limit is enabled
                            if (!unlimitedDistance && observer != pvpPlayer) {
                                val distance = observer.location.distance(pvpPlayer.location)
                                if (distance > maxDistance) continue
                            }
                            
                            // Schedule an immediate task for the observer to see the ring
                            observer.scheduler.run(
                                plugin,
                                Consumer { _: ScheduledTask ->
                                    showParticleRing(pvpPlayer, observer, plugin)
                                },
                                null
                            )
                        }
                    }
                }
            },
            1L,        // initial delay (must be > 0 for Folia)
            interval    // period
        )
    }

    fun stop() {
        task?.cancel()
        task = null
    }

    private fun showParticleRing(player: Player, observer: Player, plugin: ZPvPToggle) {
        val section = plugin.config.getConfigurationSection("particle-indicator") ?: return

        // Safely get config values, fallback to defaults
        val typeName = section.getString("type", "REDSTONE")?.uppercase() ?: "REDSTONE"
        val radius = section.getDouble("radius", 0.7)
        val points = section.getInt("points", 32)
        val yOffset = section.getDouble("y-offset", 0.1)

        val location = player.location
        val world: World = location.world ?: return

        val center = location.clone().add(0.0, yOffset, 0.0)

        // Attempt to parse the Particle type. If invalid, fallback to REDSTONE.
        val particleType = try {
            Particle.valueOf(typeName)
        } catch (_: IllegalArgumentException) {
            Particle.REDSTONE
        }

        // If using REDSTONE, parse color and size from config
        val dustOptions = if (particleType == Particle.REDSTONE) {
            val colorName = section.getString("color", "RED")?.uppercase() ?: "RED"
            val dustSize = section.getDouble("dust-size", 1.0)
            val bukkitColor = colorFromString(colorName) ?: Color.RED
            Particle.DustOptions(bukkitColor, dustSize.toFloat())
        } else null

        // Generate a ring with "points" around the player
        for (i in 0 until points) {
            val angle = 2.0 * Math.PI * i / points
            val x = radius * cos(angle)
            val z = radius * sin(angle)
            val particleLoc = center.clone().add(x, 0.0, z)
            
            if (dustOptions != null) {
                // Redstone-based color - show only to the observer
                observer.spawnParticle(particleType, particleLoc, 1, dustOptions)
            } else {
                // Simple spawn for other particle types - show only to the observer
                observer.spawnParticle(particleType, particleLoc, 1, 0.0, 0.0, 0.0, 0.0)
            }
        }
    }

    /**
     * Convert a color name from config to a Bukkit Color.
     */
    private fun colorFromString(name: String): Color? {
        return when (name) {
            "WHITE" -> Color.WHITE
            "SILVER" -> Color.SILVER
            "GRAY" -> Color.GRAY
            "BLACK" -> Color.BLACK
            "RED" -> Color.RED
            "MAROON" -> Color.MAROON
            "YELLOW" -> Color.YELLOW
            "OLIVE" -> Color.OLIVE
            "LIME" -> Color.LIME
            "GREEN" -> Color.GREEN
            "AQUA" -> Color.AQUA
            "TEAL" -> Color.TEAL
            "BLUE" -> Color.BLUE
            "NAVY" -> Color.NAVY
            "FUCHSIA" -> Color.FUCHSIA
            "PURPLE" -> Color.PURPLE
            "ORANGE" -> Color.ORANGE
            else -> null
        }
    }
}
