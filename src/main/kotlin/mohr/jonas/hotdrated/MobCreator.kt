package mohr.jonas.hotdrated

import mohr.jonas.hotdrated.db.DataManager
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object MobCreator {

    fun spawnMob(location: Location, entity: EntityType, generator: ((entity: LivingEntity) -> Unit)? = null) {
        location.world.spawnEntity(location, entity, SpawnReason.CUSTOM) {entity ->
            entity as LivingEntity
            entity.addPotionEffects(getPotionEffectsForDevelopmentLevel())
            generator?.invoke(entity)
        }
    }

    private fun getPotionEffectsForDevelopmentLevel() = when (DataManager.developmentLevel.get()) {
        in 300.0..<500.0 -> listOf(PotionEffect(PotionEffectType.HEALTH_BOOST, Int.MAX_VALUE, 2, false, false, false))
        in 500.0..<700.0 -> listOf(
            PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Int.MAX_VALUE, 2, false, false),
            PotionEffect(PotionEffectType.INCREASE_DAMAGE, Int.MAX_VALUE, 1, false, false, false)
        )
        in 700.0..<1000.0 -> listOf(
            PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Int.MAX_VALUE, 3, false, false, false),
            PotionEffect(PotionEffectType.HEALTH_BOOST, Int.MAX_VALUE, 4, false, false, false),
            PotionEffect(
                PotionEffectType.INCREASE_DAMAGE, Int.MAX_VALUE, 3, false, false, false
            )
        )

        else -> listOf(
            PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Int.MAX_VALUE, 4, false, false, false),
            PotionEffect(PotionEffectType.HEALTH_BOOST, Int.MAX_VALUE, 10, false, false, false),
            PotionEffect(
                PotionEffectType.INCREASE_DAMAGE, Int.MAX_VALUE, 5, false, false, false
            ),
            PotionEffect(
                PotionEffectType.SPEED, Int.MAX_VALUE, 2, false, false, false
            )
        )
    }
}