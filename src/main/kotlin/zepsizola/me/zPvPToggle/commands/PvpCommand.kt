package zepsizola.me.zPvPToggle.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import zepsizola.me.zPvPToggle.ZPvPToggle
import zepsizola.me.zPvPToggle.commands.subcommands.*

class PvpCommand(private val plugin: ZPvPToggle) : CommandExecutor, TabCompleter {

    private val commandManager = CommandManager(plugin)
    
    init {
        // Register all subcommands
        commandManager.registerCommand(ToggleCommand())
        commandManager.registerCommand(OnCommand())
        commandManager.registerCommand(OffCommand())
        commandManager.registerCommand(HideCommand())
        commandManager.registerCommand(ShowCommand())
        commandManager.registerCommand(HelpCommand())
        commandManager.registerCommand(ReloadCommand())
        commandManager.registerCommand(TargetCommand())
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        return commandManager.onCommand(sender, cmd, label, args)
    }
    
    override fun onTabComplete(
        sender: CommandSender,
        cmd: Command,
        label: String,
        args: Array<out String>
    ): List<String> {
        return commandManager.onTabComplete(sender, cmd, label, args)
    }
}
