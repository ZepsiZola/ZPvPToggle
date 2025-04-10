package zepsizola.me.zPvPToggle.managers

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.configuration.file.YamlConfiguration
import zepsizola.me.zPvPToggle.ZPvPToggle
import java.io.File

class MessageManager(private val plugin: ZPvPToggle) {

    private val messageConfig: YamlConfiguration
    private val miniMessage = MiniMessage.miniMessage()

    init {
        val messagesFile = File(plugin.dataFolder, "messages.yml")
        messageConfig = YamlConfiguration.loadConfiguration(messagesFile)
    }

    fun getMessage(key: String): Component {
        val raw = messageConfig.getString(key, "") ?: ""
        return miniMessage.deserialize(raw)
    }

    fun getMessage(key: String, placeholders: Map<String, String>): Component {
        var raw = messageConfig.getString(key, "") ?: ""
        placeholders.forEach { (k, v) ->
            raw = raw.replace(k, v)
        }
        return miniMessage.deserialize(raw)
    }
}