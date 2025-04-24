package zepsizola.me.zPvPToggle

import org.bukkit.plugin.java.JavaPlugin
import zepsizola.me.zPvPToggle.commands.PvpCommand
import zepsizola.me.zPvPToggle.data.DatabaseManagerImpl
import zepsizola.me.zPvPToggle.listeners.PvpListener
import zepsizola.me.zPvPToggle.managers.MessageManager
import zepsizola.me.zPvPToggle.managers.PvpManager
import zepsizola.me.zPvPToggle.indicator.ParticleIndicatorTask
import org.bstats.bukkit.Metrics
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import zepsizola.me.zPvPToggle.data.DatabaseManager

class ZPvPToggle : JavaPlugin() {

    lateinit var pvpManager: PvpManager
    lateinit var messageManager: MessageManager
    // lateinit var databaseManager: DatabaseManagerImpl
    var disablePvpOnDeath = true
    var warningMessageEnabled = true
    var protectPets = true
    var pvpCooldown = 5.0

    override fun onEnable() {
        // Save default config and messages.yml if they don't exist
        saveDefaultConfig()
        saveResource("messages.yml", false)
        
        // Add default database configuration if it doesn't exist
        if (!config.contains("database")) {
            config.set("database.use-mariadb", false)
            config.set("database.mariadb.host", "localhost")
            config.set("database.mariadb.port", 3306)
            config.set("database.mariadb.database", "minecraft")
            config.set("database.mariadb.username", "root")
            config.set("database.mariadb.password", "")
            saveConfig()
        }

        // Initialize managers
        messageManager = MessageManager(this)
        pvpManager = PvpManager(this)
        // databaseManager = DatabaseManagerImpl(this)

        // Register command executor and tab completer
        val pvpCommand = PvpCommand(this)
        getCommand("pvp")?.setExecutor(pvpCommand)
        getCommand("pvp")?.tabCompleter = pvpCommand

        // Register event listeners
        server.pluginManager.registerEvents(PvpListener(this), this)
        
        // // Register database-related listeners
        // server.pluginManager.registerEvents(object : Listener {
        //     @EventHandler
        //     fun onPlayerJoin(event: PlayerJoinEvent) {
        //         // Load player data from database when they join
        //         databaseManager.loadPlayerData(event.player)
        //     }
        // }, this)

        // Start the particle indicator task
        ParticleIndicatorTask.start(this)

        setupBStats()
        logger.info("ZPvPToggle has been enabled!")
    }

    override fun onDisable() {
        // Stop the particle indicator task
        ParticleIndicatorTask.stop()
        pvpManager.stop()

        // Unregister event listeners
        // server.pluginManager.unregisterEvents(PvpListener(this), this)
        
        logger.info("ZPvPToggle has been disabled!")
    }
    
    fun reloadPlugin() {
        // Reload config files
        reloadConfig()
        disablePvpOnDeath = config.getBoolean("disable-pvp-on-death", true)
        warningMessageEnabled = config.getBoolean("warning-message-enabled", true)
        protectPets = config.getBoolean("protect-pets", true)
        pvpCooldown = config.getDouble("pvp-cooldown", 5.0)
        
        // Reload messages
        messageManager.reload()
        
        // Restart particle indicator task with new settings
        ParticleIndicatorTask.stop()
        ParticleIndicatorTask.start(this)
        
        logger.info("ZPvPToggle configuration has been reloaded!")
    }

    private fun setupBStats() {
        // Initialize bStats
        val pluginId = 25463
        val metrics = Metrics(this, pluginId)
    }
}
