package mohr.jonas.hotdrated.data.advancements

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.researches.Research
import mohr.jonas.hotdrated.toValidKey
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ThreadLocalRandom

class Tab(val name: String) {
    lateinit var tab: AdvancementTab
    lateinit var root: RootAdvancement
    val advancements = mutableListOf<PrerequisiteAdvancement<*>>()
}

/**
 * Create a new root advancement. Every tab has to have exactly one, and it has to be the first advancement added
 * @param name Name of the advancement
 * @param icon Icon of the advancement
 * @param x X-Coordinate of the advancement, from left to right
 * @param y Y-Coordinate of the advancement, from top to bottom
 * @param description Description of the advancement
 * @param background Background used for the advancement tab
 */
fun Tab.root(name: String, icon: Material, x: Int, y: Int, description: String, background: String): RootAdvancement {
    val advancement = RootAdvancement(
        this.tab, name.toValidKey(), AdvancementDisplay(icon, name, AdvancementFrameType.TASK, true, true, x.toFloat(), y.toFloat(), listOf(description)), background
    )
    this.root = advancement
    return advancement
}

/**
 * Add a new advancement, requiring the user to break a certain kind of block(s) a certain amount of times
 * @param parent The parent of this advancement. Defaults to the last declared advancement or root, if it's the first advancement
 * @param name Name of the advancement
 * @param icon Icon of the advancement
 * @param x X-Coordinate of the advancement, from left to right
 * @param y Y-Coordinate of the advancement, from top to bottom
 * @param description Description of the advancement
 * @param frameType Frame used for the advancement, defaults to task,
 * @param maxProgression The amount of times the action has to be done
 * @param acceptedBlocks List of blocks that, when broken, increase the progress
 * @param giveReward Function called when the advancement is completed. Multiple giveReward lambdas can be added (i.e. randomItemReward(...) + fixedItemReward(...) + ...)
 * @see Tab.randomItemReward
 * @see Tab.fixedItemReward
 * @see Tab.randomSlimefunItemReward
 * @see Tab.fixedSlimefunItemReward
 * @see Tab.slimefunResearch
 */
fun Tab.mineAdvancement(
    parent: Advancement = this.advancements.lastOrNull() ?: requireNotNull(this.root),
    name: String,
    icon: ItemStack,
    x: Int,
    y: Int,
    description: String,
    frameType: AdvancementFrameType = AdvancementFrameType.TASK,
    maxProgression: Int = 1,
    vararg acceptedBlocks: Material,
    giveReward: (p: Player) -> Unit = {}
) = advancement<BlockBreakEvent>(
    parent, name, icon, x, y, description, frameType, maxProgression, {
        if (!isParentSatisfied(it.player) || !acceptedBlocks.contains(it.block.type)) return@advancement
        incrementProgression(it.player)
    }, giveReward
)

/**
 * Add a new advancement, requiring the user to craft a certain kind of item a certain amount of times
 * @param parent The parent of this advancement. Defaults to the last declared advancement or root, if it's the first advancement
 * @param name Name of the advancement
 * @param icon Icon of the advancement
 * @param x X-Coordinate of the advancement, from left to right
 * @param y Y-Coordinate of the advancement, from top to bottom
 * @param description Description of the advancement
 * @param frameType Frame used for the advancement, defaults to task,
 * @param maxProgression The amount of times the action has to be done
 * @param resultingItem Item that has to be the result from the crafting event to count towards progression
 * @param giveReward Function called when the advancement is completed. Multiple giveReward lambdas can be added (i.e. randomItemReward(...) + fixedItemReward(...) + ...)
 * @see Tab.randomItemReward
 * @see Tab.fixedItemReward
 * @see Tab.randomSlimefunItemReward
 * @see Tab.fixedSlimefunItemReward
 * @see Tab.slimefunResearch
 */
