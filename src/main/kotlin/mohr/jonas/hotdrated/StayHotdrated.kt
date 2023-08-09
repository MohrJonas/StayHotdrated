package mohr.jonas.hotdrated

import com.jeff_media.customblockdata.CustomBlockData
import mohr.jonas.hotdrated.db.DataManager
import mohr.jonas.hotdrated.managers.LootdropManager
import java.io.File
import java.util.logging.Level
import kotlin.properties.Delegates
import kotlin.time.Duration.Companion.seconds
import mohr.jonas.hotdrated.managers.TemperatureManager
import mohr.jonas.hotdrated.managers.ThirstManager
import org.bukkit.*
import org.bukkit.Registry.SimpleRegistry
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Database

@Suppress("unused")
class StayHotdrated : JavaPlugin(), Listener {

    private var showTemperature = true
    private var schedulerId by Delegates.notNull<Int>()

    override fun onEnable() {
        println("Enabled")
        //println(Bukkit.getStructureManager().loadStructure(NamespacedKey("rumblecraft", "hihihi")))
        //Bukkit.getStructureManager().structures.forEach { (t, u) -> println("${t.key} => ${u.size}") }
        //Bukkit.getLootTable(NamespacedKey("rumblecraft", "test_table"))
        logger.level = Level.ALL
        PLUGIN = this
        Database.connect("jdbc:sqlite:${File("database.db").absolutePath}", "org.sqlite.JDBC")
        // Database.connect("jdbc:postgresql://0.0.0.0:5432/test", driver = "org.postgresql.Driver", user = "postgres", password = "my_pwd")
        CustomBlockData.registerListener(this)
        server.pluginManager.registerEvents(this, this)
        server.pluginManager.registerEvents(TemperatureManager, this)
        server.pluginManager.registerEvents(ThirstManager, this)
        server.pluginManager.registerEvents(LootdropManager, this)
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
                                                val temperature =
                                                        TemperatureManager.getPlayerTemperature(
                                                                player
                                                        )
                                                DataManager.temperature.setPlayerTemperature(player.uniqueId, temperature)
                                                println(
                                                        "${player.name}@$temperature => ${temperature.isAcceptableTemperature()}"
                                                )
                                                when (temperature) {
                                                    in -100.0..9.999 -> {
                                                        println(
                                                                "Applying hypothermia to ${player.name}"
                                                        )
                                                        player.applyHypothermia()
                                                    }
                                                    in 25.001..100.0 -> {
                                                        println(
                                                                "Applying hyperthermia to ${player.name}"
                                                        )
                                                        player.applyHyperthermia()
                                                    }
                                                }
                                                val thirst = ThirstManager.getPlayerThirst(player)
                                                if (thirst == 0.0) {
                                                    player.applyThirst()
                                                }
                                                if (showTemperature)
                                                        player.displayTemperature(temperature)
                                                else player.displayWater(thirst)
                                                showTemperature = showTemperature.not()
                                            }
                                },
                                5.seconds.inTicks.toLong(),
                                5.seconds.inTicks.toLong()
                        )
    }

    override fun onDisable() {
        Bukkit.getScheduler().cancelTask(schedulerId)
        println("Disabled")
    }

    @EventHandler
    fun onWorldLoad(event: PlayerJoinEvent) {
        if (event.player.world.environment != World.Environment.NORMAL) return
        println("Structure types: ")
        Registry.STRUCTURE_TYPE.forEach { println(it.key.key) }
    }

    companion object {
        lateinit var PLUGIN: StayHotdrated
    }
}

