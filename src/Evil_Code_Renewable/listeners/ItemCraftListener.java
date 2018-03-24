package Evil_Code_Renewable.listeners;

import java.util.Collection;
import java.util.HashSet;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import Evil_Code_Renewable.Fraction;
import Evil_Code_Renewable.Renewable;
import Evil_Code_Renewable.Utils;

public class ItemCraftListener implements Listener{
	Renewable plugin;
	boolean saveItems, punishUnrenewableProcess, preventUnrenewableProcess, normalizeRescuedItems;

	public ItemCraftListener(){
		plugin = Renewable.getPlugin();
		saveItems = plugin.getConfig().getBoolean("rescue-items", true);
		punishUnrenewableProcess = plugin.getConfig().getBoolean("punish-for-irreversible-process", true);
		preventUnrenewableProcess = plugin.getConfig().getBoolean("prevent-irreversible-process", false);
		normalizeRescuedItems = plugin.getConfig().getBoolean("standardize-rescued-items", true);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemCraft(CraftItemEvent evt){
		if(evt.isCancelled() || evt.getWhoClicked().getGameMode() == GameMode.CREATIVE) return;

		Collection<ItemStack> ingredients;
		if(evt.getRecipe() instanceof ShapelessRecipe)
			ingredients = ((ShapelessRecipe)evt.getRecipe()).getIngredientList();
		else if(evt.getRecipe() instanceof ShapedRecipe)
			ingredients = ((ShapedRecipe)evt.getRecipe()).getIngredientMap().values();
		else{
			plugin.getLogger().severe("Crafting using non-standared recipe, unhandled by plugin!");
			return;
		}

		plugin.getLogger().info("Inventory Action: "+evt.getAction().name()+", ClickType: "+evt.getClick());
		int numCraft = 1;

		HashSet<ItemStack> destroyed = new HashSet<ItemStack>();
		ItemStack output = evt.getRecipe().getResult();
		ItemStack stdOutput = Utils.standardize(output);
		int amtLeft = stdOutput.getAmount() * numCraft;

		for(ItemStack ingr : ingredients){
			if(ingr != null && ingr.getType() != Material.AIR && Utils.isUnrenewable(ingr)){
				ItemStack temp = evt.getRecipe().getResult().clone();
				int gcd = Fraction.GCD(ingr.getAmount(), temp.getAmount());
				ingr.setAmount(ingr.getAmount()/gcd);
				temp.setAmount(temp.getAmount()/gcd);
				if(Utils.isUnrenewableProcess(ingr, temp)){
					if(punishUnrenewableProcess){
						plugin.punish(evt.getWhoClicked().getUniqueId(), ingr.getType());
					}
					if(preventUnrenewableProcess){
						evt.setCancelled(true);
						return;
					}
				}

				if(amtLeft == 0) destroyed.add(ingr);
				else{
					ItemStack stdIngr = normalizeRescuedItems ? Utils.standardize(ingr) : ingr;
					int amtStdIngr = stdIngr.getAmount();
					stdIngr.setAmount(stdOutput.getAmount());
					if(stdIngr.equals(stdOutput)){
						if(amtStdIngr <= amtLeft) amtLeft -= amtStdIngr;
						else{
							stdIngr.setAmount(amtStdIngr - amtLeft);
							destroyed.add(stdIngr);
							amtLeft = 0;
						}
					}
					else destroyed.add(ingr);
				}
			}
		}
		for(ItemStack ingr : destroyed){
			plugin.punish(evt.getWhoClicked().getUniqueId(), ingr.getType());
			if(saveItems){
				ingr.setAmount(ingr.getAmount()*numCraft);
				plugin.rescueItem(ingr);
			}
		}
	}
}