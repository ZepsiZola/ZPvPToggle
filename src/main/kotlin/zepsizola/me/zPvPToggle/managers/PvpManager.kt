package zepsizola.me.zPvPToggle.managers

import org.bukkit.entity.Player
import zepsizola.me.zPvPToggle.ZPvPToggle
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import zepsizola.me.zPvPToggle.data.DatabaseManager
import zepsizola.me.zPvPToggle.data.DatabaseManagerImpl

data class PlayerState(
    var pvpEnabled: Boolean = false,
    var canSeeIndicators: Boolean = true,  // Whether this player can see indicators of other players
    var canSeeOwnIndicator: Boolean = true,  // Whether this player can see their own indicator
    var firstToggleOnThisSession: Boolean = true,
    var indicatorRingId: String = "default"  // The ID of the particle ring to use for this player
)

class PvpManager(private val plugin: ZPvPToggle) {

    private val playerStates = ConcurrentHashMap<UUID, PlayerState>()
    
    // Cache of players with PvP enabled
    private val pvpEnabledPlayers = ConcurrentHashMap<UUID, Player>()
    
    lateinit var databaseManager: DatabaseManagerImpl

    init {
        // Register event listeners for player join/quit to manage the cache
        plugin.server.pluginManager.registerEvents(object : org.bukkit.event.Listener {
            @org.bukkit.event.EventHandler
            fun onPlayerJoin(event: org.bukkit.event.player.PlayerJoinEvent) {
                val player = event.player
                databaseManager.loadPlayerData(event.player)
                if (getState(player).pvpEnabled) {
                    pvpEnabledPlayers[player.uniqueId] = player
                }
            }
            
            @org.bukkit.event.EventHandler
            fun onPlayerQuit(event: org.bukkit.event.player.PlayerQuitEvent) {
                pvpEnabledPlayers.remove(event.player.uniqueId)
                removePlayerData(event.player.uniqueId)
            }
        }, plugin)

        databaseManager = DatabaseManagerImpl(plugin)
    }

    fun getState(player: Player): PlayerState {
        return playerStates.computeIfAbsent(player.uniqueId) { PlayerState() }
    }

    fun togglePvp(player: Player): Boolean {
        val state = getState(player)
        setPvp(player, !state.pvpEnabled)
        // state.pvpEnabled = !state.pvpEnabled
        // updatePvpEnabledCache(player, state.pvpEnabled)
        return state.pvpEnabled
    }

    fun setPvp(player: Player, enabled: Boolean) {
        val state = getState(player)
        state.pvpEnabled = enabled
        updatePvpEnabledCache(player, enabled)
    }

    fun isPvpEnabled(player: Player): Boolean {
        return getState(player).pvpEnabled
    }

    fun toggleIndicators(player: Player): Boolean {
        val state = getState(player)
        state.canSeeIndicators = !state.canSeeIndicators
        databaseManager.savePlayerIndicatorVisibility(player, state.canSeeIndicators)
        return state.canSeeIndicators
    }

    fun canSeeIndicators(player: Player): Boolean {
        val state = getState(player)
        return state.canSeeIndicators
    }

    fun toggleOwnIndicator(player: Player): Boolean {
        val state = getState(player)
        state.canSeeOwnIndicator = !state.canSeeOwnIndicator
        databaseManager.savePlayerOwnIndicatorVisibility(player, state.canSeeOwnIndicator)
        return state.canSeeOwnIndicator
    }

    fun canSeeOwnIndicator(player: Player): Boolean {
        val state = getState(player)
        return state.canSeeOwnIndicator
    }
    
    /**
     * Set the indicator ring for a player
     * @param player The player to set the ring for
     * @param ringId The ID of the ring to use
     */
    fun setIndicatorRing(player: Player, indicatorId: String) {
        val state = getState(player)
        databaseManager.savePlayerIndicator(player , indicatorId)
        state.indicatorRingId = indicatorId
    }
    
    /**
     * Get the indicator ring ID for a player
     * @param player The player to get the ring for
     * @return The ID of the ring
     */
    fun getIndicatorRingId(player: Player): String {
        return getState(player).indicatorRingId
    }

    fun isFirstToggleOnThisSession(player: Player): Boolean {
        val state = getState(player)
        val first = state.firstToggleOnThisSession
        if (first) {
            state.firstToggleOnThisSession = false
        }
        return first
    }

    fun removePlayerData(uuid: UUID) {
        playerStates.remove(uuid)
        pvpEnabledPlayers.remove(uuid)
    }
    
    /**
     * Updates the PvP status of a player in the cache.
     * Called whenever a player toggles their PvP status.
     */
    private fun updatePvpEnabledCache(player: Player, pvpEnabled: Boolean) {
        if (pvpEnabled) {
            pvpEnabledPlayers[player.uniqueId] = player
        } else {
            pvpEnabledPlayers.remove(player.uniqueId)
        }
    }
    
    /**
     * Gets all players who currently have PvP enabled.
     * Used by the particle task to know which players need indicators.
     */
    fun getPvpEnabledPlayers(): Collection<Player> {
        return pvpEnabledPlayers.values
    }
    
    /**
     * Initialize the PvP enabled players cache with currently online players.
     * Called when the plugin starts or reloads.
     */
    fun initializePvpEnabledCache() {
        pvpEnabledPlayers.clear()
        for (player in plugin.server.onlinePlayers) {
            if (getState(player).pvpEnabled) {
                pvpEnabledPlayers[player.uniqueId] = player
            }
        }
    }

    fun stop() {
        // // Save all player data to the database
        // for ((uuid, state) in playerStates) {
        //     databaseManager.savePlayerIndicator(uuid, state.indicatorRingId)
        //     databaseManager.savePlayerIndicatorVisibility(uuid, state.canSeeIndicators)
        //     databaseManager.savePlayerOwnIndicatorVisibility(uuid, state.canSeeOwnIndicator)
        // }
        // Close the database connection
        databaseManager.close()
    }
}
