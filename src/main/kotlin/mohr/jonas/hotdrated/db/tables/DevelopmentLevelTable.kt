package mohr.jonas.hotdrated.db.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object DevelopmentLevelTable : IntIdTable("DevelopmentLevel") {

    val level = double("level")

}