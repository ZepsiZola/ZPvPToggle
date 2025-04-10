package zepsizola.me.zPvPToggle.managers

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.configuration.file.YamlConfiguration
import zepsizola.me.zPvPToggle.ZPvPToggle
import java.io.File

class MessageManager(private val plugin: ZPvPToggle) {

    private var messageConfig: YamlConfiguration
    private val miniMessage = MiniMessage.miniMessage()
    private var prefix: String = ""

    init {
        val messagesFile = File(plugin.dataFolder, "messages.yml")
        messageConfig = YamlConfiguration.loadConfiguration(messagesFile)
        loadPrefix()
    }
    
    fun reload() {
        val messagesFile = File(plugin.dataFolder, "messages.yml")
        messageConfig = YamlConfiguration.loadConfiguration(messagesFile)
        loadPrefix()
    }
    
    private fun loadPrefix() {
        prefix = messageConfig.getString("prefix", "<red>ZPvPToggle <dark_red>»</dark_red>") ?: "<red>ZPvPToggle <dark_red>»</dark_red>"
    }

    fun getMessage(key: String): Component {
        var raw = messageConfig.getString(key, "") ?: ""
        // Replace prefix placeholder
        raw = raw.replace("%prefix%", prefix)
        return miniMessage.deserialize(raw)
    }

    fun getMessage(key: String, placeholders: Map<String, String>): Component {
        var raw = messageConfig.getString(key, "") ?: ""
        // Replace prefix placeholder first
        raw = raw.replace("%prefix%", prefix)
        // Then replace other placeholders
        placeholders.forEach { (k, v) ->
            raw = raw.replace(k, v)
        }
        return miniMessage.deserialize(raw)
    }
}
