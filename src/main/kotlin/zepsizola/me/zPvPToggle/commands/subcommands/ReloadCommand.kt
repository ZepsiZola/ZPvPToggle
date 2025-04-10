package zepsizola.me.zPvPToggle.commands.subcommands

import org.bukkit.command.CommandSender
import zepsizola.me.zPvPToggle.ZPvPToggle
import zepsizola.me.zPvPToggle.commands.SubCommand

class ReloadCommand : SubCommand {
    override val name = "reload"
    override val permission = "zpvptoggle.admin"
    override val playerOnly = false
    override val description = "Reload the plugin configuration"
    override val usage = "/pvp reload"
    
    override fun execute(plugin: ZPvPToggle, sender: CommandSender, args: Array<out String>): Boolean {
        plugin.reloadPlugin()
        sender.sendMessage(plugin.messageManager.getMessage("config_reloaded"))
        return true
    }
}

