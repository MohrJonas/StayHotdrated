package mohr.jonas.hotdrated.data.advancements

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType
import mohr.jonas.hotdrated.toValidKey
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.inventory.CraftItemEvent

class Tab(val name: String) {
    lateinit var tab: AdvancementTab
    lateinit var root: RootAdvancement
    val advancements = mutableListOf<PrerequisiteAdvancement<*>>()
}

fun Tab.root(name: String, icon: Material, x: Int, y: Int, description: String, background: String): RootAdvancement {
    val advancement = RootAdvancement(
        this.tab, name.toValidKey(), AdvancementDisplay(icon, name, AdvancementFrameType.TASK, true, true, x.toFloat(), y.toFloat(), listOf(description)), background
    )
    this.root = advancement
    return advancement
}

fun Tab.mineAdvancement(
    parent: Advancement = this.advancements.lastOrNull() ?: this.root,
    name: String,
    icon: Material,
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

fun Tab.craftAdvancement(
    parent: Advancement = this.advancements.lastOrNull() ?: this.root,
    name: String,
    icon: Material,
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

inline fun <reified T : Event> Tab.advancement(
    parent: Advancement = this.advancements.lastOrNull() ?: this.root,
    name: String,
    icon: Material,
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

fun tab(api: UltimateAdvancementAPI, name: String, init: Tab.() -> Unit): AdvancementTab {
    val tab = Tab(name)
    tab.tab = api.createAdvancementTab(name.toValidKey())
    init(tab)
    tab.tab.registerAdvancements(tab.root, *tab.advancements.toTypedArray())
    return tab.tab
}