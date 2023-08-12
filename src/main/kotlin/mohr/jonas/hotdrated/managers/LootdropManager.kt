package mohr.jonas.hotdrated.managers

import com.jeff_media.customblockdata.CustomBlockData
import kotlinx.serialization.json.Json
import mohr.jonas.hotdrated.*
import mohr.jonas.hotdrated.StayHotdrated.Companion.CONFIG
import mohr.jonas.hotdrated.data.lootdrop.ChestRarity
import mohr.jonas.hotdrated.data.lootdrop.ChestSize
import mohr.jonas.hotdrated.data.lootdrop.ChestType
import mohr.jonas.hotdrated.data.lootdrop.StructureConfig
import mohr.jonas.hotdrated.db.DataManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.World.Environment
import org.bukkit.block.Container
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
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
        val folder = StayHotdrated.PLUGIN.dataFolder.toPath().resolve("lootdrops")
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
        Bukkit.getScheduler().scheduleSyncDelayedTask(StayHotdrated.PLUGIN, {
            performTimedLootdrop()
        }, Random.nextLong(CONFIG.lootdrop.dropMinDelay, CONFIG.lootdrop.dropMaxDelay))
    }

    private fun performTimedLootdrop() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(StayHotdrated.PLUGIN, {
            performLootdrop()
            performTimedLootdrop()
        }, Random.nextLong(CONFIG.lootdrop.dropMinDelay, CONFIG.lootdrop.dropMaxDelay))
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        //TODO change lore to something proper
        if (event.itemInHand.lore()?.firstOrNull()?.let { it as TextComponent }?.content() != CONFIG.lootdrop.blockLore) return
        event.block.world.setType(event.block.location, Material.AIR)
        event.block.world.getBlockAt(event.block.location).state.update()
        performLootdrop(event.block.location)
    }

    @EventHandler
    fun onChestOpen(event: PlayerInteractEvent) {
        if (event.clickedBlock?.type != Material.CHEST) return
        val dataContainer = CustomBlockData(event.clickedBlock!!, StayHotdrated.PLUGIN)
        if (!dataContainer.has(NamespacedKey("rumblecraft", "is_lootdrop_chest"))) return
        if (dataContainer[NamespacedKey("rumblecraft", "is_lootdrop_chest"), PersistentDataType.BOOLEAN]!!) {
            dataContainer[NamespacedKey("rumblecraft", "is_lootdrop_chest"), PersistentDataType.BOOLEAN] = false
            event.player.giveExpLevels(CONFIG.lootdrop.chestOpenXp)
        }
    }

    private fun performLootdrop(preciseLocation: Location? = null) {
        val players = StayHotdrated.PLUGIN.server.onlinePlayers.filter { it.world.environment == Environment.NORMAL }
        val centerX = players.sumOf { it.location.x } / players.size
        val centerZ = players.sumOf { it.location.z } / players.size
        val positionX =
            (centerX + Random.nextInt(
                ((-CONFIG.lootdrop.dropRadius + 50 * DataManager.developmentLevel.get()).roundToInt()),
                (CONFIG.lootdrop.dropRadius + 50 * DataManager.developmentLevel.get()).roundToInt()
            )).roundToInt()
        val positionZ =
            (centerZ + Random.nextInt(
                ((-CONFIG.lootdrop.dropRadius + 50 * DataManager.developmentLevel.get()).roundToInt()),
                (CONFIG.lootdrop.dropRadius + 50 * DataManager.developmentLevel.get()).roundToInt()
            )).roundToInt()
        val world = StayHotdrated.PLUGIN.server.worlds.find { it.environment == Environment.NORMAL }!!
        val positionY = (310 downTo 0).firstOrNull { !world.getBlockAt(positionX, it, positionZ).isEmpty }!! + 1
        val biome = world.getBiome(positionX, positionY, positionZ)
        val config = Random.choice(structures.filter { config ->
            if (config.biomes == null) true
            else
                config.biomes.list.contains(biome.key.key).let { if (config.biomes.isWhitelist) it else it.not() }
        }.peek())
        val structure = Bukkit.getStructureManager().getStructure(NamespacedKey("rumblecraft", config.name))!!
        val location = preciseLocation ?: Location(world, positionX.toDouble(), positionY.toDouble(), positionZ.toDouble())
        structure.place(
            location,
            true,
            StructureRotation.NONE,
            Mirror.NONE,
            -1,
            ThreadLocalRandom.current().nextFloat(CONFIG.lootdrop.maxDestruction, CONFIG.lootdrop.minDestruction),
            ThreadLocalRandom.current()
        )
        println("placed structure at $location")
        config.chests
            .filter { Random.nextBoolean() }
            .forEach { chest ->
                val chestLocation = location.clone().add(chest.relativeLocation.toBukkitLocation())
                if (world.getBlockAt(chestLocation).type != Material.CHEST) return@forEach
                val container = world.getBlockAt(chestLocation).state as Container
                //TODO use rarity
                val rarity = Random.weightedChoice(
                    ChestRarity.entries,
                    listOf(
                        CONFIG.lootdrop.garbageChance,
                        CONFIG.lootdrop.commonChance,
                        CONFIG.lootdrop.uncommonChance,
                        CONFIG.lootdrop.rareChance,
                        CONFIG.lootdrop.epicChance,
                        CONFIG.lootdrop.legendaryChance,
                        CONFIG.lootdrop.mysticalChance
                    )
                )
                val size = Random.weightedChoice(ChestSize.entries, listOf(CONFIG.lootdrop.smallChance, CONFIG.lootdrop.mediumChance, CONFIG.lootdrop.largeChance))
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
        Random.weightedChoice(
            listOf(LootdropManager::createBigChallenge, LootdropManager::createSmallChallenge, null),
            listOf(CONFIG.lootdrop.bigChallengeChance, CONFIG.lootdrop.smallChallengeChance, 100 - (CONFIG.lootdrop.bigChallengeChance + CONFIG.lootdrop.smallChallengeChance))
        )
            ?.invoke(location.clone().add(config.mobSpawnLocation.toBukkitLocation()))
        announceDrop(location, players)
    }

    private fun createSmallChallenge(location: Location) {
        repeat(Random.nextInt(5, 10 + (DataManager.developmentLevel.get() / 10).roundToInt())) {
            MobCreator.spawnMob(
                location,
                Random.choice(listOf(EntityType.SKELETON, EntityType.ZOMBIE, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.PILLAGER, EntityType.ENDERMAN))
            ) {
                it.removeWhenFarAway = false
            }
        }
    }

    private fun createBigChallenge(location: Location) {
        repeat(Random.nextInt(10, 15 + (DataManager.developmentLevel.get() / 10).roundToInt())) {
            MobCreator.spawnMob(location, Random.choice(listOf(EntityType.MAGMA_CUBE, EntityType.WITHER_SKELETON, EntityType.SLIME, EntityType.PHANTOM))) {
                it.removeWhenFarAway = false
            }
        }
    }

    private fun announceDrop(location: Location, players: List<Player>) {
        players.forEach {
            it.sendTitle("Lootdrop landed at [${location.x} ${location.y} ${location.z}]", null, 10, 120, 20)
            it.playSound(it, Sound.ITEM_GOAT_HORN_SOUND_0, 500f, 1f)
            if (it.isOp) {
                val coordinateComponent = Component.text("[${location.x} ${location.y} ${location.z}]").color(NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.runCommand("/tp @p ${location.x} ${location.y} ${location.z}"))
                val message = Component.text("Lootdrop landed at ").color(NamedTextColor.AQUA).append(coordinateComponent)
                it.sendMessage(message)
            }
        }
    }
}