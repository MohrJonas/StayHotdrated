package mohr.jonas.hotdrated.managers

import com.jeff_media.customblockdata.CustomBlockData
import kotlinx.serialization.json.Json
import mohr.jonas.hotdrated.StayHotdrated
import mohr.jonas.hotdrated.choice
import mohr.jonas.hotdrated.data.lootdrop.ChestRarity
import mohr.jonas.hotdrated.data.lootdrop.ChestSize
import mohr.jonas.hotdrated.data.lootdrop.ChestType
import mohr.jonas.hotdrated.data.lootdrop.StructureConfig
import mohr.jonas.hotdrated.db.DataManager
import mohr.jonas.hotdrated.peek
import mohr.jonas.hotdrated.weightedChoice
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World.Environment
import org.bukkit.block.Container
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.loot.LootContext
import org.bukkit.persistence.PersistentDataType
import java.nio.file.Files
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.io.path.extension
import kotlin.math.roundToInt
import kotlin.random.Random

object LootdropManager : org.bukkit.event.Listener {

    private val structures: List<StructureConfig>

    init {
        val folder = StayHotdrated.PLUGIN.dataFolder.toPath()
        Files.createDirectories(folder)
        structures = Files.list(folder)
            .filter { it.extension == "json" }
            .map { Json.decodeFromString<StructureConfig>(Files.readString(it)) }
            .peek {
                Objects.requireNonNull(
                    Bukkit.getStructureManager().loadStructure(NamespacedKey("rumblecraft", it.name)),
                    "Unable to find structure rumblecraft:${it.name}"
                )
            }
            .toList()
        println("Loaded ${structures.size} lootdrop structure(s)")
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        //TODO change lore to something proper
        if (event.itemInHand.lore()?.firstOrNull()?.let { it as TextComponent }?.content() != "TestLore") return
        event.block.world.setType(event.block.location, Material.AIR)
        event.block.world.getBlockAt(event.block.location).state.update()
        performLootdrop()
    }

    @EventHandler
    fun onChestOpen(event: PlayerInteractEvent) {
        if (event.clickedBlock?.type != Material.CHEST) return
        val dataContainer = CustomBlockData(event.clickedBlock!!, StayHotdrated.PLUGIN)
        if (!dataContainer.has(NamespacedKey("rumblecraft", "is_lootdrop_chest"))) return
        if (dataContainer[NamespacedKey("rumblecraft", "is_lootdrop_chest"), PersistentDataType.BOOLEAN]!!) {
            dataContainer[NamespacedKey("rumblecraft", "is_lootdrop_chest"), PersistentDataType.BOOLEAN] = false
            event.player.giveExpLevels(100)
        }
    }

    private fun performLootdrop() {
        val players = StayHotdrated.PLUGIN.server.onlinePlayers.filter { it.world.environment == Environment.NORMAL }
        val centerX = players.sumOf { it.location.x } / players.size
        val centerZ = players.sumOf { it.location.z } / players.size
        val positionX =
            (centerX + Random.nextInt(((-200 * DataManager.developmentLevel.get()).roundToInt()), (100 + 50 * DataManager.developmentLevel.get()).roundToInt())).roundToInt()
        val positionZ =
            (centerZ + Random.nextInt(((-200 * DataManager.developmentLevel.get()).roundToInt()), (100 + 50 * DataManager.developmentLevel.get()).roundToInt())).roundToInt()
        val world = StayHotdrated.PLUGIN.server.worlds.find { it.environment == Environment.NORMAL }!!
        val positionY = (310 downTo 0).firstOrNull { !world.getBlockAt(positionX, it, positionZ).isEmpty }!!
        val biome = world.getBiome(positionX, positionY, positionZ)
        val config = Random.choice(structures.filter { config ->
            if (config.biomes == null) true
            else
                config.biomes.list.contains(biome.key.key).let { if (config.biomes.isWhitelist) it else it.not() }
        }.peek())
        val structure = Bukkit.getStructureManager().getStructure(NamespacedKey("rumblecraft", config.name))!!
        val location = Location(world, positionX.toDouble(), positionY.toDouble(), positionZ.toDouble())
        structure.place(location, true, StructureRotation.NONE, Mirror.NONE, -1, ThreadLocalRandom.current().nextFloat(), ThreadLocalRandom.current())
        println("placed structure at $location")
        config.chests
            .filter { Random.nextBoolean() }
            .forEach { chest ->
                val chestLocation = location.clone().add(chest.relativeLocation.x.toDouble(), chest.relativeLocation.y.toDouble(), chest.relativeLocation.z.toDouble())
                if (world.getBlockAt(chestLocation).type != Material.CHEST) return@forEach
                val container = world.getBlockAt(chestLocation).state as Container
                //TODO use rarity
                val rarity = Random.weightedChoice(ChestRarity.entries, listOf(45, 28, 12, 8, 4, 2, 1))
                val size = Random.weightedChoice(ChestSize.entries, listOf(50, 38, 12))
                val type = Random.choice(ChestType.entries)
                val lootTable = Bukkit.getLootTable(NamespacedKey("rumblecraft", type.lootTableName))!!
                repeat(size.multiplier) {
                    container.inventory.addItem(
                        *lootTable.populateLoot(
                            ThreadLocalRandom.current(),
                            LootContext.Builder(chestLocation).lootingModifier(LootContext.DEFAULT_LOOT_MODIFIER).build()
                        ).toTypedArray()
                    )
                }
                val dataContainer = CustomBlockData(world.getBlockAt(chestLocation), StayHotdrated.PLUGIN)
                dataContainer[NamespacedKey("rumblecraft", "is_lootdrop_chest"), PersistentDataType.BOOLEAN] = true
            }
        repeat(Random.nextInt(0, 10)) {
            world.spawnEntity(
                location.clone().add(Random.nextInt(-5, 6).toDouble(), 3.0, Random.nextInt(-5, 6).toDouble()),
                Random.choice(listOf(EntityType.SKELETON, EntityType.ZOMBIE, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.PILLAGER, EntityType.ENDERMAN))
            )
        }
    }
}