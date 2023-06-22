package net.evmodder.Renewable.listeners;

import java.util.Collection;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import net.evmodder.Renewable.CraftingUtil;
import net.evmodder.Renewable.Renewable;

public class VillagerTradeListener_UNUSED implements Listener{
	final private Renewable pl;
	final private CraftingUtil crafter;
	final private boolean ignoreGM1;

	public VillagerTradeListener_UNUSED(){
		pl = Renewable.getPlugin();
		crafter = new CraftingUtil();
		ignoreGM1 = pl.getConfig().getBoolean("creative-mode-ignore", true);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onVillagerAcquireTrade(VillagerAcquireTradeEvent evt){
		//modify villagers' trades in here
	}

	// TODO: Check if this event is already handled by ItemCraftListener.java
	//	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTradeWithVillager(InventoryClickEvent evt){
		if(evt.getSlotType() != SlotType.RESULT
				|| evt.getInventory().getType() != InventoryType.MERCHANT
				|| (ignoreGM1 && evt.getWhoClicked().getGameMode() == GameMode.CREATIVE)) return;

		final MerchantInventory merch = (MerchantInventory) evt.getInventory();
		final MerchantRecipe recipe = merch.getSelectedRecipe();
		recipe.getIngredients();
		if(evt.isCancelled() || evt.getWhoClicked().getGameMode() == GameMode.CREATIVE) return;

		final Collection<ItemStack> ingredients = recipe.getIngredients();

		pl.getLogger().info("Purchasing villager item: "+evt.getInventory().getItem(evt.getRawSlot()).getType());
		pl.getLogger().info("Recipe item: "+recipe.getResult().getType());
		pl.getLogger().info("Action result:  "+evt.getResult().name());

		final ItemStack output = recipe.getResult();
		crafter.handleProcess(evt, ingredients, output, evt.getWhoClicked().getUniqueId(), evt.isShiftClick(), recipe.getResult().getAmount());
	}
}