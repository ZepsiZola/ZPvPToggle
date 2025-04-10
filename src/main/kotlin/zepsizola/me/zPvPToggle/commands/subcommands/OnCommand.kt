package zepsizola.me.zPvPToggle.commands.subcommands

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import zepsizola.me.zPvPToggle.ZPvPToggle
import zepsizola.me.zPvPToggle.commands.SubCommand

class OnCommand : SubCommand {
    override val name = "on"
    override val permission = "zpvptoggle.user"
    override val playerOnly = true
    override val description = "Enable your PvP status"
    override val usage = "/pvp on"
    
    override fun execute(plugin: ZPvPToggle, sender: CommandSender, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        
        val pvpManager = plugin.pvpManager
        val messageManager = plugin.messageManager
        
        pvpManager.setPvp(sender, true)
        
        if (pvpManager.isFirstToggleOnThisSession(sender)) {
            sender.sendMessage(messageManager.getMessage("arena_message"))
        }
        sender.sendMessage(messageManager.getMessage("pvp_enabled"))
        
        return true
    }
}

