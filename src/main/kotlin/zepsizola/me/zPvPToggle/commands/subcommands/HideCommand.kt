package zepsizola.me.zPvPToggle.commands.subcommands

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import zepsizola.me.zPvPToggle.ZPvPToggle
import zepsizola.me.zPvPToggle.commands.SubCommand

class HideCommand : SubCommand {
    override val name = "hide"
    override val permission = "zpvptoggle.user"
    override val playerOnly = true
    override val description = "Hide PvP particle indicators"
    override val usage = "/pvp hide"
    
    override fun execute(plugin: ZPvPToggle, sender: CommandSender, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        
        val pvpManager = plugin.pvpManager
        val messageManager = plugin.messageManager
        
        val canSeeIndicators = pvpManager.toggleIndicators(sender)
        if (canSeeIndicators) {
            sender.sendMessage(messageManager.getMessage("show_indicators"))
        } else {
            sender.sendMessage(messageManager.getMessage("hide_indicators"))
        }
        
        return true
    }
}

