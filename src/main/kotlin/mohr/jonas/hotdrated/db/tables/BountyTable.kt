package mohr.jonas.hotdrated.db.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import java.util.*

object BountyTable : IntIdTable("PlayerBounty") {

    val uuid: Column<UUID> = uuid("uuid")
    val onlineDays: Column<Int> = integer("onlineDays")
    val offlineDays: Column<Int> = integer("offlineDays")

}