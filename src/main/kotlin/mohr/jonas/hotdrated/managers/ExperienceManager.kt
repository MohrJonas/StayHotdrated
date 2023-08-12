package mohr.jonas.hotdrated.managers

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import mohr.jonas.hotdrated.StayHotdrated
import mohr.jonas.hotdrated.StayHotdrated.Companion.CONFIG
import mohr.jonas.hotdrated.db.DataManager
import mohr.jonas.hotdrated.weightedChoice
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Monster
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import kotlin.math.roundToInt
import kotlin.random.Random

object ExperienceManager : Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val xp = getXp(event.block.type)
        if (xp == 0) return
        event.player.giveExpLevels((xp + xp * DataManager.developmentLevel.get()).roundToInt())
        event.player.playSound(event.player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 500f, 1f)
    }

    @EventHandler
    fun onMobKill(event: EntityDeathEvent) {
        val killer = event.entity.killer
        if (killer == null || killer.type != EntityType.PLAYER) return
        val xp = getXp(event.entity)
        if (xp == 0) return
        killer.giveExpLevels((xp + xp * DataManager.developmentLevel.get()).roundToInt())
    }

    @EventHandler
    fun onSunrise(event: ServerTickEndEvent) {
        if (StayHotdrated.PLUGIN.server.worlds.find { it.environment == World.Environment.NORMAL }!!.time != 1L) return
        StayHotdrated.PLUGIN.server.onlinePlayers.forEach {
            it.giveExpLevels(CONFIG.xp.wageAmount)
            it.sendMessage(Component.text("You just got your daily wage!").color(NamedTextColor.GOLD))
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        event.newTotalExp = event.droppedExp / 10
    }

    private fun getXp(entity: LivingEntity): Int {
        return if (entity is Monster) {
            Random.weightedChoice(listOf(CONFIG.xp.monsterKillXpAmount, 0), listOf(CONFIG.xp.monsterKillXpChance, 100 - CONFIG.xp.monsterKillXpChance))
        } else {
            Random.weightedChoice(listOf(CONFIG.xp.mobKillXpAmount, 0), listOf(CONFIG.xp.mobKillXpChance, 100 - CONFIG.xp.mobKillXpChance))
        }
    }

    private fun getXp(blockType: Material) = when (blockType) {
        Material.STONE, Material.DEEPSLATE -> Random.weightedChoice(listOf(CONFIG.xp.mineXpAmount, 0), listOf(CONFIG.xp.mineXpChance, 100 - CONFIG.xp.mineXpChance))
        Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG, Material.MANGROVE_LOG, Material.CHERRY_LOG, Material.WARPED_STEM, Material.CRIMSON_STEM -> Random.weightedChoice(
            listOf(CONFIG.xp.forageXpAmount, 0), listOf(CONFIG.xp.forageXpChance, 100 - CONFIG.xp.forageXpChance)
        )

        Material.CARROTS, Material.POTATOES, Material.WHEAT, Material.NETHER_WART, Material.BEETROOTS, Material.KELP_PLANT, Material.PUMPKIN, Material.MELON -> Random.weightedChoice(
            listOf(CONFIG.xp.farmXpAmount, 0), listOf(CONFIG.xp.farmXpChance, 100 - CONFIG.xp.farmXpChance)
        )

        else -> 0
    }

}