package mohr.jonas.hotdrated.data.lootdrop

import kotlinx.serialization.Serializable
import mohr.jonas.hotdrated.StayHotdrated
import org.bukkit.World

@Serializable
data class BiomeConfig(val list: List<String>, val isWhitelist: Boolean)

@Serializable
data class Location(val x: Int, val y: Int, val z: Int) {
    fun toBukkitLocation() =
        org.bukkit.Location(StayHotdrated.PLUGIN.server.worlds.find { it.environment == World.Environment.NORMAL }!!, x.toDouble(), y.toDouble(), z.toDouble())
}

@Serializable
data class ChestConfig(val relativeLocation: Location, val replaceWith: String)

@Serializable
data class StructureConfig(val name: String, val biomes: BiomeConfig? = null, val chests: List<ChestConfig>, val mobSpawnLocation: Location)
