package mohr.jonas.hotdrated

import com.jeff_media.customblockdata.CustomBlockData
import kotlinx.serialization.json.Json
import mohr.jonas.hotdrated.data.PluginConfig
import mohr.jonas.hotdrated.db.DataManager
import mohr.jonas.hotdrated.managers.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Database
import java.io.File
import java.nio.file.Files
import java.util.logging.Level
import kotlin.properties.Delegates
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@Suppress("unused")
class StayHotdrated : JavaPlugin(), Listener {

    private var showTemperature = true
    private var schedulerId by Delegates.notNull<Int>()

    override fun onEnable() {
        println("Enabled")
        logger.level = Level.ALL
        PLUGIN = this
        val configPath = this.dataFolder.toPath().resolve("config.json")
        CONFIG = if (!Files.exists(configPath))
            PluginConfig()
        else
            Json.decodeFromString(Files.readString(configPath))
        //TODO switch later in production
        Database.connect("jdbc:sqlite:${File("database.db").absolutePath}", "org.sqlite.JDBC")
        CustomBlockData.registerListener(this)
        server.pluginManager.registerEvents(this, this)
        server.pluginManager.registerEvents(TemperatureManager, this)
        server.pluginManager.registerEvents(ThirstManager, this)
        server.pluginManager.registerEvents(LootdropManager, this)
        server.pluginManager.registerEvents(ExperienceManager, this)
        server.pluginManager.registerEvents(RespawnManager, this)
        server.pluginManager.registerEvents(BountyManager, this)
        server.pluginManager.registerEvents(CurrencyManager, this)
        server.pluginManager.registerEvents(EffectBlockManager, this)
        DataManager.developmentLevel.set(DataManager.developmentLevel.recalculate(this))
        schedulerId =
            Bukkit.getScheduler()
                .scheduleSyncRepeatingTask(
                    this,
                    {
                        server.onlinePlayers
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

    override fun onDisable() {
        println("Disabled")
        Bukkit.getScheduler().cancelTask(schedulerId)
        DataManager.currency.commitToDB()
        DataManager.temperature.commitToDB()
        DataManager.thirst.commitToDB()
        DataManager.bounty.commitToDB()
        DataManager.effectBlock.commitToDB()
    }

    companion object {
        lateinit var PLUGIN: StayHotdrated
        lateinit var CONFIG: PluginConfig
    }
}

