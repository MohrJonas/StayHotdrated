package mohr.jonas.hotdrated.db.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import java.util.*

object TransactionTable : IntIdTable("Transactions") {

    val uuid: Column<UUID> = uuid("uuid")
    val balance: Column<Double> = double("balance")
    val spent: Column<Double> = double("spent")

}