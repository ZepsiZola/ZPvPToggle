package zepsizola.me.zPvPToggle.commands.subcommands

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import zepsizola.me.zPvPToggle.ZPvPToggle
import zepsizola.me.zPvPToggle.commands.SubCommand

class ToggleCommand : SubCommand {
    override val name = "toggle"
    override val permission = "zpvptoggle.user"
    override val playerOnly = false
    override val description = "Toggle your PvP status or another player's"
    override val usage = "/pvp toggle [player]"
    override val aliases = listOf("")  // Empty string alias for default command
    
    override fun execute(plugin: ZPvPToggle, sender: CommandSender, args: Array<out String>): Boolean {
        val pvpManager = plugin.pvpManager
        val messageManager = plugin.messageManager
        
        // Check if a target player is specified
        if (args.isNotEmpty() && sender.hasPermission("zpvptoggle.admin")) {
            val targetName = args[0]
            val targetPlayer = Bukkit.getPlayerExact(targetName)
            
            if (targetPlayer == null) {
                sender.sendMessage(messageManager.getMessage("player_not_found"))
                return true
            }
            
            val newState = pvpManager.togglePvp(targetPlayer)
            if (newState) {
                targetPlayer.sendMessage(messageManager.getMessage("pvp_enabled"))
                sender.sendMessage(
                    messageManager.getMessage(
                        "pvp_enabled_other",
                        mapOf("%player%" to targetPlayer.name)
                    )
                )
            } else {
                targetPlayer.sendMessage(messageManager.getMessage("pvp_disabled"))
                sender.sendMessage(
                    messageManager.getMessage(
                        "pvp_disabled_other",
                        mapOf("%player%" to targetPlayer.name)
                    )
                )
            }
            return true
        }
        
        // If no target specified, toggle the sender's PvP (if sender is a player)
        if (sender !is Player) {
            sender.sendMessage(messageManager.getMessage("player_only_command"))
            return true
        }
        
        val newState = pvpManager.togglePvp(sender)
        if (newState) {
            if (pvpManager.isFirstToggleOnThisSession(sender) && plugin.warningMessageEnabled) {
                sender.sendMessage(messageManager.getMessage("warning_message"))
            }
            sender.sendMessage(messageManager.getMessage("pvp_enabled"))
        } else {
            sender.sendMessage(messageManager.getMessage("pvp_disabled"))
        }
        
        return true
    }
    
    override fun tabComplete(plugin: ZPvPToggle, sender: CommandSender, args: Array<out String>): List<String> {
        if (args.size == 1 && sender.hasPermission("zpvptoggle.admin")) {
            val input = args[0].lowercase()
            return Bukkit.getOnlinePlayers()
                .map { it.name }
                .filter { it.lowercase().startsWith(input) }
        }
        return emptyList()
    }
}

