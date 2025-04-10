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

        // Read interval from config.yml; default to 20 ticks (1 second)
        val interval = plugin.config.getLong("particle-indicator.interval-ticks", 20L)

        // Schedule the global task to run at a fixed rate
        task = plugin.server.globalRegionScheduler.runAtFixedRate(
            plugin,
            Consumer { _: ScheduledTask ->
                // Iterate over all online players
                for (player in plugin.server.onlinePlayers) {
                    val state = plugin.pvpManager.getState(player)
                    if (state.pvpEnabled && state.indicatorsEnabled) {
                        // Schedule an immediate task for the player to show the ring
                        player.scheduler.run(
                            plugin,
                            Consumer { _: ScheduledTask ->
                                showRedRing(player, plugin)
                            },
                            null
                        )
                    }
                }
            },
            0L,        // initial delay
            interval    // period
        )
    }

    fun stop() {
        task?.cancel()
        task = null
    }

    private fun showRedRing(player: Player, plugin: ZPvPToggle) {
        val section = plugin.config.getConfigurationSection("particle-indicator") ?: return

        // Safely get config values, fallback to defaults
        val typeName = section.getString("type", "REDSTONE")?.uppercase() ?: "REDSTONE"
        val radius = section.getDouble("radius", 0.7)
        val points = section.getInt("points", 12)
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
                // Redstone-based color
                world.spawnParticle(particleType, particleLoc, 1, dustOptions)
            } else {
                // Simple spawn for other particle types
                world.spawnParticle(particleType, particleLoc, 1, 0.0, 0.0, 0.0, 0.0)
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