package mohr.jonas.hotdrated.managers

import kotlinx.serialization.json.Json
import mohr.jonas.hotdrated.StayHotdrated
import mohr.jonas.hotdrated.StayHotdrated.Companion.CONFIG
import mohr.jonas.hotdrated.data.temperature.BiomeData
import mohr.jonas.hotdrated.data.temperature.TemperatureReading
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

    fun getPlayerTemperature(player: Player): TemperatureReading {
        DataManager.temperature.setPlayerMoveStacks(player.uniqueId, (DataManager.temperature.getPlayerMoveStacks(player.uniqueId) - 20.0).coerceAtLeast(0.0))
        val baseTemperature = player.getBaseTemperature() * CONFIG.temperature.baseMultiplier
        val waterTemperature = if (player.isInWater) baseTemperature - baseTemperature / (4.0 * CONFIG.temperature.waterMultiplier) else baseTemperature
        val movementTemperature =
            waterTemperature + (DataManager.temperature.getPlayerMoveStacks(player.uniqueId).mapToRange(0.0..200.0, 0.0..15.0) * CONFIG.temperature.movementMultiplier)
        val weatherTemperature = if (player.isInRain) movementTemperature - movementTemperature / (8.0 * CONFIG.temperature.weatherMultiplier) else movementTemperature
        val blockTemperature = waterTemperature + player.calculateBlockTemperatureOffset() * CONFIG.temperature.blockMultiplier
        val armorTemperature = blockTemperature + player.calculateArmorTemperatureOffset() * CONFIG.temperature.armorMultiplier
        return TemperatureReading(baseTemperature, waterTemperature, movementTemperature, weatherTemperature, blockTemperature, armorTemperature)
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
            Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS -> CONFIG.temperature.diamondAddend
            Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS -> CONFIG.temperature.goldAddend
            Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS -> CONFIG.temperature.leatherAddend
            Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS -> CONFIG.temperature.ironAddend
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS -> CONFIG.temperature.netheriteAddend
            Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS -> CONFIG.temperature.chainmailAddend
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