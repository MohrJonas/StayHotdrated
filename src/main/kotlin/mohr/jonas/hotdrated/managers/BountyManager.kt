package mohr.jonas.hotdrated.managers

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import mohr.jonas.hotdrated.StayHotdrated
import mohr.jonas.hotdrated.db.DataManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

object BountyManager : Listener {

    data class Bounty(val xp: Int, val diamonds: Int)

    @EventHandler
    fun onSunrise(event: ServerTickEndEvent) {
        if (StayHotdrated.PLUGIN.server.worlds.find { it.environment == World.Environment.NORMAL }!!.time != 1L) return
        StayHotdrated.PLUGIN.server.onlinePlayers.forEach {
            DataManager.bounty.setOnlineDays(it.uniqueId, DataManager.bounty.getOnlineDays(it.uniqueId) + 1)
        }
        StayHotdrated.PLUGIN.server.offlinePlayers.forEach {
            DataManager.bounty.setOfflineDays(it.uniqueId, DataManager.bounty.getOfflineDays(it.uniqueId) + 1)
        }
    }

    @EventHandler
    fun onPlayerKill(event: EntityDeathEvent) {
        if (event.entity is Player) {
            DataManager.bounty.setOnlineDays(event.entity.uniqueId, 0)
            DataManager.bounty.setOfflineDays(event.entity.uniqueId, 0)
            if (event.entity.killer is Player) {
                val bounty = calculateBounty(event.entity.killer!!)
                event.entity.killer!!.sendMessage(
                    Component.text("Bounty completed. Your reward: ").color(NamedTextColor.RED)
                        .append(Component.text("XP: ${bounty.xp}, Diamonds: ${bounty.diamonds}").color(NamedTextColor.GOLD))
                )
            }
        }
    }

    private fun calculateBounty(player: Player): Bounty {
        return Bounty(1, 1)
    }

}