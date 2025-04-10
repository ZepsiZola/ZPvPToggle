package zepsizola.me.zPvPToggle.commands.subcommands

import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import zepsizola.me.zPvPToggle.ZPvPToggle
import zepsizola.me.zPvPToggle.commands.SubCommand

class ShowCommand : SubCommand {
    override val name = "show"
    override val permission = "zpvptoggle.user"
    override val playerOnly = true
    override val description = "Show PvP particle indicators"
    override val usage = "/pvp show"
    
    override fun execute(plugin: ZPvPToggle, sender: CommandSender, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        
        val pvpManager = plugin.pvpManager
        val messageManager = plugin.messageManager
        
        val state = pvpManager.getState(sender)
        if (!state.canSeeIndicators) {
            state.canSeeIndicators = true
            sender.sendMessage(messageManager.getMessage("show_indicators"))
        } else {
            sender.sendMessage(Component.text("You can already see PvP indicators."))
        }
        
        return true
    }
}

