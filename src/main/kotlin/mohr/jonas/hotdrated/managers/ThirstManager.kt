package mohr.jonas.hotdrated.managers

import mohr.jonas.hotdrated.StayHotdrated.Companion.CONFIG
import mohr.jonas.hotdrated.db.DataManager
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerRespawnEvent

object ThirstManager : Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        DataManager.thirst.setPlayerThirst(
            event.player.uniqueId,
            (DataManager.thirst.getPlayerThirst(event.player.uniqueId) - ((event.block.type.hardness / 200) / CONFIG.thirst.blockBreakDivisor)).coerceAtLeast(0.0)
        )
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        DataManager.thirst.setPlayerThirst(event.player.uniqueId, 20.0)
    }

    @EventHandler
    fun onDamageTaken(event: EntityDamageEvent) {
        if (event.entity.type != EntityType.PLAYER) return
        DataManager.thirst.setPlayerThirst(
            (event.entity as Player).uniqueId,
            (DataManager.thirst.getPlayerThirst((event.entity as Player).uniqueId) - ((event.finalDamage / 15)) / CONFIG.thirst.damageDivisor).coerceAtLeast(0.0)
        )
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        DataManager.thirst.setPlayerThirst(
            event.player.uniqueId,
            (DataManager.thirst.getPlayerThirst(event.player.uniqueId) - ((event.from.distance(event.to) / 200) / CONFIG.thirst.moveDivisor)).coerceAtLeast(0.0)
        )
    }

    @EventHandler
    fun onWaterDrink(event: PlayerItemConsumeEvent) {
        if (event.item.type != Material.POTION) return
        DataManager.thirst.setPlayerThirst(
            event.player.uniqueId,
            (DataManager.thirst.getPlayerThirst(event.player.uniqueId) + 6.0 / CONFIG.thirst.waterDivisor).coerceAtMost(20.0)
        )
    }
}