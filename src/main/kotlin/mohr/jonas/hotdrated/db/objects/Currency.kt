package mohr.jonas.hotdrated.db.objects

import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import mohr.jonas.hotdrated.db.DataManager
import mohr.jonas.hotdrated.db.tables.TransactionTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class Currency(manager: DataManager) : DataManagerObject(manager) {

    data class CacheEntry(val spent: Double, val balance: Double) {
        companion object {
            fun fromRowOrEmpty(row: ResultRow?) = row?.let { CacheEntry(it[TransactionTable.spent], it[TransactionTable.balance]) } ?: CacheEntry(0.0, 0.0)
        }
    }

    private val cache = Cache.Builder<UUID, CacheEntry>().build()

    fun getMoneySpent(uuid: UUID) = runBlocking {
        cache.get(uuid) {
            CacheEntry.fromRowOrEmpty(transaction {
                addLogger(StdOutSqlLogger)
                TransactionTable
                    .slice(TransactionTable.spent, TransactionTable.balance)
                    .select { TransactionTable.uuid eq uuid }
                    .firstOrNull()
            })
        }
    }.spent

    fun setMoneySpent(uuid: UUID, amount: Double) {
        cache.put(uuid, CacheEntry(amount, getBalance(uuid)))
    }

    fun getBalance(uuid: UUID) = runBlocking {
        cache.get(uuid) {
            CacheEntry.fromRowOrEmpty(transaction {
                addLogger(StdOutSqlLogger)
                TransactionTable
                    .slice(TransactionTable.spent, TransactionTable.balance)
                    .select { TransactionTable.uuid eq uuid }
                    .firstOrNull()
            })
        }
    }.balance

    fun setBalance(uuid: UUID, amount: Double) {
        cache.put(uuid, CacheEntry(getMoneySpent(uuid), amount))
    }

    fun commitToDB() {
        transaction {
            addLogger(StdOutSqlLogger)
            cache.asMap().forEach { it: Map.Entry<Any?, CacheEntry> ->
                val uuid = it.key as UUID
                println("Saving player $uuid")
                val spent = it.value.spent
                val balance = it.value.balance
                val alreadyExists = TransactionTable.select { TransactionTable.uuid eq uuid }.count() != 0L
                if (alreadyExists) {
                    TransactionTable.update({ TransactionTable.uuid eq uuid }) {
                        it[TransactionTable.balance] = balance
                        it[TransactionTable.spent] = spent
                    }
                } else {
                    TransactionTable.insert {
                        it[TransactionTable.uuid] = uuid
                        it[TransactionTable.spent] = spent
                        it[TransactionTable.balance] = balance
                    }
                }
            }
        }
    }
}