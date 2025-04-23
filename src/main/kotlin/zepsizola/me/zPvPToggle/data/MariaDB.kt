package zepsizola.me.zPvPToggle.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
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
 * MariaDB/MySQL implementation of the DatabaseManager interface using HikariCP
 */
class MariaDB(
    private val plugin: ZPvPToggle,
    private val host: String,
    private val port: Int,
    private val database: String,
    private val username: String,
    private val password: String
) : DatabaseManager {
    private var dataSource: HikariDataSource? = null
    private val jdbcUrl = "jdbc:mysql://$host:$port/$database"
    
    override fun initialize() {
        try {
            // Try to load MySQL JDBC driver
            try {
                // Try the newer driver class first
                Class.forName("com.mysql.cj.jdbc.Driver")
            } catch (e: ClassNotFoundException) {
                // Fall back to older driver class
                Class.forName("com.mysql.jdbc.Driver")
            }
            
            // Configure HikariCP
            val config = HikariConfig()
            config.jdbcUrl = jdbcUrl
            config.username = username
            config.password = password
            config.maximumPoolSize = 10
            config.minimumIdle = 2
            config.idleTimeout = 30000 // 30 seconds
            config.maxLifetime = 1800000 // 30 minutes
            config.connectionTimeout = 10000 // 10 seconds
            config.poolName = "ZPvPToggle-HikariCP"
            
            // Add MySQL specific properties
            config.addDataSourceProperty("cachePrepStmts", "true")
            config.addDataSourceProperty("prepStmtCacheSize", "250")
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            config.addDataSourceProperty("useServerPrepStmts", "true")
            config.addDataSourceProperty("useLocalSessionState", "true")
            config.addDataSourceProperty("rewriteBatchedStatements", "true")
            config.addDataSourceProperty("cacheResultSetMetadata", "true")
            config.addDataSourceProperty("cacheServerConfiguration", "true")
            config.addDataSourceProperty("elideSetAutoCommits", "true")
            config.addDataSourceProperty("maintainTimeStats", "false")
            
            // Create the data source
            dataSource = HikariDataSource(config)
            
            // Create tables if they don't exist
            getConnection()?.use { conn ->
                createTables(conn)
            }
            
            plugin.logger.info("MariaDB database connection pool established with HikariCP.")
        } catch (e: ClassNotFoundException) {
            plugin.logger.log(Level.SEVERE, "MySQL JDBC driver not found. Please ensure the MySQL connector is installed on your server.", e)
            throw e
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Failed to initialize MariaDB database with HikariCP", e)
            throw e
        }
    }
    
    override fun close() {
        try {
            dataSource?.close()
            plugin.logger.info("MariaDB connection pool closed.")
        } catch (e: SQLException) {
            plugin.logger.log(Level.WARNING, "Error closing MariaDB connection pool", e)
        }
    }
    
    /**
     * Get a connection from the pool
     */
    private fun getConnection(): Connection? {
        return dataSource?.connection
    }
    
    override fun loadPlayerData(uuid: UUID): Map<String, Any> {
        val data = mutableMapOf<String, Any>()
        val sql = "SELECT indicator_id, can_see_indicators, can_see_own_indicator FROM player_preferences WHERE uuid = ?"
        
        try {
            getConnection()?.prepareStatement(sql)?.use { stmt ->
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
            getConnection()?.prepareStatement(sql)?.use { stmt ->
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
            getConnection()?.prepareStatement(sql)?.use { stmt ->
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
            getConnection()?.prepareStatement(sql)?.use { stmt ->
                stmt.setString(1, uuid.toString())
                stmt.setBoolean(2, canSeeOwnIndicator)
                stmt.setBoolean(3, canSeeOwnIndicator)
                stmt.executeUpdate()
            }
        } catch (e: SQLException) {
            plugin.logger.log(Level.WARNING, "Error saving player own indicator visibility for $uuid", e)
        }
    }
    
    private fun createTables(connection: Connection) {
        val sql = """
            CREATE TABLE IF NOT EXISTS player_preferences (
                uuid VARCHAR(36) PRIMARY KEY,
                indicator_id VARCHAR(50) DEFAULT 'default',
                can_see_indicators BOOLEAN DEFAULT TRUE,
                can_see_own_indicator BOOLEAN DEFAULT TRUE
            )
        """
        
        try {
            connection.createStatement().use { stmt ->
                stmt.execute(sql)
            }
        } catch (e: SQLException) {
            plugin.logger.log(Level.SEVERE, "Error creating tables", e)
        }
    }
}

