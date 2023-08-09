package mohr.jonas.hotdrated.db.objects

import mohr.jonas.hotdrated.db.DataManager
import mohr.jonas.hotdrated.db.tables.TransactionTable
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*

class Currency(manager: DataManager) : DataManagerObject(manager) {

    fun getMoneySpent(uuid: UUID) = transaction {
        TransactionTable.slice(TransactionTable.spent)
            .select { TransactionTable.uuid eq uuid }
            .firstOrNull()?.get(TransactionTable.spent) ?: 0.0
    }

    fun setMoneySpent(uuid: UUID, amount: Double) = transaction {
        val existentRow = TransactionTable.select { TransactionTable.uuid eq uuid }.firstOrNull()
        if(existentRow != null) {
            TransactionTable.update({ TransactionTable.uuid eq uuid }) {
                it[spent] = amount
            }
        }
        else {
            TransactionTable.insert { it[TransactionTable.uuid] = uuid; it[spent] = amount; it[balance] = 0.0 }
        }
    }

    fun getBalance(uuid: UUID) = transaction {
        TransactionTable.slice(TransactionTable.balance)
            .select { TransactionTable.uuid eq uuid }
            .firstOrNull()?.get(TransactionTable.balance) ?: 0.0
    }

    fun setBalance(uuid: UUID, amount: Double) = transaction {
        val existentRow = TransactionTable.select { TransactionTable.uuid eq uuid }.firstOrNull()
        if(existentRow != null) {
            TransactionTable.update({ TransactionTable.uuid eq uuid }) {
                it[balance] = amount
            }
        }
        else {
            TransactionTable.insert { it[TransactionTable.uuid] = uuid; it[spent] = 0.0; it[balance] = amount }
        }
    }
}