package zepsizola.me.zPvPToggle.commands.subcommands

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import zepsizola.me.zPvPToggle.ZPvPToggle
import zepsizola.me.zPvPToggle.commands.SubCommand

class TargetCommand : SubCommand {
    override val name = "target"
    override val permission = "zpvptoggle.admin"
    override val playerOnly = false
    override val description = "Toggle PvP for another player"
    override val usage = "/pvp <player> <on|off>"
    
    override fun execute(plugin: ZPvPToggle, sender: CommandSender, args: Array<out String>): Boolean {
        val messageManager = plugin.messageManager
        val pvpManager = plugin.pvpManager
        
        // This command expects the first arg to be a player name and the second to be on/off
        // But in the CommandManager, we're passing the full args array, so we need to check
        // if the first arg is a valid player
        
        if (args.isEmpty()) {
            sender.sendMessage(messageManager.getMessage("invalid_command"))
            return true
        }
        
        val targetName = args[0]
        val targetPlayer = Bukkit.getPlayerExact(targetName)
        
        if (targetPlayer == null) {
            sender.sendMessage(Component.text("Player not found or not online."))
            return true
        }
        
        // Check if we have a second argument (on/off)
        if (args.size < 2) {
            sender.sendMessage(messageManager.getMessage("invalid_command"))
            return true
        }
        
        when (args[1].lowercase()) {
            "on" -> {
                pvpManager.setPvp(targetPlayer, true)
                targetPlayer.sendMessage(messageManager.getMessage("pvp_enabled"))
                sender.sendMessage(
                    messageManager.getMessage(
                        "pvp_enabled_other",
                        mapOf("%player%" to targetPlayer.name)
                    )
                )
            }
            "off" -> {
                pvpManager.setPvp(targetPlayer, false)
                targetPlayer.sendMessage(messageManager.getMessage("pvp_disabled"))
                sender.sendMessage(
                    messageManager.getMessage(
                        "pvp_disabled_other",
                        mapOf("%player%" to targetPlayer.name)
                    )
                )
            }
            else -> {
                sender.sendMessage(messageManager.getMessage("invalid_command"))
            }
        }
        
        return true
    }
    
    override fun tabComplete(plugin: ZPvPToggle, sender: CommandSender, args: Array<out String>): List<String> {
        if (args.isEmpty()) {
            // Return all online player names
            return Bukkit.getOnlinePlayers().map { it.name }
        }
        
        if (args.size == 1) {
            // Filter player names based on input
            val input = args[0].lowercase()
            return Bukkit.getOnlinePlayers()
                .map { it.name }
                .filter { it.lowercase().startsWith(input) }
        }
        
        if (args.size == 2) {
            // Suggest on/off
            val input = args[1].lowercase()
            return listOf("on", "off").filter { it.startsWith(input) }
        }
        
        return emptyList()
    }
}

