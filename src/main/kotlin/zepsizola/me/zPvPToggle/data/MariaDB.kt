package zepsizola.me.zPvPToggle.data

import org.bukkit.plugin.java.JavaPlugin
import zepsizola.me.zPvPToggle.ZPvPToggle
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.UUID
import java.util.logging.Level

/**
 * MariaDB/MySQL implementation of the DatabaseManager interface
 */
class MariaDB(
    private val plugin: ZPvPToggle,
    private val host: String,
    private val port: Int,
    private val database: String,
    private val username: String,
    private val password: String
) : DatabaseManager {
    private var connection: Connection? = null
    private val url = "jdbc:mysql://$host:$port/$database"
    
    override fun initialize() {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.jdbc.Driver")
            
            // Create connection
            connection = DriverManager.getConnection(url, username, password)
            
            // Create tables if they don't exist
            createTables()
            
            plugin.logger.info("MariaDB database connection established.")
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Failed to initialize MariaDB database", e)
            throw e
        }
    }
    
    override fun close() {
        try {
            connection?.close()
        } catch (e: SQLException) {
            plugin.logger.log(Level.WARNING, "Error closing MariaDB connection", e)
        }
    }
    
    override fun loadPlayerData(uuid: UUID): Map<String, Any> {
        val data = mutableMapOf<String, Any>()
        val sql = "SELECT indicator_id, can_see_indicators, can_see_own_indicator FROM player_preferences WHERE uuid = ?"
        
        try {
            connection?.prepareStatement(sql)?.use { stmt ->
                stmt.setString(1, uuid.toString())
                val rs = stmt.executeQuery()
                
                if (rs.next()) {
                    val indicatorId = rs.getString("indicator_id")
                    val canSeeIndicators = rs.getBoolean("can_see_indicators")
                    val canSeeOwnIndicator = rs.getBoolean("can_see_own_indicator")
                    
                    data["indicator_id"] = indicatorId
                    data["can_see_indicators"] = canSeeIndicators
                    data["can_see_own_indicator"] = canSeeOwnIndicator
                }
            }
        } catch (e: SQLException) {
            plugin.logger.log(Level.WARNING, "Error loading player data for $uuid", e)
        }
        
        return data
    }
    
    override fun savePlayerIndicator(uuid: UUID, indicatorId: String) {
        val sql = """
            INSERT INTO player_preferences (uuid, indicator_id) 
            VALUES (?, ?) 
            ON DUPLICATE KEY UPDATE indicator_id = ?
        """
        
        try {
            connection?.prepareStatement(sql)?.use { stmt ->
                stmt.setString(1, uuid.toString())
                stmt.setString(2, indicatorId)
                stmt.setString(3, indicatorId)
                stmt.executeUpdate()
            }
        } catch (e: SQLException) {
            plugin.logger.log(Level.WARNING, "Error saving player indicator for $uuid", e)
        }
    }
    
    override fun savePlayerIndicatorVisibility(uuid: UUID, canSeeIndicators: Boolean) {
        val sql = """
            INSERT INTO player_preferences (uuid, can_see_indicators) 
            VALUES (?, ?) 
            ON DUPLICATE KEY UPDATE can_see_indicators = ?
        """
        
        try {
            connection?.prepareStatement(sql)?.use { stmt ->
                stmt.setString(1, uuid.toString())
                stmt.setBoolean(2, canSeeIndicators)
                stmt.setBoolean(3, canSeeIndicators)
                stmt.executeUpdate()
            }
        } catch (e: SQLException) {
            plugin.logger.log(Level.WARNING, "Error saving player indicator visibility for $uuid", e)
        }
    }
    
    override fun savePlayerOwnIndicatorVisibility(uuid: UUID, canSeeOwnIndicator: Boolean) {
        val sql = """
            INSERT INTO player_preferences (uuid, can_see_own_indicator) 
            VALUES (?, ?) 
            ON DUPLICATE KEY UPDATE can_see_own_indicator = ?
        """
        
        try {
            connection?.prepareStatement(sql)?.use { stmt ->
                stmt.setString(1, uuid.toString())
                stmt.setBoolean(2, canSeeOwnIndicator)
                stmt.setBoolean(3, canSeeOwnIndicator)
                stmt.executeUpdate()
            }
        } catch (e: SQLException) {
            plugin.logger.log(Level.WARNING, "Error saving player own indicator visibility for $uuid", e)
        }
    }
    
    private fun createTables() {
        val sql = """
            CREATE TABLE IF NOT EXISTS player_preferences (
                uuid VARCHAR(36) PRIMARY KEY,
                indicator_id VARCHAR(50) DEFAULT 'default',
                can_see_indicators BOOLEAN DEFAULT TRUE,
                can_see_own_indicator BOOLEAN DEFAULT TRUE
            )
        """
        
        try {
            connection?.createStatement()?.use { stmt ->
                stmt.execute(sql)
            }
        } catch (e: SQLException) {
            plugin.logger.log(Level.SEVERE, "Error creating tables", e)
        }
    }
}

