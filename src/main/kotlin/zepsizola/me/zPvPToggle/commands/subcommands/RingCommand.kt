package zepsizola.me.zPvPToggle.commands.subcommands

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import zepsizola.me.zPvPToggle.ZPvPToggle
import zepsizola.me.zPvPToggle.commands.SubCommand
import zepsizola.me.zPvPToggle.models.ParticleRingManager

class RingCommand : SubCommand {
    override val name = "ring"
    override val permission = "zpvptoggle.user"
    override val playerOnly = true
    override val description = "Change your PvP indicator ring style"
    override val usage = "/pvp ring <style>"
    
    override fun execute(plugin: ZPvPToggle, sender: CommandSender, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        
        // Create a ring manager to get available rings
        val ringManager = ParticleRingManager(plugin)
        ringManager.loadRings()
        
        // If no args, list available rings
        if (args.isEmpty()) {
            val currentRing = plugin.pvpManager.getIndicatorRingId(sender)
            sender.sendMessage("§6Your current ring style: §e$currentRing")
            sender.sendMessage("§6Available ring styles:")
            
            for (ring in ringManager.getAllRings()) {
                val selected = if (ring.id == currentRing) " §a(selected)" else ""
                sender.sendMessage("§7- §e${ring.id}$selected")
            }
            return true
        }
        
        // Set the ring style
        val ringId = args[0].lowercase()
        val ring = ringManager.getRing(ringId)
        
        // If the ring doesn't exist (returns default), check if the ID matches
        if (ring.id != ringId) {
            sender.sendMessage("§cRing style '$ringId' not found. Use §e/pvp ring§c to see available styles.")
            return true
        }
        
        // Set the player's ring style
        plugin.pvpManager.setIndicatorRing(sender, ringId)
        sender.sendMessage("§aYour PvP indicator ring style has been set to §e$ringId§a.")
        
        return true
    }
    
    override fun tabComplete(plugin: ZPvPToggle, sender: CommandSender, args: Array<out String>): List<String> {
        if (args.size == 1) {
            // Create a ring manager to get available rings
            val ringManager = ParticleRingManager(plugin)
            ringManager.loadRings()
            
            val input = args[0].lowercase()
            return ringManager.getAllRings()
                .map { it.id }
                .filter { it.startsWith(input) }
        }
        return emptyList()
    }
}

