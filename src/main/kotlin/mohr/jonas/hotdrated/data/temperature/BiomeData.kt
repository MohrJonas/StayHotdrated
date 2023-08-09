package mohr.jonas.hotdrated.data.temperature

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BiomeData(
    val id: Int,
    val name: String,
    val category: String,
    @SerialName("temperature") private val temp: Double,
    @SerialName("has_precipitation") val precipitation: Boolean,
    val dimension: String,
    val displayName: String,
    val color: Int
) {
    val temperature: Double
        get() = temp * 22.222222222
}