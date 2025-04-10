package zepsizola.me.zPvPToggle.commands

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import zepsizola.me.zPvPToggle.ZPvPToggle

class PvpCommand(private val plugin: ZPvPToggle) : CommandExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        val msgManager = plugin.messageManager
        val pvpManager = plugin.pvpManager

        if (args.isEmpty()) {
            // /pvp
            if (sender !is Player) {
                sender.sendMessage(Component.text("Only players can toggle their own PvP."))
                return true
            }
            if (!sender.hasPermission("zpvptoggle.user")) {
                sender.sendMessage(Component.text("You lack permission (zpvptoggle.user)."))
                return true
            }

            val newState = pvpManager.togglePvp(sender)
            if (newState) {
                if (pvpManager.isFirstToggleOnThisSession(sender)) {
                    sender.sendMessage(msgManager.getMessage("arena_message"))
                }
                sender.sendMessage(msgManager.getMessage("pvp_enabled"))
            } else {
                sender.sendMessage(msgManager.getMessage("pvp_disabled"))
            }
            return true
        }

        when (args[0].lowercase()) {
            "help" -> {
                sender.sendMessage(msgManager.getMessage("help_message"))
            }
            "on" -> {
                handleToggleCommand(sender, true)
            }
            "off" -> {
                handleToggleCommand(sender, false)
            }
            "toggle" -> {
                if (sender !is Player) {
                    sender.sendMessage(Component.text("Only players can toggle their own PvP."))
                    return true
                }
                if (!sender.hasPermission("zpvptoggle.user")) {
                    sender.sendMessage(Component.text("You lack permission (zpvptoggle.user)."))
                    return true
                }
                val newState = pvpManager.togglePvp(sender)
                if (newState) {
                    if (pvpManager.isFirstToggleOnThisSession(sender)) {
                        sender.sendMessage(msgManager.getMessage("arena_message"))
                    }
                    sender.sendMessage(msgManager.getMessage("pvp_enabled"))
                } else {
                    sender.sendMessage(msgManager.getMessage("pvp_disabled"))
                }
            }
            "hide" -> {
                if (sender !is Player) {
                    sender.sendMessage(Component.text("Only players can hide PvP indicators."))
                    return true
                }
                if (!sender.hasPermission("zpvptoggle.user")) {
                    sender.sendMessage(Component.text("You lack permission (zpvptoggle.user)."))
                    return true
                }
                val canSeeIndicators = pvpManager.toggleIndicators(sender)
                if (canSeeIndicators) {
                    sender.sendMessage(msgManager.getMessage("show_indicators"))
                } else {
                    sender.sendMessage(msgManager.getMessage("hide_indicators"))
                }
            }
            "show" -> {
                if (sender !is Player) {
                    sender.sendMessage(Component.text("Only players can show PvP indicators."))
                    return true
                }
                if (!sender.hasPermission("zpvptoggle.user")) {
                    sender.sendMessage(Component.text("You lack permission (zpvptoggle.user)."))
                    return true
                }
                val state = pvpManager.getState(sender)
                if (!state.canSeeIndicators) {
                    state.canSeeIndicators = true
                    sender.sendMessage(msgManager.getMessage("show_indicators"))
                } else {
                    sender.sendMessage(Component.text("You can already see PvP indicators."))
                }
            }
            "reload" -> {
                if (!sender.hasPermission("zpvptoggle.admin")) {
                    sender.sendMessage(Component.text("You lack permission (zpvptoggle.admin)."))
                    return true
                }
                plugin.reloadPlugin()
                sender.sendMessage(msgManager.getMessage("config_reloaded"))
            }
            else -> {
                // Admin usage? /pvp <player> <on|off>
                if (!sender.hasPermission("zpvptoggle.admin")) {
                    sender.sendMessage(msgManager.getMessage("invalid_command"))
                    return true
                }
                if (args.size < 2) {
                    sender.sendMessage(msgManager.getMessage("invalid_command"))
                    return true
                }
                val targetPlayer = Bukkit.getPlayerExact(args[0])
                if (targetPlayer == null) {
                    sender.sendMessage(Component.text("Player not found or not online."))
                    return true
                }
                when (args[1].lowercase()) {
                    "on" -> {
                        pvpManager.setPvp(targetPlayer, true)
                        targetPlayer.sendMessage(msgManager.getMessage("pvp_enabled"))
                        sender.sendMessage(
                            msgManager.getMessage(
                                "pvp_enabled_other",
                                mapOf("%player%" to targetPlayer.name)
                            )
                        )
                    }
                    "off" -> {
                        pvpManager.setPvp(targetPlayer, false)
                        targetPlayer.sendMessage(msgManager.getMessage("pvp_disabled"))
                        sender.sendMessage(
                            msgManager.getMessage(
                                "pvp_disabled_other",
                                mapOf("%player%" to targetPlayer.name)
                            )
                        )
                    }
                    else -> {
                        sender.sendMessage(msgManager.getMessage("invalid_command"))
                    }
                }
            }
        }
        return true
    }

    private fun handleToggleCommand(sender: CommandSender, enable: Boolean) {
        if (sender !is Player) {
            sender.sendMessage(Component.text("Only players can toggle their own PvP."))
            return
        }
        if (!sender.hasPermission("zpvptoggle.user")) {
            sender.sendMessage(Component.text("You lack permission (zpvptoggle.user)."))
            return
        }
        if (enable) {
            plugin.pvpManager.setPvp(sender, true)
            if (plugin.pvpManager.isFirstToggleOnThisSession(sender)) {
                sender.sendMessage(plugin.messageManager.getMessage("arena_message"))
            }
            sender.sendMessage(plugin.messageManager.getMessage("pvp_enabled"))
        } else {
            plugin.pvpManager.setPvp(sender, false)
            sender.sendMessage(plugin.messageManager.getMessage("pvp_disabled"))
        }
    }
}
