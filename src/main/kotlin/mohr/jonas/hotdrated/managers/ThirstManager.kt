package mohr.jonas.hotdrated.managers

import mohr.jonas.hotdrated.*
import mohr.jonas.hotdrated.StayHotdrated.Companion.CONFIG
import mohr.jonas.hotdrated.db.DataManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.GameMode
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
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object ThirstManager : Listener {

    private var showTemperature = true

    init {
        Bukkit.getScheduler()
            .scheduleSyncRepeatingTask(
                StayHotdrated.PLUGIN,
                {
                    StayHotdrated.PLUGIN.server.onlinePlayers
                        .filter {
                            it.gameMode.let {
                                it != GameMode.CREATIVE &&
                                        it != GameMode.SPECTATOR
                            }
                        }
                        .forEach { player ->
                            val currentTemperature = DataManager.temperature.getPlayerTemperature(player.uniqueId)
                            val targetTemperature = TemperatureManager.getPlayerTemperature(player)
                            val newTemperature = if (currentTemperature > targetTemperature.armorTemperature)
                                (currentTemperature - 3.0).coerceAtLeast(targetTemperature.armorTemperature) else if (currentTemperature < targetTemperature.armorTemperature)
                                (currentTemperature + 3.0).coerceAtMost(targetTemperature.armorTemperature) else currentTemperature
                            DataManager.temperature.setPlayerTemperature(player.uniqueId, newTemperature)
                            println(
                                "${player.name}@$newTemperature => ${newTemperature.isAcceptableTemperature()}"
                            )
                            when (newTemperature) {
                                in -100.0..<5.0 -> {
                                    println(
                                        "Applying hypothermia to ${player.name}"
                                    )
                                    player.showTitle(
                                        Title.title(
                                            Component.text("It's really cold here").color(NamedTextColor.AQUA),
                                            Component.empty(),
                                            Title.Times.times(2.seconds.toJavaDuration(), 5.seconds.toJavaDuration(), 1.seconds.toJavaDuration())
                                        )
                                    )
                                    player.applyHypothermia()
                                }

                                in 30.1..100.0 -> {
                                    println(
                                        "Applying hyperthermia to ${player.name}"
                                    )
                                    player.showTitle(
                                        Title.title(
                                            Component.text("It's really hot here").color(NamedTextColor.RED),
                                            Component.empty(),
                                            Title.Times.times(2.seconds.toJavaDuration(), 5.seconds.toJavaDuration(), 1.seconds.toJavaDuration())
                                        )
                                    )
                                    player.applyHyperthermia()
                                }
                            }
                            val thirst = DataManager.thirst.getPlayerThirst(player.uniqueId)
                            if (thirst == 0.0) {
                                player.showTitle(
                                    Title.title(
                                        Component.text("I'm really thirsty").color(NamedTextColor.BLUE),
                                        Component.empty(),
                                        Title.Times.times(2.seconds.toJavaDuration(), 5.seconds.toJavaDuration(), 1.seconds.toJavaDuration())
                                    )
                                )
                                player.applyThirst()
                            }
                            if (showTemperature)
                                player.displayTemperature(newTemperature, targetTemperature)
                            else player.displayWater(thirst)
                            showTemperature = showTemperature.not()
                        }
                },
                3.seconds.inTicks.toLong(),
                3.seconds.inTicks.toLong()
            )
    }

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