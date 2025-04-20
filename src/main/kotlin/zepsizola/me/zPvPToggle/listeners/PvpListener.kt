package zepsizola.me.zPvPToggle.listeners

import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.entity.EntityDamageByBlockEvent
import io.papermc.paper.event.player.PlayerBedFailEnterEvent
import io.papermc.paper.event.player.PlayerBedFailEnterEvent.FailReason
import org.bukkit.persistence.PersistentDataType
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.NamespacedKey
import zepsizola.me.zPvPToggle.ZPvPToggle
import org.bukkit.block.TileState
import org.bukkit.entity.EnderCrystal
import org.bukkit.block.Block
import org.bukkit.block.data.type.Bed
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.entity.LingeringPotionSplashEvent
import org.bukkit.event.entity.AreaEffectCloudApplyEvent
import org.bukkit.potion.PotionEffectType
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType.Category
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.Material
import org.bukkit.event.block.Action
import org.bukkit.block.data.BlockData
import java.util.function.Consumer
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import io.papermc.paper.threadedregions.scheduler.RegionScheduler

class PvpListener(private val plugin: ZPvPToggle) : Listener {

    private val attackerKey: NamespacedKey = NamespacedKey(plugin, "attacker")
    private val lastRespawnAnchorInteraction = java.util.concurrent.ConcurrentHashMap<Player, Collection<Player>>()
    private val lastBedInteraction = java.util.concurrent.ConcurrentHashMap<Player, Collection<Player>>()

    /**
     * Checks if PvP is valid for the given attacker and victim.
     * @param attacker The player who is attacking.
     * @param victim The player who is being attacked.
     * @return true if PvP is valid, false otherwise.
     */
    fun isPvpValid(attacker: Player?, victim: Player?): Boolean {
        // plugin.logger.info("Checking PvP validity for attacker: ${attacker.name}, victim: ${victim.name}")
        if (attacker == null || victim == null) {
            return true
        }
        val valid = ((plugin.pvpManager.isPvpEnabled(attacker) && plugin.pvpManager.isPvpEnabled(victim)) || (victim == attacker))
        // plugin.logger.info("PvP validity: $valid")
        return valid
    }

    /**
     * Checks if a potion effect type is negative/harmful
     * @param effects The list of potion effects to check.
     */
    private fun hasNegativeEffect(effects: List<PotionEffect>): Boolean {
        for (effect in effects) {
            if (effect.type.effectCategory == PotionEffectType.Category.HARMFUL) return true
        }
        return false
    }
    /**
     * Checks if a potion meta has negative effects
     * @param potionMeta The potion meta to check.
     */
    private fun isNegativePotion(potionMeta: PotionMeta): Boolean {
        if (hasNegativeEffect(potionMeta.basePotionType?.potionEffects ?: emptyList())) return true
        if (hasNegativeEffect(potionMeta.customEffects)) return true
        return false
    }

    @EventHandler
    fun onPlayerDamage(event: EntityDamageByEntityEvent) {
        val victim = event.entity as? Player ?: return
        val attacker = event.damageSource.causingEntity as? Player ?: return
        if (isPvpValid(attacker, victim)) return
        event.isCancelled = true
        val message = plugin.messageManager.getMessage(
            "attack_pvp_disabled", 
            mapOf("%player%" to victim.name)
        )
        attacker.sendMessage(message)
    }

    @EventHandler
    fun onPlayerPushed(event: EntityPushedByEntityAttackEvent) {
        val victim = event.entity as? Player ?: return
        val attacker = event.getPushedBy() as? Player ?: return
        if (isPvpValid(attacker, victim)) return
        event.isCancelled = true
    }

    /*
     * Add a affectedPlayers collection with the culprit Player as the key to the lastBedInteraction map when a player
     *  initiates the bed explosion.
     * Then remove the entry from the lastBedInteraction map on the next tick.
     */
    @EventHandler
    fun onPlayerInitiateBedExplosion(event: PlayerBedFailEnterEvent) {
        if (!event.willExplode) return
        val attacker = event.player
        val affectedPlayers = event.bed.location.getNearbyPlayers(9.5)
        lastBedInteraction[attacker] = affectedPlayers
        plugin.server.regionScheduler.runDelayed(plugin, attacker.location, Consumer {_: ScheduledTask ->
            lastBedInteraction.remove(attacker)
        }, 1L)
    }

