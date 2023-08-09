package mohr.jonas.hotdrated.db.objects

import mohr.jonas.hotdrated.db.DataManager
import mohr.jonas.hotdrated.db.tables.PlayerTemperatureTable
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class PlayerTemperature(manager: DataManager) : DataManagerObject(manager) {

    fun getPlayerTemperature(uuid: UUID) = transaction {
        addLogger(StdOutSqlLogger)
        val result = PlayerTemperatureTable.slice(PlayerTemperatureTable.temperature).select { PlayerTemperatureTable.uuid eq uuid }
            .firstOrNull()?.get(PlayerTemperatureTable.temperature)
        if (result == null) {
            PlayerTemperatureTable.insert { it[temperature] = 15.0; it[moveStacks] = 0.0; it[PlayerTemperatureTable.uuid] = uuid }
            1.0
        } else
            result
    }

    fun setPlayerTemperature(uuid: UUID, temperature: Double) = transaction {
        addLogger(StdOutSqlLogger)
        PlayerTemperatureTable.update({ PlayerTemperatureTable.uuid eq uuid }) {
            it[PlayerTemperatureTable.temperature] = temperature
        }
    }

    fun getPlayerMoveStacks(uuid: UUID) = transaction {
        addLogger(StdOutSqlLogger)
        val result = PlayerTemperatureTable.slice(PlayerTemperatureTable.moveStacks).select { PlayerTemperatureTable.uuid eq uuid }
            .firstOrNull()?.get(PlayerTemperatureTable.moveStacks)
        if (result == null) {
            PlayerTemperatureTable.insert { it[temperature] = 15.0; it[moveStacks] = 0.0; it[PlayerTemperatureTable.uuid] = uuid }
            0.0
        } else
            result
    }

    fun setPlayerMoveStacks(uuid: UUID, stacks: Double) = transaction {
        addLogger(StdOutSqlLogger)
        PlayerTemperatureTable.update({ PlayerTemperatureTable.uuid eq uuid }) {
            it[moveStacks] = stacks
        }
    }

}