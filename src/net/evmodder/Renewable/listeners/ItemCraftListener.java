package net.evmodder.Renewable.listeners;

import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import net.evmodder.Renewable.CraftingUtil;
import net.evmodder.Renewable.Renewable;

public class ItemCraftListener implements Listener{
	final private Renewable pl;
	final private CraftingUtil crafter;
	final private boolean ignoreGM1;

	public ItemCraftListener(){
		pl = Renewable.getPlugin();
		crafter = new CraftingUtil();
		ignoreGM1 = pl.getConfig().getBoolean("creative-mode-ignore", true);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemCraft(CraftItemEvent evt){
		if(evt.getWhoClicked().getGameMode() == GameMode.CREATIVE){
			//TODO: if(supplyGM1) ???
			if(ignoreGM1) return;
		}

//		plugin.getLogger().info("Inventory Action: "+evt.getAction().name()+", ClickType: "+evt.getClick());
//		plugin.getLogger().info("Current item: "+evt.getCurrentItem().getType()+", amt: "+evt.getCurrentItem().getAmount());

		ItemStack result = evt.getRecipe().getResult();
//		pl.getLogger().info("result amt:"+result.getAmount());
		final int resultMult = result.getAmount();
		Collection<ItemStack> ingredients = null;
//		if(evt.getRecipe() instanceof ShapelessRecipe) ingredients = ((ShapelessRecipe)evt.getRecipe()).getIngredientList();
//		else if(evt.getRecipe() instanceof ShapedRecipe) ingredients = ((ShapedRecipe)evt.getRecipe()).getIngredientMap().values();
//		else if(evt.getRecipe() instanceof ComplexRecipe);
//		else{
			//ingredients = Arrays.asList(evt.getClickedInventory().getStorageContents());//seems to include the result
			ingredients = new ArrayList<>();
			for(int i=0; i<evt.getClickedInventory().getSize(); ++i){
				final ItemStack item = evt.getClickedInventory().getItem(i);
				if(i != evt.getSlot()){if(item != null)ingredients.add(item);}
				else result = item;
			}
//			pl.getLogger().warning("Crafting using non-standared recipe type: "+evt.getRecipe().getClass().getSimpleName());
			//return;
//		}
//		for(ItemStack i : ingredients) pl.getLogger().info("ingr: "+i.getType());
//		pl.getLogger().info("result: "+result.getType()+", result amt: "+result.getAmount());

		crafter.handleProcess(evt, ingredients, result, evt.getWhoClicked().getUniqueId(), evt.isShiftClick(), resultMult);
	}
}