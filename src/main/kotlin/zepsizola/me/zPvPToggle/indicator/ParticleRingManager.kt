package zepsizola.me.zPvPToggle.indicator

import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Material
import org.bukkit.Location
import org.bukkit.Vibration
import kotlin.math.cos
import kotlin.math.sin
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.configuration.ConfigurationSection
import zepsizola.me.zPvPToggle.ZPvPToggle
import java.util.concurrent.ConcurrentHashMap
import kotlin.Double

/**
 * Manages the collection of particle ring settings
 */
class ParticleRingManager(private val plugin: ZPvPToggle) {
    
    // Map of ring ID to ring settings
    private val rings = ConcurrentHashMap<String, ParticleRingSettings>()
    
    // Default ring ID
    private var defaultRingId: String = "default"
    
    /**
     * Load all particle ring settings from the config
     */
    fun loadRings() {
        rings.clear()
        
        val section = plugin.config.getConfigurationSection("particle-indicator.indicators")
        if (section == null) {
            // If no indicators section exists, create a default ring
            rings["default"] = ParticleRingSettings.createDefault("default")
            return
        }
        
        // Load each ring from the config
        for (key in section.getKeys(false)) {
            val ringSection = section.getConfigurationSection(key)
            if (ringSection != null) {
                val ring = loadRingFromConfig(key, ringSection)
                rings[key] = ring
            }
        }
        
        // If no rings were loaded, create a default one
        if (rings.isEmpty()) {
            rings["default"] = ParticleRingSettings.createDefault("default")
        }
        
        // Set the default ring ID from config or use "default" if not specified
        defaultRingId = plugin.config.getString("particle-indicator.default-indicator", "default") ?: "default"
        
        // Ensure the default ring exists
        if (!rings.containsKey(defaultRingId)) {
            defaultRingId = rings.keys.first()
        }
    }
    
    /**
     * Load a single ring from a configuration section
     */
    private fun loadRingFromConfig(id: String, section: ConfigurationSection): ParticleRingSettings {
        // Get particle type
        val typeName = section.getString("type") ?: ParticleRingSettings.DEFAULT_TYPE_NAME
        
        // Parse particle type, fallback to REDSTONE if invalid
        val particleType = try {
            Particle.valueOf(typeName)
        } catch (_: IllegalArgumentException) {
            plugin.logger.warning("Invalid particle type: $typeName, using REDSTONE instead")
            Particle.DUST
        }

        // Create the ring settings
        return ParticleRingSettings(
            id = id,
            type = particleType,
            points = section.getInt("points", ParticleRingSettings.DEFAULT_POINTS),
            radius = section.getDouble("radius", ParticleRingSettings.DEFAULT_RADIUS),
            interval = section.getInt("interval", ParticleRingSettings.DEFAULT_INTERVAL).toLong(),
            randomParticlePositions = section.getBoolean("random-particle-positions", ParticleRingSettings.DEFAULT_RANDOM_PARTICLE_POSITIONS),
            yOffset = section.getDouble("y-offset", ParticleRingSettings.DEFAULT_Y_OFFSET),
            randomOffsetVert = section.getDouble("random-offset-vertical", ParticleRingSettings.DEFAULT_RANDOM_OFFSET_VERT),
            randomOffsetHoriz = section.getDouble("random-offset-horizontal", ParticleRingSettings.DEFAULT_RANDOM_OFFSET_HORIZ),
            speed = section.getDouble("speed", 0.0),
            extra = getExtra(particleType, section)
        )
    }

