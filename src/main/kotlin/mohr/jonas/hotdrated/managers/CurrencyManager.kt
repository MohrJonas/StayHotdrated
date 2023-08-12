package mohr.jonas.hotdrated.managers

import mohr.jonas.hotdrated.db.DataManager
import net.kyori.adventure.text.TextComponent
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType

object CurrencyManager : Listener {

    @EventHandler
    fun onCurrencyDeposit(event: InventoryClickEvent) {
        if (
            event.inventory.type != InventoryType.MERCHANT
            || ((event.inventory.holder as Entity).customName() as? TextComponent)?.content() != "Teller"
            || (event.currentItem?.lore()?.first() as? TextComponent)?.content() != "Deposit 1000 levels"
            || event.rawSlot != 2
        ) return
        if ((event.whoClicked as Player).level < 1000) {
            event.isCancelled = true
            return
        }
        DataManager.currency.setBalance(event.whoClicked.uniqueId, DataManager.currency.getBalance(event.whoClicked.uniqueId) + 1000)
        (event.whoClicked as Player).giveExpLevels(-1000)
    }

    @EventHandler
    fun onCurrencyWithdraw(event: InventoryClickEvent) {
        if (
            event.inventory.type != InventoryType.MERCHANT
            || ((event.inventory.holder as Entity).customName() as? TextComponent)?.content() != "Teller"
            || (event.currentItem?.lore()?.first() as? TextComponent)?.content() != "Withdraw 1000 levels"
            || event.rawSlot != 2
        ) return
        if (DataManager.currency.getBalance(event.whoClicked.uniqueId) < 1000) {
            event.isCancelled = true
            return
        }
        DataManager.currency.setBalance(event.whoClicked.uniqueId, DataManager.currency.getBalance(event.whoClicked.uniqueId) - 1000)
        (event.whoClicked as Player).giveExpLevels(1000)
    }
}