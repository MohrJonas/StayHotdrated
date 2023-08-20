package mohr.jonas.hotdrated.managers

import mohr.jonas.hotdrated.StayHotdrated
import mohr.jonas.hotdrated.StayHotdrated.Companion.CONFIG
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerTeleportEvent
import kotlin.random.Random

object RespawnManager : Listener {

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        if (event.player.bedSpawnLocation != null) return
        val overworld = StayHotdrated.PLUGIN.server.worlds.find { it.environment == World.Environment.NORMAL }!!
        val positionX =
            CONFIG.respawn.respawnCenter.x + Random.nextInt(-CONFIG.respawn.respawnRadius, CONFIG.respawn.respawnRadius)
        val positionZ =
            CONFIG.respawn.respawnCenter.z + Random.nextInt(-CONFIG.respawn.respawnRadius, CONFIG.respawn.respawnRadius)
        val positionY = (310 downTo 0).firstOrNull { !overworld.getBlockAt(positionX, it, positionZ).isEmpty }!! + 1
        event.player.teleport(Location(overworld, positionX.toDouble(), positionY.toDouble(), positionZ.toDouble()), PlayerTeleportEvent.TeleportCause.PLUGIN)
    }
}