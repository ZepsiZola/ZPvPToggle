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
    override val usage = "/pvp hide [--own]"
    
    override fun execute(plugin: ZPvPToggle, sender: CommandSender, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        
        val pvpManager = plugin.pvpManager
        val messageManager = plugin.messageManager
        
        // Check if the --own flag is present
        if (args.isNotEmpty() && (args[0] == "--own" || args[0] == "-o")) {
            val canSeeOwnIndicator = pvpManager.toggleOwnIndicator(sender)
            if (canSeeOwnIndicator) {
                sender.sendMessage(messageManager.getMessage("indicator.show_own"))
            } else {
                sender.sendMessage(messageManager.getMessage("indicator.hide_own"))
            }
        } else {
            val canSeeIndicators = pvpManager.toggleIndicators(sender)
            if (canSeeIndicators) {
                sender.sendMessage(messageManager.getMessage("indicator.show"))
            } else {
                sender.sendMessage(messageManager.getMessage("indicator.hide"))
            }
        }
        
        return true
    }
    
    override fun tabComplete(plugin: ZPvPToggle, sender: CommandSender, args: Array<out String>): List<String> {
        if (args.size == 1) {
            val input = args[0].lowercase()
            return listOf("--own", "-o").filter { it.startsWith(input) }
        }
        return emptyList()
    }
}

