package zepsizola.me.zPvPToggle.commands.subcommands

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import zepsizola.me.zPvPToggle.ZPvPToggle
import zepsizola.me.zPvPToggle.commands.SubCommand
import zepsizola.me.zPvPToggle.models.ParticleRingManager

class RingCommand : SubCommand {
    override val name = "indicator"
    override val permission = "zpvptoggle.user"
    override val playerOnly = true
    override val description = "Change your PvP indicator ring style"
    override val usage = "/pvp indicator <style>"
    
    override fun execute(plugin: ZPvPToggle, sender: CommandSender, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        
        // Create a ring manager to get available rings
        val ringManager = ParticleRingManager(plugin)
        ringManager.loadRings()
        
        // If no args, list available rings
        if (args.isEmpty()) {
            val currentRing = plugin.pvpManager.getIndicatorRingId(sender)
            
            // Get messages using MessageManager
            sender.sendMessage(plugin.messageManager.getMessage(
                "indicator.available_styles", 
                mapOf("%indicator%" to currentRing)
            ))
            // sender.sendMessage(plugin.messageManager.getMessage("indicator.available_styles"))
            val defaultRing = ringManager.getDefaultRing()
            for (ring in ringManager.getAllRings()) {
                // Only show rings the player has permission to use or the default ring
                if (ring.id == defaultRing.id || sender.hasPermission("zpvptoggle.indicator.${ring.id}")) {
                    val selected = if (ring.id == currentRing) " (selected)" else ""
                    sender.sendMessage(plugin.messageManager.getMessage(
                        "indicator.style_entry", 
                        mapOf("%indicator%" to ring.id, "%selected%" to selected)
                    ))
                }
            }
            return true
        }
        
        // Set the ring style
        val ringId = args[0].lowercase()
        val ring = ringManager.getRing(ringId)
        
// If the ring doesn't exist (returns default), check if the ID matches
        if (ring.id != ringId) {
            sender.sendMessage(plugin.messageManager.getMessage(
                "indicator.not_found", 
                mapOf("%indicator%" to ringId)
            ))
            return true
        }
        
        // Check if this is the default ring or if the player has permission to use it
        val defaultRing = ringManager.getDefaultRing()
        if (ring.id != defaultRing.id && !sender.hasPermission("zpvptoggle.indicator.${ring.id}")) {
            sender.sendMessage(plugin.messageManager.getMessage(
                "indicator.no_permission", 
                mapOf("%indicator%" to ringId)
            ))
            return true
        }
        
        // Set the player's ring style
        plugin.pvpManager.setIndicatorRing(sender, ringId)
        sender.sendMessage(plugin.messageManager.getMessage(
            "indicator.set_success", 
            mapOf("%indicator%" to ringId)
        ))
        
        return true
    }
    
    override fun tabComplete(plugin: ZPvPToggle, sender: CommandSender, args: Array<out String>): List<String> {
        if (args.size == 1) {
            // Create a ring manager to get available rings
            val ringManager = ParticleRingManager(plugin)
            ringManager.loadRings()
            
            val defaultRing = ringManager.getDefaultRing()
            val input = args[0].lowercase()
            
            return ringManager.getAllRings()
                .map { it.id }
                .filter { 
                    // Include the ring if it's the default or if the player has permission
                    it.startsWith(input) && (it == defaultRing.id || 
                        (sender is Player && sender.hasPermission("zpvptoggle.indicator.$it")))
                }
        }
        return emptyList()
    }
}

