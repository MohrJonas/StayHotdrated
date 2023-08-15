package mohr.jonas.hotdrated.managers

import com.jeff_media.customblockdata.CustomBlockData
import com.jeff_media.morepersistentdatatypes.DataType
import mohr.jonas.hotdrated.StayHotdrated
import mohr.jonas.hotdrated.db.DataManager
import mohr.jonas.hotdrated.inTicks
import net.kyori.adventure.text.TextComponent
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.time.Duration.Companion.seconds

object EffectBlockManager : Listener {

    init {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(StayHotdrated.PLUGIN, {
            DataManager.effectBlock.getAll().forEach { entry ->
                val world = StayHotdrated.PLUGIN.server.worlds.find { it.name == entry.world }!!
                world.spawnParticle(Particle.DRAGON_BREATH, entry.x + 0.5, entry.y + 2.0, entry.z + 0.5, 1000)
                val data = CustomBlockData(world.getBlockAt(entry.x, entry.y, entry.z), StayHotdrated.PLUGIN)
                val effect = data[NamespacedKey("rumblecraft", "effect_block_type"), DataType.POTION_EFFECT]!!
                world.getNearbyEntities(Location(world, entry.x.toDouble(), entry.y.toDouble(), entry.z.toDouble()), 20.0, 20.0, 20.0)
                    .filterIsInstance<Player>()
                    .forEach { it.addPotionEffect(effect) }
            }
        }, 10.seconds.inTicks.toLong(), 10.seconds.inTicks.toLong())
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (event.block.type != Material.CRYING_OBSIDIAN) return
        val worldName = event.block.world.name
        if (!DataManager.effectBlock.has(worldName, event.block.x, event.block.y, event.block.z)) return
        DataManager.effectBlock.removeCacheEntry(worldName, event.block.x, event.block.y, event.block.z)
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (event.block.type != Material.CRYING_OBSIDIAN) return
        val lore = event.itemInHand.lore() ?: return
        val loreContent = lore.map { (it as TextComponent).content() }
        if (loreContent[0] != "Effect Block") return
        val worldName = event.block.world.name
        DataManager.effectBlock.addCacheEntry(worldName, event.block.x, event.block.y, event.block.z)
        val data = CustomBlockData(event.block, StayHotdrated.PLUGIN)
        data.set(NamespacedKey("rumblecraft", "effect_block_type"), DataType.POTION_EFFECT, PotionEffect(PotionEffectType.getByName(loreContent[1])!!, 5.seconds.inTicks, 1))
    }

}