package mohr.jonas.hotdrated.db.objects

import mohr.jonas.hotdrated.db.DataManager
import mohr.jonas.hotdrated.db.tables.PlayerThirstTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class PlayerThirst(manager: DataManager) : DataManagerObject(manager) {

    fun getPlayerThirst(uuid: UUID) = transaction {
        addLogger(StdOutSqlLogger)
        val result = PlayerThirstTable.slice(PlayerThirstTable.thirst).select { PlayerThirstTable.uuid eq uuid }
            .firstOrNull()?.get(PlayerThirstTable.thirst)
        if (result == null) {
            PlayerThirstTable.insert { it[thirst] = 20.0; it[PlayerThirstTable.uuid] = uuid }
            20.0
        } else
            result
    }

    fun setPlayerThirst(uuid: UUID, thirst: Double) = transaction {
        addLogger(StdOutSqlLogger)
        PlayerThirstTable.update({ PlayerThirstTable.uuid eq uuid }) {
            it[PlayerThirstTable.thirst] = thirst
        }
    }

}