package zepsizola.me.zPvPToggle

import org.bukkit.plugin.java.JavaPlugin
import zepsizola.me.zPvPToggle.commands.PvpCommand
import zepsizola.me.zPvPToggle.listeners.PvpListener
import zepsizola.me.zPvPToggle.managers.MessageManager
import zepsizola.me.zPvPToggle.managers.PvpManager
import zepsizola.me.zPvPToggle.tasks.ParticleIndicatorTask

class ZPvPToggle : JavaPlugin() {

    lateinit var pvpManager: PvpManager
    lateinit var messageManager: MessageManager

    override fun onEnable() {
        // Save default config and messages.yml if they don't exist
        saveDefaultConfig()
        saveResource("messages.yml", false)

        // Initialize managers
        messageManager = MessageManager(this)
        pvpManager = PvpManager(this)

        // Register command executor
        getCommand("pvp")?.setExecutor(PvpCommand(this))

        // Register event listeners
        server.pluginManager.registerEvents(PvpListener(this), this)

        // Start the particle indicator task
        ParticleIndicatorTask.start(this)

        logger.info("ZPvPToggle has been enabled!")
    }

    override fun onDisable() {
        // Stop the particle indicator task
        ParticleIndicatorTask.stop()

        logger.info("ZPvPToggle has been disabled!")
    }
}