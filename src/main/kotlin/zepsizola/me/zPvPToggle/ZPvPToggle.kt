package zepsizola.me.zPvPToggle

import org.bukkit.plugin.java.JavaPlugin
import zepsizola.me.zPvPToggle.commands.PvpCommand
import zepsizola.me.zPvPToggle.listeners.PvpListener
import zepsizola.me.zPvPToggle.managers.MessageManager
import zepsizola.me.zPvPToggle.managers.PvpManager
import zepsizola.me.zPvPToggle.tasks.ParticleIndicatorTask
import org.bstats.bukkit.Metrics

class ZPvPToggle : JavaPlugin() {

    lateinit var pvpManager: PvpManager
    lateinit var messageManager: MessageManager
    var disablePvpOnDeath = true
    var warningMessageEnabled = true

    override fun onEnable() {
        // Save default config and messages.yml if they don't exist
        saveDefaultConfig()
        saveResource("messages.yml", false)

        // Initialize managers
        messageManager = MessageManager(this)
        pvpManager = PvpManager(this)

        // Register command executor and tab completer
        val pvpCommand = PvpCommand(this)
        getCommand("pvp")?.setExecutor(pvpCommand)
        getCommand("pvp")?.tabCompleter = pvpCommand

        // Register event listeners
        server.pluginManager.registerEvents(PvpListener(this), this)

        // Start the particle indicator task
        ParticleIndicatorTask.start(this)

        setupBStats()
        logger.info("ZPvPToggle has been enabled!")
    }

    override fun onDisable() {
        // Stop the particle indicator task
        ParticleIndicatorTask.stop()

        logger.info("ZPvPToggle has been disabled!")
    }
    
    fun reloadPlugin() {
        // Reload config files
        reloadConfig()
        disablePvpOnDeath = config.getBoolean("disable-pvp-on-death", true)
        warningMessageEnabled = config.getBoolean("warning-message-enabled", true)
        
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
