package mohr.jonas.hotdrated.db

import mohr.jonas.hotdrated.db.objects.Currency
import mohr.jonas.hotdrated.db.objects.DevelopmentLevel
import mohr.jonas.hotdrated.db.objects.PlayerTemperature
import mohr.jonas.hotdrated.db.objects.PlayerThirst
import mohr.jonas.hotdrated.db.tables.DevelopmentLevelTable
import mohr.jonas.hotdrated.db.tables.PlayerTemperatureTable
import mohr.jonas.hotdrated.db.tables.PlayerThirstTable
import mohr.jonas.hotdrated.db.tables.TransactionTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object DataManager {

    init {
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(DevelopmentLevelTable, PlayerTemperatureTable, PlayerThirstTable, TransactionTable)
        }
    }

    val currency = Currency(this)
    val developmentLevel = DevelopmentLevel(this)
    val thirst = PlayerThirst(this)
    val temperature = PlayerTemperature(this)

}