fun Tab.craftAdvancement(
    parent: Advancement = this.advancements.lastOrNull() ?: requireNotNull(this.root),
    name: String,
    icon: ItemStack,
    x: Int,
    y: Int,
    description: String,
    frameType: AdvancementFrameType = AdvancementFrameType.TASK,
    maxProgression: Int = 1,
    resultingItem: Material,
    giveReward: (p: Player) -> Unit = {}
) = advancement<CraftItemEvent>(
    parent,
    name,
    icon,
    x,
    y,
    description,
    frameType,
    maxProgression,
    {
        if (it.whoClicked !is Player || !isParentSatisfied(it.whoClicked as Player) || it.recipe.result.type != resultingItem) return@advancement
        incrementProgression(it.whoClicked as Player)
    },
    giveReward
)

/**
 * Add a new advancement, requiring the user to pick up a certain kind(s) of item a certain amount of times
 * @param parent The parent of this advancement. Defaults to the last declared advancement or root, if it's the first advancement
 * @param name Name of the advancement
 * @param icon Icon of the advancement
 * @param x X-Coordinate of the advancement, from left to right
 * @param y Y-Coordinate of the advancement, from top to bottom
 * @param description Description of the advancement
 * @param frameType Frame used for the advancement, defaults to task,
 * @param maxProgression The amount of times the action has to be done
 * @param acceptedItems Items that, on pick up, count towards progression
 * @param giveReward Function called when the advancement is completed. Multiple giveReward lambdas can be added (i.e. randomItemReward(...) + fixedItemReward(...) + ...)
 * @see Tab.randomItemReward
 * @see Tab.fixedItemReward
 * @see Tab.randomSlimefunItemReward
 * @see Tab.fixedSlimefunItemReward
 * @see Tab.slimefunResearch
 */
fun Tab.pickupAdvancement(
    parent: Advancement = this.advancements.lastOrNull() ?: requireNotNull(this.root),
    name: String,
    icon: ItemStack,
    x: Int,
    y: Int,
    description: String,
    frameType: AdvancementFrameType = AdvancementFrameType.TASK,
    maxProgression: Int = 1,
    vararg acceptedItems: Material,
    giveReward: (p: Player) -> Unit = {}
) = advancement<EntityPickupItemEvent>(
    parent,
    name,
    icon,
    x,
    y,
    description,
    frameType,
    maxProgression,
    {
        if (it.entity !is Player || !isParentSatisfied(it.entity as Player) || !acceptedItems.contains(it.item.itemStack.type)) return@advancement
        incrementProgression(it.entity as Player)
    },
    giveReward
)

/**
 * Add a new advancement, providing the type of event and a callback for that event. This event will have to handle incrementing progression manually
 * @param T Type of the event to react to
 * @param parent The parent of this advancement. Defaults to the last declared advancement or root, if it's the first advancement
 * @param name Name of the advancement
 * @param icon Icon of the advancement
 * @param x X-Coordinate of the advancement, from left to right
 * @param y Y-Coordinate of the advancement, from top to bottom
 * @param description Description of the advancement
 * @param frameType Frame used for the advancement, defaults to task,
 * @param maxProgression The amount of times the action has to be done
 * @param onEvent Lambda called when the specified event occurs
 * @param giveReward Function called when the advancement is completed. Multiple giveReward lambdas can be added (i.e. randomItemReward(...) + fixedItemReward(...) + ...)
 * @see Tab.randomItemReward
 * @see Tab.fixedItemReward
 * @see Tab.randomSlimefunItemReward
 * @see Tab.fixedSlimefunItemReward
 * @see Tab.slimefunResearch
 */
inline fun <reified T : Event> Tab.advancement(
    parent: Advancement = this.advancements.lastOrNull() ?: this.root,
    name: String,
    icon: ItemStack,
    x: Int,
    y: Int,
    description: String,
    frameType: AdvancementFrameType = AdvancementFrameType.TASK,
    maxProgression: Int = 1,
    crossinline onEvent: PrerequisiteAdvancement<T>.(event: T) -> Unit,
    crossinline giveReward: (p: Player) -> Unit = {}
): PrerequisiteAdvancement<T> {
    val advancement = object : PrerequisiteAdvancement<T>(
        name.toValidKey(), name, x, y, parent, T::class.java, frameType, listOf(description), icon, maxProgression
    ) {
        override fun onEvent(event: T) {
            onEvent(this, event)
        }

        override fun giveReward(player: Player) {
            giveReward(player)
        }
    }
    this.advancements.add(advancement)
    return advancement
}

