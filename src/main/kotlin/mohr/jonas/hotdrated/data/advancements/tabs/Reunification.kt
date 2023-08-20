package mohr.jonas.hotdrated.data.advancements.tabs

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.researches.Research
import mohr.jonas.hotdrated.StayHotdrated
import mohr.jonas.hotdrated.data.advancements.*
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs

val LOGS = arrayOf(
    Material.OAK_LOG,
    Material.SPRUCE_LOG,
    Material.BIRCH_LOG,
    Material.JUNGLE_LOG,
    Material.ACACIA_LOG,
    Material.DARK_OAK_LOG,
    Material.MANGROVE_LOG,
    Material.CHERRY_LOG,
    Material.WARPED_STEM,
    Material.CRIMSON_STEM
)

val STONES = arrayOf(
    Material.STONE, Material.GRANITE, Material.ANDESITE, Material.DIORITE, Material.DEEPSLATE, Material.NETHERRACK
)

val FOODS = arrayOf(
    Material.COOKED_MUTTON,
    Material.COOKED_PORKCHOP,
    Material.COOKED_SALMON,
    Material.COOKED_BEEF,
    Material.COOKED_CHICKEN,
    Material.COOKED_COD,
    Material.COOKED_RABBIT,
    Material.BAKED_POTATO,
    Material.BEETROOT,
    Material.BREAD,
    Material.CARROT,
    Material.APPLE
)

val DIRTS = arrayOf(
    Material.GRASS_BLOCK,
    Material.DIRT,
    Material.DIRT_PATH,
    Material.COARSE_DIRT,
    Material.ROOTED_DIRT
)

val ReunificationAdvancementTab = tab(StayHotdrated.ADVANCEMENT_API, "Reunification") {
    root("Reunification", Material.EGG, 0, 0, "Your first steps in Rumblecraft", "textures/block/stone.png")
    mineAdvancement(
        name = "Getting wood (II)", description = "I think you know what to do...", icon = Material.OAK_LOG, x = 1, y = 0, maxProgression = 8, acceptedBlocks = LOGS
    ) {
        it.inventory.addItem(ItemStack(Material.APPLE, ThreadLocalRandom.current().nextInt(4, 9)))
    }
    val craftingTable = craftAdvancement(
        name = "Mine + Craft = Minecraft",
        description = "Can't really do anything without a crafting table, so get one",
        icon = Material.CRAFTING_TABLE,
        x = 2,
        y = 0,
        resultingItem = Material.CRAFTING_TABLE
    )
    val pickaxe = craftAdvancement(
        name = "Miner's delight", description = "Grab yourself a pickaxe and get digging", icon = Material.STONE_PICKAXE, x = 3, y = 0, resultingItem = Material.STONE_PICKAXE
    )
    mineAdvancement(
        parent = pickaxe, name = "Hard as a rock", icon = Material.COBBLESTONE, x = 4, y = 0, description = "Dig Dig Dig", acceptedBlocks = STONES, maxProgression = 32
    )
    craftAdvancement(
        name = "Damn you're hot", description = "Grab some stone and build a furnace", icon = Material.FURNACE, x = 5, y = 0, resultingItem = Material.FURNACE
    )
    advancement<PlayerMoveEvent>(name = "Home sweet home", icon = Material.COMPASS, x = 6, y = 0, description = "Get going and reunite with your guild", onEvent = {
        val targetLocation = Location(it.player.location.world, 0.0, 0.0, 0.0)
        if (abs(it.player.location.x - targetLocation.x) <= 30 && abs(it.player.location.z - targetLocation.z) <= 30) incrementProgression(it.player)
    }) {
        it.inventory.addItem(SlimefunItem.getById("FORTUNE_COOKIE")!!.item.clone())
        Research.getResearch(NamespacedKey("slimefun", "ore_crusher")).orElseThrow().unlock(it, true)
        Research.getResearch(NamespacedKey("slimefun", "fortune_cookie")).orElseThrow().unlock(it, true)
    }
    craftAdvancement(
        parent = craftingTable, name = "All these hoes", description = "These hoes ain't loyal", icon = Material.STONE_HOE, x = 3, y = 1, resultingItem = Material.STONE_HOE
    )
    mineAdvancement(
        name = "W33d",
        description = "Not THAT kind of wheat",
        icon = Material.WHEAT,
        x = 4,
        y = 1,
        acceptedBlocks = arrayOf(Material.WHEAT),
        maxProgression = 3,
        giveReward = randomItemReward(Material.BREAD, 4, 9)
    )
    craftAdvancement(
        parent = craftingTable, name = "Is it a shovel or a spade?", description = "You need a way to get rid of that annoying gravel", icon = Material.STONE_SHOVEL, x = 3, y = 2, resultingItem = Material.STONE_SHOVEL
    )
    mineAdvancement(
        name = "Dirt busters",
        description = "Don't worry it isn't a ghost. It's just some dirt",
        icon = Material.GRASS_BLOCK,
        x = 4,
        y = 2,
        acceptedBlocks = DIRTS,
        maxProgression = 16
    )
    pickupAdvancement(
        parent = craftingTable,
        name = "You're just let me starve?!",
        description = "Get me something to eat. Anything!",
        icon = Material.APPLE,
        x = 2,
        y = 1,
        acceptedItems = FOODS
    )
}