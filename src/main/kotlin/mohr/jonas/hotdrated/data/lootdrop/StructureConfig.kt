package mohr.jonas.hotdrated.data.lootdrop

import kotlinx.serialization.Serializable

@Serializable
data class BiomeConfig(val list: List<String>, val isWhitelist: Boolean)

@Serializable
data class Location(val x: Int, val y: Int, val z: Int)

@Serializable
data class ChestConfig(val relativeLocation: Location, val replaceWith: String)

@Serializable
data class StructureConfig(val name: String, val biomes: BiomeConfig? = null, val chests: List<ChestConfig>)
