package zepsizola.me.zPvPToggle.commands.subcommands

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import zepsizola.me.zPvPToggle.ZPvPToggle
import zepsizola.me.zPvPToggle.commands.SubCommand

class ToggleCommand : SubCommand {
    override val name = "toggle"
    override val permission = "zpvptoggle.user"
    override val playerOnly = true
    override val description = "Toggle your PvP status"
    override val usage = "/pvp toggle"
    override val aliases = listOf("")  // Empty string alias for default command
    
    override fun execute(plugin: ZPvPToggle, sender: CommandSender, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        
        val pvpManager = plugin.pvpManager
        val messageManager = plugin.messageManager
        
        val newState = pvpManager.togglePvp(sender)
        if (newState) {
            if (pvpManager.isFirstToggleOnThisSession(sender)) {
                sender.sendMessage(messageManager.getMessage("arena_message"))
            }
            sender.sendMessage(messageManager.getMessage("pvp_enabled"))
        } else {
            sender.sendMessage(messageManager.getMessage("pvp_disabled"))
        }
        
        return true
    }
}

