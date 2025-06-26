package zepsizola.me.zPvPToggle.listeners

import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable
import org.bukkit.entity.LivingEntity
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
import org.bukkit.Server
import org.bukkit.Bukkit
import org.bukkit.event.block.Action
import org.bukkit.block.data.BlockData
import java.util.function.Consumer
import java.util.function.Predicate
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import io.papermc.paper.threadedregions.scheduler.RegionScheduler
import org.bukkit.entity.TNTPrimed
import org.bukkit.entity.minecart.ExplosiveMinecart
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.ExplosionPrimeEvent
import org.bukkit.entity.Entity

class PvpListener(private val plugin: ZPvPToggle) : Listener {

    private val attackerKey: NamespacedKey = NamespacedKey(plugin, "attacker")
    private val lastRespawnAnchorInteraction = java.util.concurrent.ConcurrentHashMap<Player, Collection<LivingEntity>>()
    private val lastBedInteraction = java.util.concurrent.ConcurrentHashMap<Player, Collection<LivingEntity>>()
    private val tntOwners = java.util.concurrent.ConcurrentHashMap<Entity, Player>()

    /**
     * Checks if PvP is valid for the given attacker and victim.
     * @param attacker The player who is attacking.
     * @param victim The player who is being attacked.
     * @return true if PvP is valid, false otherwise.
     */
    fun isPvpValid(attacker: Entity?, victim: Entity?): Boolean {
        // plugin.logger.info("Checking PvP validity for attacker: ${attacker.name}, victim: ${victim.name}")
        if (attacker == null || victim == null) {
            return true
        }
        if (victim is Tameable && !victim.ownerNearby()) return false
        if (attacker is Tameable && !attacker.ownerNearby()) return false
        val attackerPlayer = attacker.getPlayerOrPet() ?: return true
        val victimPlayer = victim.getPlayerOrPet() ?: return true
        val valid = ((plugin.pvpManager.isPvpEnabled(attackerPlayer) && plugin.pvpManager.isPvpEnabled(victimPlayer)) || (victimPlayer == attackerPlayer))
        if (valid) {
            plugin.pvpManager.setCooldown(attackerPlayer)
            plugin.pvpManager.setCooldown(victimPlayer)
        }
        // plugin.logger.debug("PvP validity: $valid")
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

    private fun Entity.getPlayerOrPet(): Player? {
        if (this is Player) return this
        if (!plugin.protectPets) return null
        return (this as? Tameable)?.owner as? Player ?: return null
    }

    /**
     * Checks if a pet's owner is nearby
     * @return true if the owner is nearby (of if pet has no owner), false otherwise.
     */
    private fun Tameable.ownerNearby(): Boolean {
        if (!this.isTamed) return true
        if (!plugin.protectPets) return true
        return this.location.getNearbyPlayers(32.0).contains(this.owner)
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

    private fun makeAndSendMessage(attacker: Entity, victim: Entity, yamlMessage: String) {
        val message = when (victim) {
            is Tameable -> plugin.messageManager.getMessage(
                "$yamlMessage.pet", 
                mapOf("%player%" to (Bukkit.getOfflinePlayer(victim.ownerUniqueId!!).name ?: "unknown"))
            )
            is Player -> plugin.messageManager.getMessage(
                "$yamlMessage.player", 
                mapOf("%player%" to victim.name)
            )
            else -> null
        }
        if (message != null) attacker.sendMessage(message)
    }

    @EventHandler
    fun onPlayerDamage(event: EntityDamageByEntityEvent) {
        val victim = event.entity
        val attacker = event.damageSource.causingEntity ?: return
        if (isPvpValid(attacker, victim)) return
        event.isCancelled = true
        makeAndSendMessage(attacker, victim, "attack_pvp_disabled")
    }

    @EventHandler
    fun onPlayerPushed(event: EntityPushedByEntityAttackEvent) {
        val victim = event.entity
        val attacker = event.pushedBy
        if (isPvpValid(attacker, victim)) return
        event.isCancelled = true
        // makeAndSendMessage(attacker, victim, "attack_pvp_disabled")
    }

    /*
     * add a affectedplayers collection with the culprit player as the key to the lastbedinteraction map when a player
     *  initiates the bed explosion.
     * Then remove the entry from the lastBedInteraction map on the next tick.
     */
    @EventHandler
    fun onPlayerInitiateBedExplosion(event: PlayerBedFailEnterEvent) {
        if (!event.willExplode) return
        val attacker = event.player
        val affectedPlayers = event.bed.location.getNearbyLivingEntities(9.5, Predicate<LivingEntity> { entity ->
            entity is Player || entity is Tameable
        })
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
            // val affectedPlayers = block.location.getNearbyEntities(9.5)
            val affectedPlayers = block.location.getNearbyLivingEntities(9.5, Predicate<LivingEntity> { entity ->
                entity is Player || entity is Tameable
            })
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
        val victim = event.entity
        var attacker: Player? = null
        for ((player, affectedEntities) in lastBedInteraction) {
            if (!affectedEntities.contains(victim)) continue
            attacker = player
        }
        if (isPvpValid(attacker, victim)) return
        event.isCancelled = true
        makeAndSendMessage((attacker as Entity), victim, "explosion_pvp_disabled")
    }

    @EventHandler
    fun onRespawnAnchorExplodesPlayer(event: EntityDamageByBlockEvent) {
        if (event.cause != DamageCause.BLOCK_EXPLOSION) return
        if (event.damagerBlockState?.blockData !is org.bukkit.block.data.type.RespawnAnchor) return
        val victim = event.entity
        var attacker: Player? = null
        for ((player, affectedEntities) in lastRespawnAnchorInteraction) {
            if (!affectedEntities.contains(victim)) continue
            attacker = player
        }
        if (isPvpValid(attacker, victim)) return
        event.isCancelled = true
        makeAndSendMessage((attacker as Entity), victim, "explosion_pvp_disabled")
    }

    @EventHandler
    fun onPotionSplash(event: PotionSplashEvent) {
        val thrower = event.entity.shooter as? Player ?: return
        if (!isNegativePotion(event.potion.potionMeta)) return
        for (affectedEntity in event.affectedEntities) {
            if (affectedEntity !is Player && affectedEntity !is Tameable) continue
            if (affectedEntity == thrower) continue
            if (isPvpValid(thrower, affectedEntity)) continue
            event.setIntensity(affectedEntity, 0.0)
            makeAndSendMessage(thrower, affectedEntity, "potion_pvp_disabled")
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
            if (affectedEntity !is Player && affectedEntity !is Tameable) continue
            if (isPvpValid(thrower, affectedEntity)) continue
            event.affectedEntities.remove(affectedEntity)
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
    
    /**
     * Track TNT when it's primed by a player
     */
    @EventHandler
    fun onTNTPrimed(event: ExplosionPrimeEvent) {
        val entity = event.entity
        if (entity !is TNTPrimed && entity !is ExplosiveMinecart) return
        
        // Get the entity that ignited the TNT
        val source = when (entity) {
            is TNTPrimed -> entity.source as? Player
            is ExplosiveMinecart -> {
                // For TNT minecarts, we need to check the persistent data
                val sourceUUID = entity.persistentDataContainer.get(
                    NamespacedKey(plugin, "placer"),
                    PersistentDataType.STRING
                )
                if (sourceUUID != null) {
                    plugin.server.getPlayer(java.util.UUID.fromString(sourceUUID))
                } else null
            }
            else -> null
        }
        
        if (source != null) {
            tntOwners[entity] = source
            
            // Remove the entity from the map after a delay (in case the explosion doesn't happen)
            plugin.server.regionScheduler.runDelayed(plugin, entity.location, Consumer {_: ScheduledTask ->
                tntOwners.remove(entity)
            }, 100L) // 5 seconds (100 ticks)
        }
    }
    
    /**
     * Track TNT minecarts when placed by a player
     */
    @EventHandler
    fun onTNTMinecartPlaced(event: org.bukkit.event.entity.EntityPlaceEvent) {
        val player = event.player ?: return
        val vehicle = event.entity
        if (vehicle !is ExplosiveMinecart) return
        vehicle.persistentDataContainer.set(NamespacedKey(plugin, "placer"), PersistentDataType.STRING, player.uniqueId.toString())
    }
    
    
    /**
     * Handle damage from TNT and TNT minecarts
     */
    @EventHandler
    fun onExplosionDamage(event: EntityDamageByEntityEvent) {
        if (event.cause != DamageCause.ENTITY_EXPLOSION) return
        val victim = event.entity
        // Check if the damage source is TNT or TNT minecart
        val damageSource = event.damageSource.directEntity
        if (damageSource !is TNTPrimed && damageSource !is ExplosiveMinecart) return
        // Get the player who placed/ignited the TNT
        val attacker = tntOwners[damageSource]
        // If we couldn't determine who placed the TNT, allow the damage
        if (attacker == null) return
        // Check if PvP is valid between these players
        if (isPvpValid(attacker, victim)) return
        // Cancel the damage and send a message
        event.isCancelled = true
        makeAndSendMessage(attacker, victim, "potion_pvp_disabled")
    }
}
