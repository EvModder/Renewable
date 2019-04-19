package Evil_Code_Renewable.listeners;

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
import Evil_Code_Renewable.CraftingUtil;
import Evil_Code_Renewable.Renewable;

public class VillagerTradeListener implements Listener{
	final Renewable plugin;
	final CraftingUtil crafter;
	final boolean ignoreGM1;

	public VillagerTradeListener(){
		plugin = Renewable.getPlugin();
		crafter = new CraftingUtil();
		ignoreGM1 = plugin.getConfig().getBoolean("creative-mode-ignore", true);
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onVillagerAcquireTrade(VillagerAcquireTradeEvent evt){
		//modify villagers' trades in here
	}

	//	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerTradeWithVillager(InventoryClickEvent evt){
		if(evt.getSlotType() != SlotType.RESULT
				|| evt.getInventory().getType() != InventoryType.MERCHANT
				|| evt.isCancelled() ||
				(ignoreGM1 && evt.getWhoClicked().getGameMode() == GameMode.CREATIVE)) return;

		MerchantInventory merch = (MerchantInventory) evt.getInventory();
		MerchantRecipe recipe = merch.getSelectedRecipe();
		recipe.getIngredients();
		if(evt.isCancelled() || evt.getWhoClicked().getGameMode() == GameMode.CREATIVE) return;

		Collection<ItemStack> ingredients = recipe.getIngredients();

		plugin.getLogger().info("Purchasing villager item: "+evt.getInventory().getItem(evt.getRawSlot()).getType());
		plugin.getLogger().info("Recipe item: "+recipe.getResult().getType());
		plugin.getLogger().info("Action result:  "+evt.getResult().name());
		int numCraft = 1;

		ItemStack output = recipe.getResult();
		crafter.handleProcess(evt, ingredients, output, evt.getWhoClicked().getUniqueId(), numCraft);
	}
}