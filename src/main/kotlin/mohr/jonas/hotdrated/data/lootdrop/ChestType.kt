package mohr.jonas.hotdrated.data.lootdrop

enum class ChestType(val lootTableName: String) {
    RANDOM("lootdrop_random"),
    LUCKY("lootdrop_lucky"),
    MYSTICAL("lootdrop_mystical"),
    GARBAGE("lootdrop_garbage"),
    FOOD("lootdrop_food"),
    WEAPON("lootdrop_weapon"),
    AMMO("lootdrop_ammo"),
    ARMOR("lootdrop_armor"),
    RESOURCES("lootdrop_resources")
}