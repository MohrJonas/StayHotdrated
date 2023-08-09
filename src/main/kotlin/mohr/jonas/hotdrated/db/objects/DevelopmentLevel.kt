package mohr.jonas.hotdrated.db.objects

import mohr.jonas.hotdrated.db.DataManager
import mohr.jonas.hotdrated.db.tables.DevelopmentLevelTable
import org.bukkit.Material
import org.bukkit.Statistic
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class DevelopmentLevel(manager: DataManager) : DataManagerObject(manager) {
    fun get() = transaction {
        DevelopmentLevelTable.slice(DevelopmentLevelTable.level).selectAll().firstOrNull()?.get(DevelopmentLevelTable.level) ?: 0.0
    }

    fun set(value: Double) = transaction {
        val existentRow = DevelopmentLevelTable.selectAll().empty()
        if (existentRow)
            DevelopmentLevelTable.update { it[level] = value }
        else
            DevelopmentLevelTable.insert { it[level] = value }
    }

    fun recalculate(plugin: JavaPlugin) {
        val players = plugin.server.onlinePlayers + plugin.server.offlinePlayers
        val level = 0.06 * players.sumOf { DataManager.currency.getMoneySpent(it.uniqueId) }
        +100 * players.sumOf { it.getStatistic(Statistic.PLAYER_KILLS) }
        +10 * players.sumOf { it.getStatistic(Statistic.KILL_ENTITY, EntityType.ZOMBIE) }
        +11 * players.sumOf { it.getStatistic(Statistic.KILL_ENTITY, EntityType.SKELETON) }
        +0.75 * players.sumOf {
            arrayOf(
                it.getStatistic(Statistic.USE_ITEM, Material.WOODEN_PICKAXE),
                it.getStatistic(Statistic.USE_ITEM, Material.STONE_PICKAXE),
                it.getStatistic(Statistic.USE_ITEM, Material.IRON_PICKAXE),
                it.getStatistic(Statistic.USE_ITEM, Material.GOLDEN_PICKAXE),
                it.getStatistic(Statistic.USE_ITEM, Material.DIAMOND_PICKAXE),
                it.getStatistic(Statistic.USE_ITEM, Material.NETHERITE_PICKAXE)
            ).sum()
        }
        +0.1 * players.sumOf { it.getStatistic(Statistic.CRAFT_ITEM) }
        +55 * ((players.sumOf { it.getStatistic(Statistic.PLAY_ONE_MINUTE) } / players.size) / plugin.server.worlds.find { it.environment == World.Environment.NORMAL }!!.gameTime)
        set(level)
    }
}