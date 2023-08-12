package mohr.jonas.hotdrated.db

import mohr.jonas.hotdrated.db.objects.*
import mohr.jonas.hotdrated.db.tables.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object DataManager {

    init {
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(DevelopmentLevelTable, PlayerTemperatureTable, PlayerThirstTable, TransactionTable, BountyTable)
        }
    }

    val currency = Currency(this)
    val developmentLevel = DevelopmentLevel(this)
    val thirst = PlayerThirst(this)
    val temperature = PlayerTemperature(this)
    val bounty = Bounty(this)

}