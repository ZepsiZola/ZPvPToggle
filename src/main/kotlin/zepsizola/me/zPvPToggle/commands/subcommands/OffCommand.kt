package zepsizola.me.zPvPToggle.commands.subcommands

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import zepsizola.me.zPvPToggle.ZPvPToggle
import zepsizola.me.zPvPToggle.commands.SubCommand

class OffCommand : SubCommand {
    override val name = "off"
    override val permission = "zpvptoggle.user"
    override val playerOnly = true
    override val description = "Disable your PvP status"
    override val usage = "/pvp off"
    
    override fun execute(plugin: ZPvPToggle, sender: CommandSender, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        
        val pvpManager = plugin.pvpManager
        val messageManager = plugin.messageManager
        
        pvpManager.setPvp(sender, false)
        sender.sendMessage(messageManager.getMessage("pvp_disabled"))
        
        return true
    }
}

