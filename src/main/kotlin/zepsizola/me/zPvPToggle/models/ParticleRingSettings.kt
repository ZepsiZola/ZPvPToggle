package zepsizola.me.zPvPToggle.models

import org.bukkit.Color
import org.bukkit.Particle

/**
 * Represents the settings for a particle ring indicator
 */
data class ParticleRingSettings(
    val id: String,
    val type: Particle,
    val points: Int,
    val radius: Double,
    val yOffset: Double,
    val randomOffsetVert: Double,
    val randomOffsetHoriz: Double,
    val interval: Long,
    val randomParticlePositions: Boolean,
    val speed: Double = 0.0,
    val extra: Any? = null
) {

    /**
     * Gets the appropriate data object for this particle type
     * based on the particle's data type requirements
     */
    fun getParticleDataType(): Any? {
        if (extra == null) return null
        return type.getDataType()
        
    }

    companion object {
        // Default values for particle ring settings
        const val DEFAULT_ID = "default"
        const val DEFAULT_TYPE_NAME = "DUST"
        const val DEFAULT_TYPE = "DUST"
        const val DEFAULT_COLOR = "RED"
        const val DEFAULT_DUST_SIZE = 1.0
        const val DEFAULT_RADIUS = 0.7
        const val DEFAULT_POINTS = 32
        const val DEFAULT_Y_OFFSET = 0.1
        const val DEFAULT_RANDOM_OFFSET_VERT = 0.0
        const val DEFAULT_RANDOM_OFFSET_HORIZ = 0.0
        const val DEFAULT_SPEED = 0.0
        const val DEFAULT_INTERVAL = 5
        const val DEFAULT_RANDOM_PARTICLE_POSITIONS = false
        // const val DEFAULT_EXTRA = Particle.DustOptions(Color.RED, 1.0.toFloat())
        
        // Create a default ring with the given ID
        fun createDefault(id: String): ParticleRingSettings {
            return ParticleRingSettings(
                id = DEFAULT_ID,
                type = Particle.valueOf(DEFAULT_TYPE),
                points = DEFAULT_POINTS,
                radius = DEFAULT_RADIUS,
                yOffset = DEFAULT_Y_OFFSET,
                extra = Particle.DustOptions(Color.RED, DEFAULT_DUST_SIZE.toFloat()),
                randomOffsetVert = DEFAULT_RANDOM_OFFSET_VERT,
                randomOffsetHoriz = DEFAULT_RANDOM_OFFSET_HORIZ,
                speed = DEFAULT_SPEED,
                interval = DEFAULT_INTERVAL.toLong(),
                randomParticlePositions = DEFAULT_RANDOM_PARTICLE_POSITIONS
            )
        }
    }
}


