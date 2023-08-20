package mohr.jonas.hotdrated.data.advancements.tabs

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType
import mohr.jonas.hotdrated.StayHotdrated
import mohr.jonas.hotdrated.data.advancements.*
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

private fun Tab.brewingAdvancement(
    parent: Advancement = this.advancements.lastOrNull() ?: requireNotNull(this.root),
    name: String,
    icon: ItemStack,
    x: Int,
    y: Int,
    description: String,
    frameType: AdvancementFrameType = AdvancementFrameType.TASK,
    maxProgression: Int = 1,
    resultingPotionType: PotionType,
    giveReward: (p: Player) -> Unit = {}
) = advancement<InventoryMoveItemEvent>(parent, name, icon, x, y, description, frameType, maxProgression, onEvent = {
    if (it.source.type != InventoryType.BREWING || it.destination.type != InventoryType.PLAYER || it.item.type != Material.POTION || it.destination.holder !is Player || !isParentSatisfied(
            it.destination.holder as Player
        )
    ) return@advancement
    val meta = it.item.itemMeta as PotionMeta
    if (meta.basePotionData.type != resultingPotionType) return@advancement
    incrementProgression(it.destination.holder as Player)
}, giveReward)

private fun potion(type: PotionType): ItemStack {
    val stack = ItemStack(Material.POTION)
    val meta = stack.itemMeta as PotionMeta
    meta.basePotionData = PotionData(type)
    stack.itemMeta = meta
    return stack
}

