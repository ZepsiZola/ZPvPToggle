package zepsizola.me.zPvPToggle.commands.subcommands

import org.bukkit.command.CommandSender
import zepsizola.me.zPvPToggle.ZPvPToggle
import zepsizola.me.zPvPToggle.commands.SubCommand

class HelpCommand : SubCommand {
    override val name = "help"
    override val permission = "zpvptoggle.user"
    override val playerOnly = false
    override val description = "Display help information"
    override val usage = "/pvp help"
    
    override fun execute(plugin: ZPvPToggle, sender: CommandSender, args: Array<out String>): Boolean {
        sender.sendMessage(plugin.messageManager.getMessage("help_message"))
        return true
    }
}

