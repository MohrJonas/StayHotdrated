package mohr.jonas.hotdrated

import mohr.jonas.hotdrated.StayHotdrated.Companion.CONFIG
import mohr.jonas.hotdrated.data.temperature.TemperatureReading
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

val Duration.inTicks: Int
    get() = (this.inWholeSeconds * 20).toInt()

fun Double.mapToRange(from: ClosedFloatingPointRange<Double>, to: ClosedFloatingPointRange<Double>): Double {
    return to.start + ((to.endInclusive - to.start) / (from.endInclusive - from.start)) * (this - from.start)
}

fun Player.applyHypothermia() {
    this.freezeTicks = 15.seconds.inTicks
    this.addPotionEffects(
        mutableListOf(
            PotionEffect(PotionEffectType.SLOW_DIGGING, 6.seconds.inTicks, 1),
            PotionEffect(PotionEffectType.SLOW, 6.seconds.inTicks, 1)
        )
    )
}

fun Player.applyHyperthermia() {
    this.fireTicks = 6.seconds.inTicks
    this.addPotionEffects(
        mutableListOf(
            PotionEffect(PotionEffectType.SLOW_DIGGING, 6.seconds.inTicks, 1),
            PotionEffect(PotionEffectType.SLOW, 6.seconds.inTicks, 1)
        )
    )
}

fun Player.applyThirst() {
    this.damage(2.0)
    this.addPotionEffects(
        mutableListOf(
            PotionEffect(PotionEffectType.SLOW_DIGGING, 6.seconds.inTicks, 1),
            PotionEffect(PotionEffectType.BLINDNESS, 6.seconds.inTicks, 1),
            PotionEffect(PotionEffectType.WEAKNESS, 6.seconds.inTicks, 1),
            PotionEffect(PotionEffectType.SLOW, 6.seconds.inTicks, 1)
        )
    )
}

fun Double.isAcceptableTemperature() = this in CONFIG.temperature.minAcceptableTemperature..CONFIG.temperature.maxAcceptableTemperature

fun Player.displayTemperature(finalTemp: Double, reading: TemperatureReading) {
    val icons = arrayOf("🌡", "🌊", "🪶", "☔", "🔥", "🎽")
    val component =
        (if (finalTemp.isAcceptableTemperature()) Component.text("☀ ").color(NamedTextColor.YELLOW) else Component.text("☃ ").color(NamedTextColor.AQUA))
            .append(Component.text("${finalTemp.roundToInt()}°C (").color(NamedTextColor.WHITE)).append(
        baseComponent(icons[0], reading.baseTemperature).color(NamedTextColor.RED)).append(
        baseComponent(icons[1], reading.waterTemperature).color(NamedTextColor.YELLOW)).append(
        baseComponent(icons[2], reading.movementTemperature).color(NamedTextColor.AQUA)).append(
        baseComponent(icons[3], reading.weatherTemperature).color(NamedTextColor.GREEN)).append(
        baseComponent(icons[4], reading.blockTemperature).color(NamedTextColor.LIGHT_PURPLE)).append(
        baseComponent(icons[5], reading.armorTemperature).color(NamedTextColor.GOLD)).append(
        Component.text(" )").color(NamedTextColor.WHITE))
    this.sendActionBar(
        component
    )
}

private fun baseComponent(icon: String, text: Double) = Component.text(" -> $icon ${text.roundToInt()}°C")

fun Player.displayWater(thirst: Double) {
    this.sendActionBar(Component.text("☕ ${thirst.roundToInt()} / 20").color(NamedTextColor.BLUE))
}

fun <T> T.peek(): T {
    println(this)
    return this
}

fun <T> Random.choice(choices: List<T>) = choices[this.nextInt(0, choices.size)]

fun <T> Random.weightedChoice(choices: List<T>, weights: List<Int>): T {
    if (weights.sum() != 100)
        throw UnsupportedOperationException("Choice weights ($weights) don't add up to 100%")
    if(choices.size != weights.size)
        throw UnsupportedOperationException("Size of choices != Size of weights")
    val random = nextInt(0, 101)
    for (i in choices.indices) {
        val range = if (i == 0) 0..<weights[i]
        else {
            val sum = weights.subList(0, i).sum()
            sum..<sum + weights[i]
        }
        if(random in range)
            return choices[i]
    }
    throw IllegalStateException("Weighted choice was unable to come to a result")
}