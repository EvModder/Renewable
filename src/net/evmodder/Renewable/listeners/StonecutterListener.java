package net.evmodder.Renewable.listeners;

import org.bukkit.event.Listener;
import net.evmodder.Renewable.CraftingUtil;
import net.evmodder.Renewable.Renewable;

public class StonecutterListener implements Listener{
	final Renewable plugin;
	final CraftingUtil crafter;
	final boolean ignoreGM1;

	public StonecutterListener(){
		plugin = Renewable.getPlugin();
		crafter = new CraftingUtil();
		ignoreGM1 = plugin.getConfig().getBoolean("creative-mode-ignore", true);
	}

/*	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemCraft(StonecutterEvent evt){
		if(evt.isCancelled()) return;
		if(ignoreGM1 && evt.getWhoClicked().getGameMode() == GameMode.CREATIVE) return;

		Collection<ItemStack> ingredients;
		if(evt.getRecipe() instanceof ShapelessRecipe){
			ingredients = Arrays.asList(evt.getClickedInventory().getContents());
		}
		else if(evt.getRecipe() instanceof ShapedRecipe)
			ingredients = ((ShapedRecipe)evt.getRecipe()).getIngredientMap().values();
		else{
			plugin.getLogger().severe("Crafting using non-standared recipe, unhandled by plugin!");
			return;
		}

		plugin.getLogger().info("Inventory Action: "+evt.getAction().name()+", ClickType: "+evt.getClick());
		int numCraft = 1;

		ItemStack output = evt.getRecipe().getResult();
		crafter.handleProcess(evt, ingredients, output, evt.getWhoClicked().getUniqueId(), numCraft);
	}*/
}