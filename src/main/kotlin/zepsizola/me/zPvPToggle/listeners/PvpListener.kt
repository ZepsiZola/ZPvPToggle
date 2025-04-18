package zepsizola.me.zPvPToggle.listeners

import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import zepsizola.me.zPvPToggle.ZPvPToggle

class PvpListener(private val plugin: ZPvPToggle) : Listener {

    @EventHandler
    fun onPlayerDamage(event: EntityDamageByEntityEvent) {
        if (event.entityType != EntityType.PLAYER) return
        val victim = event.entity as Player


        // Determine the attacker
        val damager = event.damager
        val attacker: Player? = when (damager.type) {
            EntityType.PLAYER -> damager as Player
            EntityType.ARROW, EntityType.SPECTRAL_ARROW -> {
                val projectile = damager as? org.bukkit.entity.Projectile
                projectile?.shooter as? Player
            }
            else -> null
        }

        // Check if the victim has PvP disabled
        if (!plugin.pvpManager.isPvpEnabled(victim)) {
            event.isCancelled = true
            // Notify the attacker if they exist
            if (attacker != null) {
                val message = plugin.messageManager.getMessage(
                    "attack_pvp_disabled", 
                    mapOf("%player%" to victim.name)
                )
                attacker.sendMessage(message)
            }
            return
        }

        // If attacker exists and has PvP disabled, cancel the event
        if (attacker != null && !plugin.pvpManager.isPvpEnabled(attacker)) {
            event.isCancelled = true
            // Notify the attacker that they need to enable PvP
            val message = plugin.messageManager.getMessage(
                "attack_pvp_disabled", 
                mapOf("%player%" to victim.name)
            )
            attacker.sendMessage(message)
        }
    }
}
