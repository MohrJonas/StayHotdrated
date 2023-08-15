package mohr.jonas.hotdrated.db.objects

import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import mohr.jonas.hotdrated.db.DataManager
import mohr.jonas.hotdrated.db.tables.PlayerTemperatureTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class PlayerTemperature(manager: DataManager) : DataManagerObject(manager) {

    data class CacheEntry(val temperature: Double, val moveStacks: Double) {
        companion object {
            fun fromRowOrEmpty(row: ResultRow?) =
                row?.let { CacheEntry(it[PlayerTemperatureTable.temperature], it[PlayerTemperatureTable.moveStacks]) } ?: CacheEntry(0.0, 0.0)
        }
    }

    private val cache = Cache.Builder<UUID, CacheEntry>().build()


    fun getPlayerTemperature(uuid: UUID) = runBlocking {
        cache.get(uuid) {
            transaction {
                addLogger(StdOutSqlLogger)
                CacheEntry.fromRowOrEmpty(
                    PlayerTemperatureTable.slice(PlayerTemperatureTable.temperature, PlayerTemperatureTable.moveStacks).select { PlayerTemperatureTable.uuid eq uuid }
                        .firstOrNull())
            }
        }.temperature
    }

    fun setPlayerTemperature(uuid: UUID, temperature: Double) {
        cache.put(uuid, CacheEntry(temperature, getPlayerMoveStacks(uuid)))
    }

    fun getPlayerMoveStacks(uuid: UUID) = runBlocking {
        cache.get(uuid) {
            transaction {
                addLogger(StdOutSqlLogger)
                CacheEntry.fromRowOrEmpty(
                    PlayerTemperatureTable.slice(PlayerTemperatureTable.temperature, PlayerTemperatureTable.moveStacks).select { PlayerTemperatureTable.uuid eq uuid }
                        .firstOrNull())
            }
        }.moveStacks
    }

    fun setPlayerMoveStacks(uuid: UUID, stacks: Double) = runBlocking {
        cache.put(uuid, CacheEntry(getPlayerTemperature(uuid), stacks))
    }

    fun commitToDB() {
        transaction {
            addLogger(StdOutSqlLogger)
            cache.asMap().forEach { it: Map.Entry<Any?, CacheEntry> ->
                val uuid = it.key as UUID
                println("Saving player $uuid")
                val temperature = it.value.temperature
                val stacks = it.value.moveStacks
                val alreadyExists = PlayerTemperatureTable.select { PlayerTemperatureTable.uuid eq uuid }.count() != 0L
                if (alreadyExists) {
                    PlayerTemperatureTable.update({ PlayerTemperatureTable.uuid eq uuid }) {
                        it[PlayerTemperatureTable.temperature] = temperature
                        it[PlayerTemperatureTable.moveStacks] = stacks
                    }
                } else {
                    PlayerTemperatureTable.insert {
                        it[PlayerTemperatureTable.uuid] = uuid
                        it[PlayerTemperatureTable.temperature] = temperature
                        it[PlayerTemperatureTable.moveStacks] = stacks
                    }
                }
            }
        }
    }
}