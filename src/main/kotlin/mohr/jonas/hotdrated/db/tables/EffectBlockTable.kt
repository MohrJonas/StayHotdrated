package mohr.jonas.hotdrated.db.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object EffectBlockTable: IntIdTable("EffectBlock") {

    val world: Column<String> = text("world")
    val x: Column<Int> = integer("x")
    val y: Column<Int> = integer("y")
    val z: Column<Int> = integer("z")

}