    /*
     * Add a affectedPlayers collection with the culprit Player as the key to the lastRespawnAnchorInteraction map when a player
     *  initiates the respawn anchor explosion.
     * Then remove the entry from the lastRespawnAnchorInteraction map on the next tick.
     */
    @EventHandler
    fun onPlayerInitiateRespawnAnchorExplosion(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return
        val respawnAnchor = block.blockData as? org.bukkit.block.data.type.RespawnAnchor ?: return
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.material != Material.GLOWSTONE) return
        val player = event.player
        if (respawnAnchor.charges == (respawnAnchor.maximumCharges)) {
            val affectedPlayers = block.location.getNearbyPlayers(9.5)
            lastRespawnAnchorInteraction[player] = affectedPlayers
            plugin.server.regionScheduler.runDelayed(plugin, player.location, Consumer {_: ScheduledTask ->
                lastRespawnAnchorInteraction.remove(player)
            }, 1L)
        }
    }

    @EventHandler
    fun onBedExplodesPlayer(event: EntityDamageByBlockEvent) {
        if (event.cause != DamageCause.BLOCK_EXPLOSION) return
        if (event.damagerBlockState?.blockData !is org.bukkit.block.data.type.Bed) return
        val victim = event.entity as? Player ?: return
        var attacker: Player? = null
        for ((player, affectedPlayers) in lastBedInteraction) {
            if (!affectedPlayers.contains(victim)) continue
            attacker = player
        }
        if (isPvpValid(attacker, victim)) return
        event.isCancelled = true
        val message = plugin.messageManager.getMessage(
            "attack_pvp_disabled", 
            mapOf("%player%" to victim.name)
        )
        attacker!!.sendMessage(message)
    }

    @EventHandler
    fun onRespawnAnchorExplodesPlayer(event: EntityDamageByBlockEvent) {
        if (event.cause != DamageCause.BLOCK_EXPLOSION) return
        if (event.damagerBlockState?.blockData !is org.bukkit.block.data.type.RespawnAnchor) return
        val victim = event.entity as? Player ?: return
        var attacker: Player? = null
        for ((player, affectedPlayers) in lastRespawnAnchorInteraction) {
            if (!affectedPlayers.contains(victim)) continue
            attacker = player
        }
        if (isPvpValid(attacker, victim)) return
        event.isCancelled = true
        val message = plugin.messageManager.getMessage(
            "attack_pvp_disabled", 
            mapOf("%player%" to victim.name)
        )
        attacker!!.sendMessage(message)
        // val message = plugin.messageManager.getMessage(
        //     "bed_exploded", 
        //     mapOf("%player%" to victim.name)
        // )
        // attacker.sendMessage(message)
    }

    @EventHandler
    fun onPotionSplash(event: PotionSplashEvent) {
        val thrower = event.entity.shooter as? Player ?: return
        if (!isNegativePotion(event.potion.potionMeta)) return
        for (affectedEntity in event.affectedEntities) {
            if (affectedEntity !is Player) continue
            if (affectedEntity == thrower) continue
            if (isPvpValid(thrower, affectedEntity)) continue
            event.setIntensity(affectedEntity, 0.0)
            val message = plugin.messageManager.getMessage(
                "attack_pvp_disabled", 
                mapOf("%player%" to affectedEntity.name)
            )
            thrower.sendMessage(message)
        }
    }
    
    @EventHandler
    fun onLingeringPotionSplash(event: LingeringPotionSplashEvent) {
        val thrower = event.entity.shooter as? Player ?: return
        val potionMeta = event.entity.potionMeta
        val areaEffectCloud = event.areaEffectCloud
        areaEffectCloud.persistentDataContainer.set(
            NamespacedKey(plugin, "potion_thrower"),
            PersistentDataType.STRING,
            thrower.uniqueId.toString()
        )
        areaEffectCloud.persistentDataContainer.set(
            NamespacedKey(plugin, "has_negative_effects"),
            PersistentDataType.BOOLEAN,
            isNegativePotion(potionMeta)
        )
    }
    
    @EventHandler
    fun onAreaEffectCloudApply(event: AreaEffectCloudApplyEvent) {
        val cloud = event.entity
        val throwerUUID = cloud.persistentDataContainer.get(
            NamespacedKey(plugin, "potion_thrower"),
            PersistentDataType.STRING
        ) ?: return
        val hasNegativeEffects = cloud.persistentDataContainer.get(
            NamespacedKey(plugin, "has_negative_effects"),
            PersistentDataType.BOOLEAN
        ) ?: false
        if (!hasNegativeEffects) return
        val uuid = java.util.UUID.fromString(throwerUUID)
        val thrower = plugin.server.getPlayer(uuid) ?: return
        val affectedEntities = ArrayList(event.affectedEntities)
        for (affectedEntity in affectedEntities) {
            if (affectedEntity !is Player) continue
            if (isPvpValid(thrower, affectedEntity)) continue
            event.affectedEntities.remove(affectedEntity)
            // val message = plugin.messageManager.getMessage(
            //     "attack_pvp_disabled", 
            //     mapOf("%player%" to affectedEntity.name)
            // )
            // thrower.sendMessage(message)
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (!plugin.disablePvpOnDeath) return
        val player = event.player
        if (plugin.pvpManager.isPvpEnabled(player)) {
            plugin.pvpManager.setPvp(player, false)
            val message = plugin.messageManager.getMessage(
                "pvp_disabled_on_death",
                mapOf("%player%" to player.name)
            )
            player.sendMessage(message)
        }
    }
}
