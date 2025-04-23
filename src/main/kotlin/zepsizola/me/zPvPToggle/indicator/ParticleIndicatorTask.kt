package zepsizola.me.zPvPToggle.indicator

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Location
import org.bukkit.entity.Player
import zepsizola.me.zPvPToggle.ZPvPToggle
import java.util.function.Consumer
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import java.util.concurrent.TimeUnit.MILLISECONDS
import zepsizola.me.zPvPToggle.indicator.ParticleRingManager
import zepsizola.me.zPvPToggle.indicator.ParticleRingSettings

object ParticleIndicatorTask {

    private var task: ScheduledTask? = null
    private lateinit var ringManager: ParticleRingManager
    private var indicatorCheckInterval: Long = 1000L
    private var indicatorCheckIntervalTicks: Long = 20L


    fun start(plugin: ZPvPToggle) {
        // Cancel any existing task to prevent duplicates
        stop()
        
        // Initialize the ring manager
        ringManager = ParticleRingManager(plugin)
        ringManager.loadRings()
        
        // Initialize the PvP enabled players cache in PvpManager
        plugin.pvpManager.initializePvpEnabledCache()
        
        // Read interval from config.yml; default to 5 ticks
        indicatorCheckIntervalTicks = plugin.config.getLong("particle-indicator.interval-ticks", 5L)
        indicatorCheckInterval = indicatorCheckIntervalTicks * 50L
        
        // Get the max view distance from config and ensure it's between 0 and 64.
        val maxDistance = plugin.config.getDouble("particle-indicator.max-view-distance", 32.0).coerceIn(0.0, 64.0)
        
        // Schedule the global task to run at a fixed rate
        task = plugin.server.asyncScheduler.runAtFixedRate(plugin, Consumer { _: ScheduledTask ->
            // Get all players with PvP enabled from the PvpManager
            val pvpEnabledPlayers = plugin.pvpManager.getPvpEnabledPlayers()
            
            // For each player with PvP enabled
            playerLoop@ for (pvpPlayer in pvpEnabledPlayers) {
                // Skip if player is no longer valid or online
                if (!pvpPlayer.isValid || !pvpPlayer.isOnline) continue
                
                // Skip if player is in spectator mode
                if (pvpPlayer.gameMode == org.bukkit.GameMode.SPECTATOR) continue
                
                // Check for vanish status using common metadata keys used by vanish plugins
                val vanishKeys = listOf("vanished", "isVanished", "vanish")
                for (key in vanishKeys) {
                    val isVanished = pvpPlayer.getMetadata(key).firstOrNull()?.asBoolean() ?: false
                    if (isVanished) continue@playerLoop // Skip if player is vanished
                }
                
                val location = pvpPlayer.location
                val playerState = plugin.pvpManager.getState(pvpPlayer)
                val ring = ringManager.getRing(playerState.indicatorRingId)
                plugin.server.regionScheduler.run(plugin, location, Consumer {_: ScheduledTask ->
                    // For each observer who can see indicators
                    for (observer in location.getNearbyPlayers(maxDistance)) {
                        val observerState = plugin.pvpManager.getState(observer)
                        // Skip if observer can't see indicators
                        if (!observerState.canSeeIndicators || (observer == pvpPlayer && !observerState.canSeeOwnIndicator)) continue
                        // if (observer == pvpPlayer && !playerState.canSeeOwnIndicator) continue // Skip if observer is the player themselves
                        
                        // Schedule an immediate task for the observer to see the ring
                        // var curr = 0
                        // observer.scheduler.runAtFixedRate(plugin, Consumer { _: ScheduledTask ->
                        //     if (curr >= 20) return@Consumer // Stop if count exceeds 20
                        //     curr++
                        showParticleRing(pvpPlayer, observer, ring, plugin)
                        // }, null, 1L, 1L)
                    }
                })
            }
        }, indicatorCheckInterval, indicatorCheckInterval, MILLISECONDS)
    }

    fun stop() {
        task?.cancel()
        task = null
    }


    fun showParticleRing(player: Player, observer: Player, ring: ParticleRingSettings, plugin: ZPvPToggle) {
        var curr = 0
        val end = Math.round(indicatorCheckIntervalTicks.toDouble() / ring.interval.toInt()).toInt()
        observer.scheduler.runAtFixedRate(plugin, Consumer { _: ScheduledTask ->
            if (curr >= end) return@Consumer // Stop if count exceeds 20
            curr++
            val location = player.location

            val center = location.clone().add(0.0, ring.yOffset, 0.0)
            // Generate a ring with "points" around the player
            for (i in 0 until ring.points) {
                val angle = if (!ring.randomParticlePositions) 2.0 * Math.PI * i / ring.points else Random.nextDouble(0.0, 2 * Math.PI)
                val x = ring.radius * cos(angle)
                val z = ring.radius * sin(angle)
                val particleLoc = center.clone().add(x, 0.0, z)

                observer.spawnParticle(
                    ring.type,
                    particleLoc,
                    1,
                    ring.randomOffsetHoriz,
                    ring.randomOffsetVert,
                    ring.randomOffsetHoriz,
                    ring.speed,
                    ring.extra as? Any
                )
            }
        }, null, ring.interval, ring.interval)
    }

    // private fun reloadParticleRing(plugin: ZPvPToggle) {
    //     val section = plugin.config.getConfigurationSection("particle-indicator") ?: return
    //
    //     // Safely get config values, fallback to defaults
    //     typeName = section.getString("type", "REDSTONE")?.uppercase() ?: "REDSTONE"
    //     radius = section.getDouble("radius", 0.7)
    //     points = section.getInt("points", 32)
    //     yOffset = section.getDouble("y-offset", 0.1)
    //
    //     particleType = try {
    //         Particle.valueOf(typeName)
    //     } catch (_: IllegalArgumentException) {
    //         Particle.valueOf("REDSTONE")
    //     }
    //
    //     // If using REDSTONE, parse color and size from config
    //     // dustOptions = if (particleType == Particle.valueOf("REDSTONE")) {
    //     val colorName = section.getString("color", "RED")?.uppercase() ?: "RED"
    //     val dustSize = section.getDouble("dust-size", 1.0)
    //     val bukkitColor = colorFromString(colorName) ?: Color.RED
    //     dustOptions = Particle.DustOptions(bukkitColor, dustSize.toFloat())
    //     // } else null
    // }

    // private fun showParticleRing(player: Player, observer: Player, ring: ParticleRingSettings) {
    //     val location = player.location
    //
    //     val center = location.clone().add(0.0, ring.yOffset, 0.0)
    //     // Generate a ring with "points" around the player
    //     for (i in 0 until ring.points) {
    //         val angle = 2.0 * Math.PI * i / ring.points
    //         val x = ring.radius * cos(angle)
    //         val z = ring.radius * sin(angle)
    //         val particleLoc = center.clone().add(x, 0.0, z)
    //
    //         if (ring.extra != null) {
    //             // Spawn with data - show only to the observer
    //             observer.spawnParticle(ring.type, particleLoc, 1, ring.extra)
    //         } else {
    //             // Simple spawn for other particle types - show only to the observer
    //             observer.spawnParticle(ring.type, particleLoc, 1, 0.0, 0.0, 0.0, 0.0)
    //         }
    //     }
    // }
}
