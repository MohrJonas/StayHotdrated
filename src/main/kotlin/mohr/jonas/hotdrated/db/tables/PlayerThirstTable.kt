package mohr.jonas.hotdrated.db.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import java.util.*

object PlayerThirstTable : IntIdTable("PlayerThirsts") {

    val uuid: Column<UUID> = uuid("uuid")
    val thirst: Column<Double> = double("thirst")

}