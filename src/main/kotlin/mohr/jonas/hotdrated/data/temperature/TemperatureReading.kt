package mohr.jonas.hotdrated.data.temperature

data class TemperatureReading(
    val baseTemperature: Double,
    val waterTemperature: Double,
    val movementTemperature: Double,
    val weatherTemperature: Double,
    val blockTemperature: Double,
    val armorTemperature: Double
)
