package zepsizola.me.zPvPToggle.managers

import org.bukkit.entity.Player
import zepsizola.me.zPvPToggle.ZPvPToggle
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class PlayerState(
    var pvpEnabled: Boolean = false,
    var indicatorsEnabled: Boolean = true,
    var firstToggleOnThisSession: Boolean = true
)

class PvpManager(private val plugin: ZPvPToggle) {

    private val playerStates = ConcurrentHashMap<UUID, PlayerState>()

    fun getState(player: Player): PlayerState {
        return playerStates.computeIfAbsent(player.uniqueId) { PlayerState() }
    }

    fun togglePvp(player: Player): Boolean {
        val state = getState(player)
        state.pvpEnabled = !state.pvpEnabled
        return state.pvpEnabled
    }

    fun setPvp(player: Player, enabled: Boolean) {
        val state = getState(player)
        state.pvpEnabled = enabled
    }

    fun isPvpEnabled(player: Player): Boolean {
        return getState(player).pvpEnabled
    }

    fun toggleIndicators(player: Player): Boolean {
        val state = getState(player)
        state.indicatorsEnabled = !state.indicatorsEnabled
        return state.indicatorsEnabled
    }

    fun isIndicatorsEnabled(player: Player): Boolean {
        return getState(player).indicatorsEnabled
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
    }
}