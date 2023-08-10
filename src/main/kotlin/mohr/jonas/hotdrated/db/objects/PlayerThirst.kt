package mohr.jonas.hotdrated.db.objects

import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import mohr.jonas.hotdrated.db.DataManager
import mohr.jonas.hotdrated.db.tables.PlayerThirstTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class PlayerThirst(manager: DataManager) : DataManagerObject(manager) {

    private val cache = Cache.Builder<UUID, Double>().build()

    fun getPlayerThirst(uuid: UUID) = runBlocking {
        cache.get(uuid) {
            transaction {
                addLogger(StdOutSqlLogger)
                PlayerThirstTable.slice(PlayerThirstTable.thirst).select { PlayerThirstTable.uuid eq uuid }
                    .firstOrNull()?.get(PlayerThirstTable.thirst) ?: 20.0
            }
        }
    }

    fun setPlayerThirst(uuid: UUID, thirst: Double) {
        cache.put(uuid, thirst)
    }

    fun commitToDB() {
        transaction {
            addLogger(StdOutSqlLogger)
            cache.asMap().forEach { it: Map.Entry<Any?, Double> ->
                val uuid = it.key as UUID
                println("Saving player $uuid")
                val thirst = it.value
                val alreadyExists = PlayerThirstTable.select { PlayerThirstTable.uuid eq uuid }.count() != 0L
                if (alreadyExists) {
                    PlayerThirstTable.update({ PlayerThirstTable.uuid eq uuid }) {
                        it[PlayerThirstTable.thirst] = thirst
                    }
                } else {
                    PlayerThirstTable.insert {
                        it[PlayerThirstTable.uuid] = uuid
                        it[PlayerThirstTable.thirst] = thirst
                    }
                }
            }
        }
    }

}