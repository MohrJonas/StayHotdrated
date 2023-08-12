package mohr.jonas.hotdrated.data

import kotlinx.serialization.Serializable
import mohr.jonas.hotdrated.data.lootdrop.Location
import mohr.jonas.hotdrated.inTicks
import kotlin.time.Duration.Companion.minutes

@Serializable
data class XpConfig(
    val mineXpChance: Int = 30,
    val mineXpAmount: Int = 1,
    val forageXpChance: Int = 50,
    val forageXpAmount: Int = 3,
    val farmXpChance: Int = 25,
    val farmXpAmount: Int = 3,
    val monsterKillXpChance: Int = 100,
    val monsterKillXpAmount: Int = 3,
    val mobKillXpChance: Int = 100,
    val mobKillXpAmount: Int = 1,
    val wageAmount: Int = 180
)

@Serializable
data class LootdropConfig(
    val dropMinDelay: Long = 5.minutes.inTicks.toLong(),
    val dropMaxDelay: Long = 10.minutes.inTicks.toLong(),
    val blockLore: String = "Lootdrop",
    val chestOpenXp: Int = 100,
    val dropRadius: Int = 100,
    val maxDestruction: Float = 0f,
    val minDestruction: Float = 1f,
    val garbageChance: Int = 45,
    val commonChance: Int = 28,
    val uncommonChance: Int = 12,
    val rareChance: Int = 8,
    val epicChance: Int = 4,
    val legendaryChance: Int = 2,
    val mysticalChance: Int = 1,
    val smallChance: Int = 50,
    val mediumChance: Int = 38,
    val largeChance: Int = 12,
    val smallChallengeChance: Int = 60,
    val bigChallengeChance: Int = 20
)

@Serializable
data class RespawnConfig(val respawnCenter: Location = Location(0, 0, 0), val respawnRadius: Int = 100)

@Serializable
data class TemperatureConfig(
    val baseMultiplier: Double = 1.0,
    val waterMultiplier: Double = 1.0,
    val movementMultiplier: Double = 1.0,
    val weatherMultiplier: Double = 1.0,
    val blockMultiplier: Double = 1.0,
    val armorMultiplier: Double = 1.0,
    val maxAcceptableTemperature: Double = 30.0,
    val minAcceptableTemperature: Double = 0.0,
    val leatherAddend: Double = 2.5,
    val ironAddend: Double = -3.75,
    val goldAddend: Double = 3.75,
    val diamondAddend: Double = -2.5,
    val netheriteAddend: Double = -1.25,
    val chainmailAddend: Double = 1.25
)

@Serializable
data class ThirstConfig(
    val blockBreakDivisor: Double = 1.0,
    val damageDivisor: Double = 1.0,
    val moveDivisor: Double = 1.0,
    val waterDivisor: Double = 1.0
)

@Serializable
data class PotionEffect(val key: String, val amplifier: Int)

@Serializable
data class PowerScale(val minPowerLevel: Double, val maxPowerLevel: Double, val effects: List<PotionEffect>)

@Serializable
data class MobConfig(
    val powerScale: List<PowerScale> = emptyList()
)

@Serializable
data class PluginConfig(
    val xp: XpConfig = XpConfig(),
    val lootdrop: LootdropConfig = LootdropConfig(),
    val respawn: RespawnConfig = RespawnConfig(),
    val temperature: TemperatureConfig = TemperatureConfig(),
    val mob: MobConfig = MobConfig(),
    val thirst:ThirstConfig = ThirstConfig()
)