    fun getExtra(particleType: Particle, section: ConfigurationSection): Any? {
        return when (particleType) {
            Particle.DUST -> {
                val color = colorFromString(section.getString("color", "RED") ?: "RED")
                val dustSize = section.getDouble("dust-size", 1.0).toFloat()
                Particle.DustOptions(color, dustSize)
            }
            Particle.DUST_COLOR_TRANSITION -> {
                val fromColor = colorFromString(section.getString("from-color", "RED") ?: "RED")
                val toColor = colorFromString(section.getString("to-color", "BLUE") ?: "BLUE")
                Particle.DustTransition(fromColor, toColor, section.getDouble("dust-size", 1.0).toFloat())
            }
            Particle.BLOCK,
            // Particle.BLOCK_CRUMBLE,
            Particle.BLOCK_MARKER,
            Particle.DUST_PILLAR,
            Particle.FALLING_DUST -> {
                val blockData = section.getString("block-data", "STONE") ?: "STONE"
                val blockType = Material.getMaterial(blockData) ?: Material.STONE
                blockType.createBlockData()
            } 
            Particle.ITEM -> {
                val itemString = section.getString("item-data", "EMERALD") ?: "EMERALD"
                val itemStack = ItemStack(Material.getMaterial(itemString) ?: Material.EMERALD)
                itemStack
            }
            // Color as DataType
            Particle.ENTITY_EFFECT -> {
                val colorName = section.getString("color", "RED")?.uppercase() ?: "RED"
                val color = colorFromString(colorName)
                color
            }
            Particle.SCULK_CHARGE -> {
                val sculkCharge = section.getDouble("sculk-charge", 1.0).toFloat()
                sculkCharge
            }
            Particle.SHRIEK -> {
                val sculkShriek = section.getInt("sculk-shriek", 1)
                sculkShriek
            }
            // Particle.TRAIL -> {
            //     val trail = section.getString("trail", "NONE")?.uppercase() ?: "NONE"
            //     val trailType = Particle.valueOf(trail)
            //     if (trailType != null) {
            //         trailType
            //     } else {
            //         null
            //     }
            // }
            Particle.VIBRATION -> {
                val arrivalTime = section.getInt("arrival-time", 20)
                val destination = Vibration.Destination.BlockDestination(section.getLocation("destination", Location(null, 0.0, 0.0, 0.0)) ?: Location(null, 0.0, 0.0, 0.0))
                Vibration(destination, arrivalTime)
            }
            else -> null
        }
    }
    
    /**
     * Get a ring by its ID, or the default ring if not found
     */
    fun getRing(id: String): ParticleRingSettings {
        return rings[id] ?: rings[defaultRingId] ?: ParticleRingSettings.createDefault("default")
    }
    
    /**
     * Get the default ring
     */
    fun getDefaultRing(): ParticleRingSettings {
        return rings[defaultRingId] ?: ParticleRingSettings.createDefault("default")
    }
    
    /**
     * Get all available rings
     */
    fun getAllRings(): Collection<ParticleRingSettings> {
        return rings.values
    }
    

    // fun showParticleRing(player: Player, observer: Player, ring: ParticleRingSettings) {
    //     var curr = 0
    //     observer.scheduler.runAtFixedRate(plugin, Consumer { _: ScheduledTask ->
    //         if (curr >= 20) return@Consumer // Stop if count exceeds 20
    //         curr++
    //         val location = player.location
    //
    //         val center = location.clone().add(0.0, ring.yOffset, 0.0)
    //         // Generate a ring with "points" around the player
    //         for (i in 0 until points) {
    //             val angle = if (!ring.randomParticlePositions) 2.0 * Math.PI * i / ring.points else Random.nextDouble(0.0, 2 * Math.PI)
    //             val x = ring.radius * cos(angle)
    //             val z = ring.radius * sin(angle)
    //             val particleLoc = center.clone().add(x, 0.0, z)
    //
    //             observer.spawnParticle(
    //                 ring.type,
    //                 particleLoc,
    //                 1,
    //                 ring.randomOffsetHoriz,
    //                 ring.randomOffsetVert,
    //                 ring.randomOffsetHoriz,
    //                 ring.speed,
    //                 ring.extra as? Any
    //             )
    //         }
    //     }, null, ring.interval, ring.interval)
    // }

    /**
     * Convert a color name from config to a Bukkit Color.
     */
    fun colorFromString(name: String): Color {
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
            else -> {
                // Create a color if the "name" is a hex value
                if (name.startsWith("#") && name.length == 7) {
                    val r = Integer.parseInt(name.substring(1, 3), 16)
                    val g = Integer.parseInt(name.substring(3, 5), 16)
                    val b = Integer.parseInt(name.substring(5, 7), 16)
                    return Color.fromRGB(r, g, b)
                } else {
                    // Default to RED if the color name is not recognized
                    Color.GRAY
                }
            }
        }
    }
}

