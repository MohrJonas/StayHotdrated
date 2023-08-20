package mohr.jonas.hotdrated.data.advancements

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack

/*
* A PrerequisiteAdvancement requires its parent to be completed before it becomes visible and / or completable
*/
abstract class PrerequisiteAdvancement<T : Event>(
    key: String,
    title: String,
    x: Int,
    y: Int,
    parent: Advancement,
    eventType: Class<T>,
    frame: AdvancementFrameType,
    description: List<String>,
    icon: ItemStack,
    maxProgression: Int
) : BaseAdvancement(key, AdvancementDisplay(icon, title, frame, true, true, x.toFloat(), y.toFloat(), description), parent, maxProgression) {

    init {
        registerEvent(eventType, ::onEvent)
    }

    abstract fun onEvent(event: T)

    abstract override fun giveReward(player: Player)

    override fun isVisible(progression: TeamProgression) = this.parent is RootAdvancement || progression.getProgression(this.parent) == this.parent.maxProgression

    fun isParentSatisfied(player: Player) = this.parent is RootAdvancement || this.parent.getProgression(player) == this.parent.maxProgression
}