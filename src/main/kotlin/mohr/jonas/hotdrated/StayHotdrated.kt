package mohr.jonas.hotdrated

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingCompletedEvent
import com.jeff_media.customblockdata.CustomBlockData
import kotlinx.serialization.json.Json
import mohr.jonas.hotdrated.data.PluginConfig
import mohr.jonas.hotdrated.data.advancements.Advancements
import mohr.jonas.hotdrated.db.DataManager
import mohr.jonas.hotdrated.managers.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Database
import java.io.File
import java.nio.file.Files

@Suppress("unused")
class StayHotdrated : JavaPlugin(), Listener {

    private lateinit var main: AdvancementMain

    override fun onEnable() {
        PLUGIN = this
        loadConfig()
        //TODO switch later in production
        Database.connect("jdbc:sqlite:${File("database.db").absolutePath}", "org.sqlite.JDBC")
        //TODO switch to db in production
        //main.enableSQLite(File("database.db"))
        registerManagers()
        DataManager.developmentLevel.set(DataManager.developmentLevel.recalculate(this))
        main.enableInMemory()
        ADVANCEMENT_API = UltimateAdvancementAPI.getInstance(this)
    }

    override fun onLoad() {
        main = AdvancementMain(this)
        main.load()
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerLoadingCompletedEvent) {
        Advancements.forEach {
            it.showTab(e.player)
            it.grantRootAdvancement(e.player)
            //TODO remove in production
            it.advancements.forEach { it.grant(e.player) }
        }
    }

    override fun onDisable() {
        main.disable()
        DataManager.commitData()
    }

    private fun loadConfig() {
        val configPath = this.dataFolder.toPath().resolve("config.json")
        CONFIG = if (!Files.exists(configPath))
            PluginConfig()
        else
            Json.decodeFromString(Files.readString(configPath))
    }

    private fun registerManagers() {
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
    }

    companion object {
        lateinit var PLUGIN: StayHotdrated
        lateinit var CONFIG: PluginConfig
        lateinit var ADVANCEMENT_API: UltimateAdvancementAPI
    }
}

