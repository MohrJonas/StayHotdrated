package mohr.jonas.hotdrated.db.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import java.util.*

object PlayerTemperatureTable : IntIdTable("PlayerTemperatures") {

    val uuid: Column<UUID> = uuid("uuid")
    val temperature: Column<Double> = double("temperature")
    val moveStacks: Column<Double> = double("move_stacks")

}