val BrewingAdvancementTab = tab(StayHotdrated.ADVANCEMENT_API, "Brewing") {
    root(
        name = "Brewing",
        icon = Material.DRAGON_BREATH,
        x = 0,
        y = 0,
        description = "Potions can give you a distinct advantage in tricky situations",
        background = "textures/block/stone.png"
    )
    pickupAdvancement(
        name = "Eww, warts",
        icon = ItemStack(Material.NETHER_WART, 1),
        x = right(),
        y = 0,
        description = "I don't like 'em, but we need them anyways",
        maxProgression = 8,
        acceptedItems = arrayOf(Material.NETHER_WART)
    )
    val brewingStand = craftAdvancement(
        name = "Brewing Stand",
        icon = ItemStack(Material.BREWING_STAND, 1),
        x = right(),
        y = 0,
        description = "Nothing like throwing together some stone and rods",
        maxProgression = 1,
        resultingItem = Material.BREWING_STAND,
        giveReward = slimefunResearch(NamespacedKey("slimefun", "auto_brewer"))
    )
    craftAdvancement(
        parent = brewingStand,
        name = "Some white powder",
        icon = ItemStack(Material.SUGAR, 1),
        x = right(),
        y = 0,
        description = "Some white powder. Might be sugar, or something else",
        resultingItem = Material.SUGAR
    )
    brewingAdvancement(
        name = "Gotta go fast",
        icon = potion(PotionType.SPEED),
        description = "Sanic",
        x = right(),
        y = 0,
        resultingPotionType = PotionType.SPEED
    )
    brewingAdvancement(
        name = "Like going fast...",
        icon = potion(PotionType.SLOWNESS),
        description = "...But with UNO reverse",
        x = right(),
        y = 0,
        resultingPotionType = PotionType.SLOWNESS
    )
    pickupAdvancement(
        parent = brewingStand,
        name = "RIP Rabbit",
        icon = ItemStack(Material.RABBIT_FOOT, 1),
        description = "PETA is already notified",
        x = rightOf(brewingStand),
        y = 1,
        acceptedItems = arrayOf(Material.RABBIT_FOOT)
    )
    brewingAdvancement(
        name = "Watch your feet",
        icon = potion(PotionType.JUMP),
        description = "Better not break you feet...",
        x = right(),
        y = 1,
        resultingPotionType = PotionType.JUMP
    )
    craftAdvancement(
        parent = brewingStand,
        name = "Blaze(powder) it",
        icon = ItemStack(Material.BLAZE_POWDER, 1),
        x = rightOf(brewingStand),
        y = 2,
        description = "420",
        resultingItem = Material.BLAZE_POWDER
    )
    brewingAdvancement(
        name = "Hittin' hard doesn't make you smart",
        icon = potion(PotionType.STRENGTH),
        description = "Sorry to say, but true",
        x = right(),
        y = 2,
        resultingPotionType = PotionType.STRENGTH
    )
    craftAdvancement(
        parent = brewingStand,
        name = "Eating gold",
        icon = ItemStack(Material.GLISTERING_MELON_SLICE, 1),
        x = rightOf(brewingStand),
        y = 3,
        description = "Really?",
        resultingItem = Material.GLISTERING_MELON_SLICE
    )
    brewingAdvancement(
        name = "Just like taking a pill",
        icon = potion(PotionType.INSTANT_HEAL),
        description = "Big pharma would like to know you location",
        x = right(),
        y = 3,
        resultingPotionType = PotionType.INSTANT_HEAL
    )
    brewingAdvancement(
        name = "Ouch",
        icon = potion(PotionType.INSTANT_DAMAGE),
        description = "That hurt",
        x = right(),
        y = 3,
        resultingPotionType = PotionType.INSTANT_DAMAGE
    )
    pickupAdvancement(
        parent = brewingStand,
        name = "It's the eye of the spider",
        icon = ItemStack(Material.SPIDER_EYE, 1),
        description = "Da da di da da dum",
        x = rightOf(brewingStand),
        y = 4,
        acceptedItems = arrayOf(Material.SPIDER_EYE)
    )
    brewingAdvancement(
        name = "Eww",
        icon = potion(PotionType.POISON),
        description = "You better not drink that",
        x = right(),
        y = 4,
        resultingPotionType = PotionType.POISON
    )
    pickupAdvancement(
        parent = brewingStand,
        name = "Poor ghast",
        icon = ItemStack(Material.GHAST_TEAR, 1),
        description = "Maybe the needs a tissue",
        x = rightOf(brewingStand),
        y = 5,
        acceptedItems = arrayOf(Material.GHAST_TEAR)
    )
    brewingAdvancement(
        name = "Like a potion of health",
        icon = potion(PotionType.REGEN),
        description = "But, let's be honest, worse",
        x = right(),
        y = 5,
        resultingPotionType = PotionType.REGEN
    )
    pickupAdvancement(
        parent = brewingStand,
        name = "I hope it's not THAT kind of creme...",
        icon = ItemStack(Material.MAGMA_CREAM, 1),
        description = "...But who knows",
        x = rightOf(brewingStand),
        y = 6,
        acceptedItems = arrayOf(Material.MAGMA_CREAM)
    )
    brewingAdvancement(
        name = "You can now swim in lava...",
        icon = potion(PotionType.FIRE_RESISTANCE),
        description = "...But who asked",
        x = right(),
        y = 6,
        resultingPotionType = PotionType.FIRE_RESISTANCE
    )
    pickupAdvancement(
        parent = brewingStand,
        name = "Did you know...",
        icon = ItemStack(Material.PUFFERFISH, 1),
        description = "...You actually need a license to prepare this fish",
        x = rightOf(brewingStand),
        y = 7,
        acceptedItems = arrayOf(Material.PUFFERFISH)
    )
    brewingAdvancement(
        name = "You can now breath underwater...",
        icon = potion(PotionType.WATER_BREATHING),
        description = "...But is your phone water resistant?",
        x = right(),
        y = 7,
        resultingPotionType = PotionType.WATER_BREATHING
    )
    craftAdvancement(
        parent = brewingStand,
        name = "Eating gold (II)",
        icon = ItemStack(Material.GOLDEN_CARROT, 1),
        x = rightOf(brewingStand),
        y = 8,
        description = "Seriously, again?",
        resultingItem = Material.GOLDEN_CARROT
    )
    brewingAdvancement(
        name = "Night Vision",
        icon = potion(PotionType.NIGHT_VISION),
        description = "Almost like Nightwish (an awesome band, check it out)",
        x = right(),
        y = 8,
        resultingPotionType = PotionType.NIGHT_VISION
    )
    brewingAdvancement(
        name = "Just because it can't see you...",
        icon = potion(PotionType.INVISIBILITY),
        description = "...Doesn't mean it can't hurt you",
        x = right(),
        y = 8,
        resultingPotionType = PotionType.INVISIBILITY
    )
    craftAdvancement(
        parent = brewingStand,
        name = "Poor turle",
        icon = ItemStack(Material.TURTLE_HELMET, 1),
        x = rightOf(brewingStand),
        y = 9,
        description = "WHY DO YOU KEEP KILLING INNOCENT ANIMALS?!",
        resultingItem = Material.TURTLE_HELMET
    )
    brewingAdvancement(
        name = "Master of all turtles",
        icon = potion(PotionType.TURTLE_MASTER),
        description = "Teenage mutant ninja turtle master",
        x = right(),
        y = 9,
        resultingPotionType = PotionType.TURTLE_MASTER
    )
    pickupAdvancement(
        parent = brewingStand,
        name = "I hate these things",
        icon = ItemStack(Material.PHANTOM_MEMBRANE, 1),
        description = "Super annoying",
        x = rightOf(brewingStand),
        y = 10,
        acceptedItems = arrayOf(Material.PHANTOM_MEMBRANE)
    )
    brewingAdvancement(
        name = "Falling slow...",
        icon = potion(PotionType.SLOW_FALLING),
        description = "...Doesn't mean it won't hurt",
        x = right(),
        y = 10,
        resultingPotionType = PotionType.SLOW_FALLING
    )
}