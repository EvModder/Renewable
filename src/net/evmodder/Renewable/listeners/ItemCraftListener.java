package net.evmodder.Renewable.listeners;

import java.util.Arrays;
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

		Collection<ItemStack> ingredients;
		if(evt.getRecipe() instanceof ShapelessRecipe){
			//TODO: doesn't detect firework diamonds!
			//ingredients = ((ShapelessRecipe)evt.getRecipe()).getIngredientList();
			//TODO: test that THIS detects diamonds:
			ingredients = Arrays.asList(evt.getClickedInventory().getContents());
		}
		else if(evt.getRecipe() instanceof ShapedRecipe)
			ingredients = ((ShapedRecipe)evt.getRecipe()).getIngredientMap().values();
		else{
			plugin.getLogger().severe("Crafting using non-standared recipe, unhandled by plugin!: "+evt.getRecipe().getClass().getName());
			return;
		}

		plugin.getLogger().fine("Inventory Action: "+evt.getAction().name()+", ClickType: "+evt.getClick());
		int numCraft = 1;

		ItemStack output = evt.getRecipe().getResult();
		crafter.handleProcess(evt, ingredients, output, evt.getWhoClicked().getUniqueId(), numCraft);
	}
}