package zepsizola.me.zPvPToggle.commands

import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import zepsizola.me.zPvPToggle.ZPvPToggle

/**
 * Manages all subcommands for the plugin
 */
class CommandManager(private val plugin: ZPvPToggle) : CommandExecutor, TabCompleter {
    
    private val commands = mutableMapOf<String, SubCommand>()
    private val aliases = mutableMapOf<String, String>()
    
    /**
     * Register a subcommand
     */
    fun registerCommand(command: SubCommand) {
        commands[command.name.lowercase()] = command
        
        // Register aliases
        for (alias in command.aliases) {
            aliases[alias.lowercase()] = command.name.lowercase()
        }
    }
    
    /**
     * Get a subcommand by name or alias
     */
    fun getCommand(name: String): SubCommand? {
        val commandName = aliases[name.lowercase()] ?: name.lowercase()
        return commands[commandName]
    }
    
    /**
     * Get all registered commands
     */
    fun getCommands(): Collection<SubCommand> {
        return commands.values
    }
    
    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            // Default to toggle command if no args provided
            val toggleCommand = getCommand("toggle")
            if (toggleCommand != null && toggleCommand.canExecute(sender)) {
                return toggleCommand.execute(plugin, sender, emptyArray())
            } else {
                sender.sendMessage(plugin.messageManager.getMessage("invalid_command"))
                return true
            }
        }
        
        val subCommandName = args[0].lowercase()
        val subCommand = getCommand(subCommandName)
        
        if (subCommand == null) {
            // Check if the first argument is a player name (for admin toggle)
            if (sender.hasPermission("zpvptoggle.admin")) {
                val targetPlayer = plugin.server.getPlayerExact(args[0])
                if (targetPlayer != null) {
                    // If it's a player name, toggle their PvP status
                    val pvpManager = plugin.pvpManager
                    val messageManager = plugin.messageManager
                    
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
                
                // If args[0] isn't a player name but we have more args, try the target command
                if (args.size >= 2) {
                    val targetCommand = getCommand("target")
                    if (targetCommand != null) {
                        return targetCommand.execute(plugin, sender, args)
                    }
                }
            }
            
            sender.sendMessage(plugin.messageManager.getMessage("invalid_command"))
            return true
        }
        
        if (!subCommand.canExecute(sender)) {
            if (subCommand.playerOnly && sender !is org.bukkit.entity.Player) {
                sender.sendMessage(Component.text("Only players can use this command."))
            } else {
                sender.sendMessage(Component.text("You lack permission (${subCommand.permission})."))
            }
            return true
        }
        
        // Execute the subcommand with remaining args
        val subArgs = args.copyOfRange(1, args.size)
        return subCommand.execute(plugin, sender, subArgs)
    }
    
    override fun onTabComplete(
        sender: CommandSender,
        cmd: Command,
        label: String,
        args: Array<out String>
    ): List<String> {
        if (args.isEmpty()) {
            return emptyList()
        }
        
        if (args.size == 1) {
            val result = mutableListOf<String>()
            
            // Tab complete the subcommand name
            val availableCommands = commands.values
                .filter { it.canExecute(sender) }
                .flatMap { listOf(it.name) + it.aliases }
                .filter { it.startsWith(args[0].lowercase()) }
            
            result.addAll(availableCommands)
            
            // We don't suggest player names for the first argument anymore
            // This is to match the requested behavior where `/pvp [player]` doesn't tab complete
            
            return result
        }
        
        // Tab complete arguments for the subcommand
        val subCommandName = args[0].lowercase()
        val subCommand = getCommand(subCommandName)
        
        if (subCommand != null && subCommand.canExecute(sender)) {
            val subArgs = args.copyOfRange(1, args.size)
            return subCommand.tabComplete(plugin, sender, subArgs)
        }
        
        return emptyList()
    }
}

