package mohr.jonas.hotdrated.managers

import kotlinx.serialization.json.Json
import mohr.jonas.hotdrated.StayHotdrated
import mohr.jonas.hotdrated.data.temperature.BiomeData
import mohr.jonas.hotdrated.db.DataManager
import mohr.jonas.hotdrated.mapToRange
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

object TemperatureManager : Listener {

    private val biomeData: Array<BiomeData>

    init {
        val reader = StayHotdrated.PLUGIN.getResource("biomes.json")!!.reader()
        val rawJson = reader.readText()
        reader.close()
        biomeData = Json.decodeFromString(rawJson)
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        DataManager.temperature.setPlayerMoveStacks(
            event.player.uniqueId,
            (DataManager.temperature.getPlayerMoveStacks(event.player.uniqueId) + event.to.distance(event.from)).coerceAtMost(200.0)
        )
    }

    fun getPlayerTemperature(player: Player): Double {
        DataManager.temperature.setPlayerMoveStacks(player.uniqueId, (DataManager.temperature.getPlayerMoveStacks(player.uniqueId) - 20.0).coerceAtLeast(0.0))
        return player.getBaseTemperature()
            // Apply height modifier
            .let {
                println("===============================")
                println("Base temperature: $it")
                val max = player.world.maxHeight.toDouble()
                val min = player.world.minHeight.toDouble()
                it + player.location.y.mapToRange(min..max, -15.0..15.0)
            }
            // Apply water modifier
            .let {
                println("w/ Height modifier: $it")
                if (player.isInWater) it - it / 4.0 else it
            }
            // Apply movement modifier
            .let {
                println("w/ Water modifier: $it")
                it + DataManager.temperature.getPlayerMoveStacks(player.uniqueId).mapToRange(0.0..200.0, 0.0..15.0)
            }
            // Apply weather modifier
            .let {
                println("w/ Movement modifier: $it")
                if (player.isInRain) it - it / 8.0 else it
            }
            // Apply heat blocks modifier
            .let {
                println("w/ Rain modifier: $it")
                it + player.calculateBlockTemperatureOffset()
            }
            // Apply armor modifier
            .let {
                println("w/ Block modifier: $it")
                it + player.calculateArmorTemperatureOffset()
            }.also {
                println("w/ Armor modifier: $it")
                println("===============================")
            }
    }

    private fun Player.getBaseTemperature() = when (this.world.environment) {
        World.Environment.THE_END -> -30.0
        World.Environment.NETHER -> 30.0
        World.Environment.CUSTOM -> 0.0
        World.Environment.NORMAL -> {
            val baseTemp = TemperatureManager[player!!.world.getBiome(player!!.location)]
            if (this.world.isDayTime) {
                baseTemp
            } else {
                if (baseTemp >= 0) baseTemp / 2 else baseTemp * 2
            }
        }
    }

    private fun Player.calculateArmorTemperatureOffset(): Double {
        fun getItemOffset(material: Material?) = when (material) {
            Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS -> -2.5
            Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS -> 3.75
            Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS -> 2.5
            Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS -> -3.75
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS -> -1.25
            Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS -> 1.25
            else -> 0.0
        }
        return arrayOf(
            getItemOffset(this.inventory.helmet?.type),
            getItemOffset(this.inventory.chestplate?.type),
            getItemOffset(this.inventory.leggings?.type),
            getItemOffset(this.inventory.boots?.type)
        ).sum()
    }

    private fun Player.calculateBlockTemperatureOffset() =
        this.location.block.lightFromBlocks.toDouble()

    private operator fun get(biome: Biome): Double = biomeData.find { it.name == biome.name.lowercase() }!!.temperature

}