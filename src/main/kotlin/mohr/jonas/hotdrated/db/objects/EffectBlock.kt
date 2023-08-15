package mohr.jonas.hotdrated.db.objects

import mohr.jonas.hotdrated.db.DataManager
import mohr.jonas.hotdrated.db.tables.EffectBlockTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class EffectBlock(manager: DataManager) : DataManagerObject(manager) {

    data class CacheEntry(val world: String, val x: Int, val y: Int, val z: Int) {
        companion object {
            fun fromRowOrEmpty(row: ResultRow) =
                CacheEntry(row[EffectBlockTable.world], row[EffectBlockTable.x], row[EffectBlockTable.y], row[EffectBlockTable.z])
        }
    }

    private val cache: MutableList<CacheEntry> = transaction {
        EffectBlockTable.selectAll().map { CacheEntry.fromRowOrEmpty(it) }
    }.toMutableList()

    fun removeCacheEntry(world: String, x: Int, y: Int, z: Int) {
        cache.removeIf { it.world == world && it.x == x && it.y == y && it.z == z }
    }

    fun addCacheEntry(world: String, x: Int, y: Int, z: Int) {
        cache.add(CacheEntry(world, x, y, z))
    }

    fun getAll() = cache

    fun has(world: String, x: Int, y: Int, z: Int) = cache.firstOrNull { it.world == world && it.x == x && it.y == y && it.z == z } != null

    fun commitToDB() {
        transaction {
            EffectBlockTable.deleteAll()
            EffectBlockTable.batchInsert(cache) {
                this[EffectBlockTable.world] = it.world
                this[EffectBlockTable.x] = it.x
                this[EffectBlockTable.y] = it.y
                this[EffectBlockTable.z] = it.z
            }
        }
    }

}