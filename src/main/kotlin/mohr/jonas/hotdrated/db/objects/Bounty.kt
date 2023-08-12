package mohr.jonas.hotdrated.db.objects

import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import mohr.jonas.hotdrated.db.DataManager
import mohr.jonas.hotdrated.db.tables.BountyTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class Bounty(manager: DataManager) : DataManagerObject(manager) {

    data class CacheEntry(val onlineDays: Int, val offlineDays: Int) {
        companion object {
            fun fromRowOrEmpty(row: ResultRow?) = row?.let { CacheEntry(it[BountyTable.onlineDays], it[BountyTable.offlineDays]) } ?: CacheEntry(0, 0)
        }
    }

    private val cache = Cache.Builder<UUID, CacheEntry>().build()

    fun getOnlineDays(uuid: UUID) = runBlocking {
        cache.get(uuid) {
            transaction {
                CacheEntry.fromRowOrEmpty(BountyTable.slice(BountyTable.onlineDays, BountyTable.offlineDays).select { BountyTable.uuid eq uuid }.firstOrNull())
            }
        }.onlineDays
    }

    fun getOfflineDays(uuid: UUID) = runBlocking {
        cache.get(uuid) {
            transaction {
                CacheEntry.fromRowOrEmpty(BountyTable.slice(BountyTable.onlineDays, BountyTable.offlineDays).select { BountyTable.uuid eq uuid }.firstOrNull())
            }
        }.offlineDays
    }

    fun setOnlineDays(uuid: UUID, onlineDays: Int) {
        cache.put(uuid, CacheEntry(onlineDays, getOfflineDays(uuid)))
    }

    fun setOfflineDays(uuid: UUID, offlineDays: Int) {
        cache.put(uuid, CacheEntry(getOnlineDays(uuid), offlineDays))
    }

    fun commitToDB() {
        transaction {
            addLogger(StdOutSqlLogger)
            cache.asMap().forEach { it: Map.Entry<Any?, CacheEntry> ->
                val uuid = it.key as UUID
                println("Saving player $uuid")
                val onlineDays = it.value.onlineDays
                val offlineDays = it.value.offlineDays
                val alreadyExists = BountyTable.select { BountyTable.uuid eq uuid }.count() != 0L
                if (alreadyExists) {
                    BountyTable.update({ BountyTable.uuid eq uuid }) {
                        it[BountyTable.onlineDays] = onlineDays
                        it[BountyTable.offlineDays] = offlineDays
                    }
                } else {
                    BountyTable.insert {
                        it[BountyTable.uuid] = uuid
                        it[BountyTable.onlineDays] = onlineDays
                        it[BountyTable.offlineDays] = offlineDays
                    }
                }
            }
        }
    }
}