/**
 * Return a lambda, giving the player a specified ItemStack upon invocation. Can be passed to giveReward
 * @param stack The ItemStack to give to the player
 * */
fun Tab.fixedItemReward(stack: ItemStack) = { it: Player -> it.inventory.addItem(stack).drop() }

/**
 * Return a lambda, giving the player an ItemStack with the given Material and a random amount upon invocation. Can be passed to giveReward
 * @param type Type of ItemStack to give
 * @param min Min size of stack to give
 * @param max Max size of stack to give
 * */
fun Tab.randomItemReward(type: Material, min: Int, max: Int) =
    { it: Player -> it.inventory.addItem(ItemStack(type, ThreadLocalRandom.current().nextInt(min, max + 1))).drop() }

/**
 * Return a lambda, giving the player a Slimefun Item a certain amount of times. Can be passed to giveReward
 * @param id ID of SlimefunItem to give
 * @param amount Size of stack to give
 * */
fun Tab.fixedSlimefunItemReward(id: String, amount: Int) = { it: Player -> it.inventory.addItem(ItemStack(SlimefunItem.getById(id)!!.item.type, amount)).drop() }

/**
 * Return a lambda, giving the player an ItemStack with the given Slimefun Item and a random amount upon invocation. Can be passed to giveReward
 * @param id ID of SlimefunItem to give
 * @param min Min size of stack to give
 * @param max Max size of stack to give
 * */
fun Tab.randomSlimefunItemReward(id: String, min: Int, max: Int) =
    { it: Player -> it.inventory.addItem(ItemStack(SlimefunItem.getById(id)!!.item.type, ThreadLocalRandom.current().nextInt(min, max + 1))).drop() }

/**
 * Return a lambda, giving the player the Slimefun research. Can be passed to giveReward
 * @param key Key of the research to give
 * */
fun Tab.slimefunResearch(key: NamespacedKey) = { it: Player -> Research.getResearch(key).orElseThrow().unlock(it, true).drop() }

/**
 * Get an x value, so this advancement will be placed to the right of the previous.
 * */
fun Tab.right() = ((this.advancements.lastOrNull() ?: this.root).display.x + 1).toInt()

/**
 * Get an x value, so this advancement will be placed to the left of the previous.
 * */
fun Tab.left() = ((this.advancements.lastOrNull() ?: this.root).display.x - 1).toInt()

/**
 * Get a y value, so this advancement will be placed above the previous.
 * */
fun Tab.up() = ((this.advancements.lastOrNull() ?: this.root).display.y - 1).toInt()

/**
 * Get a y value, so this advancement will be placed below the previous.
 * */
fun Tab.down() = ((this.advancements.lastOrNull() ?: this.root).display.y + 1).toInt()

/**
 * Get an x value, so this advancement will be placed to the right of given one.
 * */
fun Tab.rightOf(advancement: Advancement) = (advancement.display.x + 1).toInt()

/**
 * Get an x value, so this advancement will be placed to the left of given one.
 * */
fun Tab.leftOf(advancement: Advancement) = (advancement.display.x - 1).toInt()

/**
 * Get a y value, so this advancement will be placed above given one.
 * */
fun Tab.aboveOf(advancement: Advancement) = (advancement.display.y - 1).toInt()

/**
 * Get a y value, so this advancement will be placed below given one.
 * */
fun Tab.belowOf(advancement: Advancement) = (advancement.display.y + 1).toInt()

/**
 * Create a new Advancement Tab
 * @param api Api to use. Will most likely be StayHotdrated.ADVANCEMENT_API
 * @param name Name of the tab to create
 * @param init Lambda responsible for tab initialization
 * */
fun tab(api: UltimateAdvancementAPI, name: String, init: Tab.() -> Unit): AdvancementTab {
    val tab = Tab(name)
    tab.tab = api.createAdvancementTab(name.toValidKey())
    init(tab)
    tab.tab.registerAdvancements(tab.root, *tab.advancements.toTypedArray())
    return tab.tab
}

private fun Any.drop() = Unit

operator fun ((p: Player) -> Unit).plus(other: (p: Player) -> Unit) = { it: Player -> this(it); other(it) }