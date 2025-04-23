package zepsizola.me.zPvPToggle.data

import org.bukkit.entity.Player
import zepsizola.me.zPvPToggle.ZPvPToggle
import java.util.UUID
import java.util.logging.Level

/**
 * Interface for database operations to store player preferences
 */
interface DatabaseManager {
    /**
     * Initialize the database connection and create tables if needed
     */
    fun initialize()
    
    /**
     * Close the database connection
     */
    fun close()
    
    /**
     * Load player data from the database
     * @param uuid The UUID of the player
     * @return A map of player preferences
     */
    fun loadPlayerData(uuid: UUID): Map<String, Any>
    
    /**
     * Save player indicator preference
     * @param uuid The UUID of the player
     * @param indicatorId The ID of the indicator
     */
    fun savePlayerIndicator(uuid: UUID, indicatorId: String)
    
    /**
     * Save player indicator visibility preference
     * @param uuid The UUID of the player
     * @param canSeeIndicators Whether the player can see indicators
     */
    fun savePlayerIndicatorVisibility(uuid: UUID, canSeeIndicators: Boolean)
    
    /**
     * Save player own indicator visibility preference
     * @param uuid The UUID of the player
     * @param canSeeIndicators Whether the player can see indicators
     */
    fun savePlayerOwnIndicatorVisibility(uuid: UUID, canSeeOwnIndicator: Boolean)
    
    /**
     * Create a new database manager instance based on configuration
     * @param plugin The plugin instance
     * @return A database manager implementation
     */
    companion object {
        fun create(plugin: ZPvPToggle): DatabaseManager {
            return try {
                val useMariaDB = plugin.config.getBoolean("database.use-mariadb", false)
                if (useMariaDB) {
                    val host = plugin.config.getString("database.mariadb.host", "localhost")
                    val port = plugin.config.getInt("database.mariadb.port", 3306)
                    val database = plugin.config.getString("database.mariadb.database", "minecraft")
                    val username = plugin.config.getString("database.mariadb.username", "root")
                    val password = plugin.config.getString("database.mariadb.password", "")
                    
                    // Create MariaDB connection with HikariCP
                    MariaDB(plugin, host!!, port, database!!, username!!, password!!)
                } else {
                    SQLite(plugin)
                }
            } catch (e: Exception) {
                plugin.logger.log(Level.SEVERE, "Failed to initialize database, falling back to SQLite", e)
                SQLite(plugin)
            }
        }
    }
}

/**
 * Class to manage database operations
 */
class DatabaseManagerImpl(private val plugin: ZPvPToggle) {
    private val dbManager: DatabaseManager = DatabaseManager.create(plugin)
    
    init {
        dbManager.initialize()
    }
    
    /**
     * Load player data from the database and apply it to the player
     * @param player The player to load data for
     */
    fun loadPlayerData(player: Player) {
        try {
            val data = dbManager.loadPlayerData(player.uniqueId)
            
            // Apply indicator preference if exists
            if (data.containsKey("indicator_id")) {
                val indicatorId = data["indicator_id"] as String
                plugin.pvpManager.setIndicatorRing(player, indicatorId)
            }
            
            // Apply indicator visibility preference if exists
            if (data.containsKey("can_see_indicators")) {
                val canSeeIndicators = data["can_see_indicators"] as Boolean
                val state = plugin.pvpManager.getState(player)
                state.canSeeIndicators = canSeeIndicators
            }
            
            // Apply own indicator visibility preference if exists
            if (data.containsKey("can_see_own_indicator")) {
                val canSeeOwnIndicator = data["can_see_own_indicator"] as Boolean
                val state = plugin.pvpManager.getState(player)
                state.canSeeOwnIndicator = canSeeOwnIndicator
            }
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "Failed to load player data for ${player.name}", e)
        }
    }
    
    /**
     * Save player indicator preference
     * @param player The player to save data for
     * @param indicatorId The ID of the indicator
     */
    fun savePlayerIndicator(player: Player, indicatorId: String) {
        try {
            dbManager.savePlayerIndicator(player.uniqueId, indicatorId)
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "Failed to save indicator preference for ${player.name}", e)
        }
    }
    
    /**
     * Save player indicator visibility preference
     * @param player The player to save data for
     * @param canSeeIndicators Whether the player can see indicators
     */
    fun savePlayerIndicatorVisibility(player: Player, canSeeIndicators: Boolean) {
        try {
            dbManager.savePlayerIndicatorVisibility(player.uniqueId, canSeeIndicators)
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "Failed to save indicator visibility for ${player.name}", e)
        }
    }
    
    /**
     * Save player indicator visibility preference
     * @param player The player to save data for
     * @param canSeeIndicators Whether the player can see indicators
     */
    fun savePlayerOwnIndicatorVisibility(player: Player, canSeeOwnIndicator: Boolean) {
        try {
            dbManager.savePlayerOwnIndicatorVisibility(player.uniqueId, canSeeOwnIndicator)
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "Failed to save own indicator visibility for ${player.name}", e)
        }
    }
    
    /**
     * Close the database connection
     */
    fun close() {
        dbManager.close()
    }
}

