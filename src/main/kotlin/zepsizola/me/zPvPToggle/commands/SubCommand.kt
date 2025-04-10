package zepsizola.me.zPvPToggle.commands

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import zepsizola.me.zPvPToggle.ZPvPToggle

/**
 * Interface for all subcommands of the /pvp command
 */
interface SubCommand {
    /**
     * The name of the subcommand (e.g., "toggle", "on", "off")
     */
    val name: String
    
    /**
     * Permission required to use this command
     */
    val permission: String
    
    /**
     * Whether this command can only be used by players (not console)
     */
    val playerOnly: Boolean
    
    /**
     * Brief description of what the command does
     */
    val description: String
    
    /**
     * Usage example for the command
     */
    val usage: String
    
    /**
     * List of aliases for this command
     */
    val aliases: List<String>
        get() = emptyList()
    
    /**
     * Execute the command
     *
     * @param plugin The main plugin instance
     * @param sender The command sender
     * @param args Additional arguments passed to the command
     * @return true if the command was handled, false otherwise
     */
    fun execute(plugin: ZPvPToggle, sender: CommandSender, args: Array<out String>): Boolean
    
    /**
     * Provide tab completion options for this command
     *
     * @param plugin The main plugin instance
     * @param sender The command sender
     * @param args Additional arguments passed to the command
     * @return List of tab completion suggestions
     */
    fun tabComplete(plugin: ZPvPToggle, sender: CommandSender, args: Array<out String>): List<String> {
        return emptyList()
    }
    
    /**
     * Check if the sender has permission to use this command
     *
     * @param sender The command sender
     * @return true if the sender has permission, false otherwise
     */
    fun hasPermission(sender: CommandSender): Boolean {
        return sender.hasPermission(permission)
    }
    
    /**
     * Check if the command can be executed by the sender
     *
     * @param sender The command sender
     * @return true if the sender can execute the command, false otherwise
     */
    fun canExecute(sender: CommandSender): Boolean {
        if (playerOnly && sender !is Player) {
            return false
        }
        return hasPermission(sender)
    }
}

