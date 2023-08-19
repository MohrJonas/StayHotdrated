package mohr.jonas.hotdrated.data.advancements.reunification

import mohr.jonas.hotdrated.StayHotdrated
import mohr.jonas.hotdrated.data.advancements.*
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.player.PlayerMoveEvent
import kotlin.math.abs

val ReunificationAdvancementTab = tab(StayHotdrated.ADVANCEMENT_API, "Reunification") {
    root("The start of everything", Material.EGG, 0, 0, "Welcome to Rumblecraft", "textures/block/stone.png")
    craftAdvancement(name = "Damn you're hot", icon = Material.FURNACE, x = 1, y = 0, description = "Grab some stone and build a furnace", resultingItem = Material.FURNACE)
    craftAdvancement(
        name = "Miner's delight", icon = Material.STONE_PICKAXE, x = 2, y = 0, description = "Grab yourself a pickaxe and get digging", resultingItem = Material.STONE_PICKAXE
    )
    val home = advancement<PlayerMoveEvent>(name = "Home sweet home", icon = Material.COMPASS, x = 3, y = 0, description = "Get going and reunite with your guild", onEvent = {
        val targetLocation = Location(it.player.location.world, 0.0, 0.0, 0.0)
        if (abs(it.player.location.x - targetLocation.x) <= 30 && abs(it.player.location.z - targetLocation.z) <= 30) incrementProgression(it.player)
    })
    mineAdvancement(
        parent = home,
        name = "Hard as a rock",
        icon = Material.COBBLESTONE,
        x = 3,
        y = 1,
        description = "Dig Dig Dig",
        acceptedBlocks = arrayOf(Material.STONE, Material.DEEPSLATE, Material.GRANITE, Material.DIORITE, Material.ANDESITE),
        maxProgression = 32
    )
}