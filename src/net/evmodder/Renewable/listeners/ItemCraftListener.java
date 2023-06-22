package net.evmodder.Renewable.listeners;

import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import net.evmodder.Renewable.CraftingUtil;
import net.evmodder.Renewable.Renewable;

public class ItemCraftListener implements Listener{
	final Renewable plugin;
	final CraftingUtil crafter;
	final boolean ignoreGM1;

	public ItemCraftListener(){
		plugin = Renewable.getPlugin();
		crafter = new CraftingUtil();
		ignoreGM1 = plugin.getConfig().getBoolean("creative-mode-ignore", true);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemCraft(CraftItemEvent evt){
		if(evt.getWhoClicked().getGameMode() == GameMode.CREATIVE){
			//TODO: if(supplyGM1) ???
			if(ignoreGM1) return;
		}

		ItemStack result = evt.getRecipe().getResult();
		Collection<ItemStack> ingredients = null;
		if(evt.getRecipe() instanceof ShapelessRecipe) ingredients = ((ShapelessRecipe)evt.getRecipe()).getIngredientList();
		else if(evt.getRecipe() instanceof ShapedRecipe) ingredients = ((ShapedRecipe)evt.getRecipe()).getIngredientMap().values();
//		else if(evt.getRecipe() instanceof ComplexRecipe);
		else{
			//ingredients = Arrays.asList(evt.getClickedInventory().getStorageContents());//seems to include the result
			ingredients = new ArrayList<>();
			for(int i=0; i<evt.getClickedInventory().getSize(); ++i){
				if(i != evt.getSlot()) ingredients.add(evt.getClickedInventory().getItem(i));
				else result = evt.getClickedInventory().getItem(i);
			}
			plugin.getLogger().warning("Crafting using non-standared recipe type: "+evt.getRecipe().getClass().getSimpleName());
			//return;
		}

//		plugin.getLogger().info("Inventory Action: "+evt.getAction().name()+", ClickType: "+evt.getClick());
//		plugin.getLogger().info("Current item: "+evt.getCurrentItem().getType()+", amt: "+evt.getCurrentItem().getAmount());

//		plugin.getLogger().info("result item: "+result.getType()+", result unrenewable: "+plugin.getAPI().isUnrenewable(result));
		crafter.handleProcess(evt, ingredients, result, evt.getWhoClicked().getUniqueId(), evt.isShiftClick());
	}
}