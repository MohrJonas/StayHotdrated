package mohr.jonas.hotdrated.data.advancements.tabs

import mohr.jonas.hotdrated.StayHotdrated
import mohr.jonas.hotdrated.data.advancements.craftAdvancement
import mohr.jonas.hotdrated.data.advancements.root
import mohr.jonas.hotdrated.data.advancements.tab
import org.bukkit.Material

val CombatAdvancementTab = tab(StayHotdrated.ADVANCEMENT_API, "Combat") {
    val root = root("Combat", Material.IRON_SWORD, 0, 0, "You'll need a way to defend yourself", "textures/block/stone.png")
    craftAdvancement(
        name = "Blocker McBlockface",
        description = "Wouldn't it be nice to block attacks?",
        icon = Material.SHIELD,
        x = 1,
        y = 0,
        resultingItem = Material.SHIELD
    )
    val bow = craftAdvancement(
        parent = root,
        name = "Sniper elite",
        description = "Headshot!",
        icon = Material.BOW,
        x = 1,
        y = 1,
        resultingItem = Material.BOW
    )
    craftAdvancement(
        parent = root,
        name = "Rip and tear (I)",
        description = "Like a knife through butter",
        icon = Material.IRON_SWORD,
        x = 1,
        y = 2,
        resultingItem = Material.IRON_SWORD
    )
    craftAdvancement(
        name = "Rip and tear (II)",
        description = "Like a knife through butter",
        icon = Material.GOLDEN_SWORD,
        x = 2,
        y = 2,
        resultingItem = Material.GOLDEN_SWORD
    )
    craftAdvancement(
        name = "Rip and tear (III)",
        description = "Like a knife through butter",
        icon = Material.DIAMOND_SWORD,
        x = 3,
        y = 2,
        resultingItem = Material.DIAMOND_SWORD
    )
    craftAdvancement(
        name = "Rip and tear (IV)",
        description = "Like a knife through butter",
        icon = Material.NETHERITE_SWORD,
        x = 4,
        y = 2,
        resultingItem = Material.NETHERITE_SWORD
    )
    craftAdvancement(
        parent = bow,
        name = "Shine bright like a diamond",
        description = "Brighter that a torch",
        icon = Material.SPECTRAL_ARROW,
        x = 2,
        y = 1,
        resultingItem = Material.SPECTRAL_ARROW
    )
    craftAdvancement(
        name = "Splash",
        description = "Like strapping a potion to an arrow",
        icon = Material.TIPPED_ARROW,
        x = 3,
        y = 1,
        resultingItem = Material.TIPPED_ARROW
    )
}