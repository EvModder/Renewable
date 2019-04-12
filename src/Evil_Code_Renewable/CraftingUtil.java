package Evil_Code_Renewable;

import java.util.Collection;
import java.util.UUID;
import java.util.Vector;
import org.bukkit.Material;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

public class CraftingUtil{
	final Renewable plugin;
	final boolean saveItems, punishUnrenewableProcess, preventUnrenewableProcess;

	public CraftingUtil(){
		plugin = Renewable.getPlugin();
		saveItems = plugin.getConfig().getBoolean("rescue-items", true);
		punishUnrenewableProcess = plugin.getConfig().getBoolean("punish-for-irreversible-process", true);
		preventUnrenewableProcess = plugin.getConfig().getBoolean("prevent-irreversible-process", false);
	}

	public void handleProcess(Cancellable evt, Collection<ItemStack> inputs, ItemStack output, UUID player, int numCraft){
		Vector<ItemStack> destroyed = new Vector<ItemStack>();
		if(!plugin.getAPI().isUnrenewable(output)) for(ItemStack ingr : inputs){
			if(ingr != null && ingr.getType() != Material.AIR && plugin.getAPI().isUnrenewable(ingr)){
				ingr.setAmount(numCraft * ingr.getAmount());
				destroyed.add(ingr);
			}
		}
		else{
			ItemStack stdOutput = plugin.getAPI().standardizer.standardize(output, true);
			int amtLeft = output.getAmount() * numCraft;
			int stdAmtLeft = stdOutput.getAmount() * numCraft;
			Vector<Material> gotStandardized = new Vector<Material>();

			plugin.getLogger().info("Value of output: "+amtLeft);
			plugin.getLogger().info("Value of stdOutput: "+stdAmtLeft);

			for(ItemStack ingr : inputs){
				if(ingr != null && ingr.getType() != Material.AIR && plugin.getAPI().isUnrenewable(ingr)){
					if(plugin.getAPI().isUnrenewableProcess(ingr, output) == false) continue;
					ItemStack stdIngr = plugin.getAPI().standardizer.standardize(ingr, true);
					int amt = ingr.getAmount(), stdAmt = stdIngr.getAmount();

					plugin.getLogger().info("Value of ingr: "+amt);
					plugin.getLogger().info("Value of stdIngr: "+stdAmt);

					if(ingr.getType().equals(output.getType())){
						if(amtLeft >= amt){
							amtLeft -= amt;
							amt = 0;
						}
						else{
							amt -= amtLeft;
							amtLeft = 0;
						}
					}
					if(stdIngr.getType().equals(stdOutput.getType())){
						if(stdAmtLeft >= stdAmt){
							stdAmtLeft -= stdAmt;
							stdAmt = 0;
						}
						else{
							stdAmt -= stdAmtLeft;
							stdAmtLeft = 0;
						}
					}
					if(amt > 0){
						if(stdAmt > 0){
							stdIngr.setAmount(numCraft * stdAmt);
							destroyed.add(stdIngr);
						}
						else gotStandardized.add(ingr.getType());
					}
				}
			}
			plugin.getLogger().info("Left: "+amtLeft);
			plugin.getLogger().info("Left std: "+stdAmtLeft);
			if(destroyed.isEmpty()){
				for(Material ingr : gotStandardized){
					if(punishUnrenewableProcess){
						plugin.getAPI().punish(player, ingr);
					}
					if(preventUnrenewableProcess){
						evt.setCancelled(true);
					}
				}
				return;
			}
		}
		for(ItemStack ingr : destroyed){
			plugin.getAPI().punish(player, ingr.getType());
			if(saveItems){
				ingr.setAmount(ingr.getAmount()*numCraft);
				plugin.getAPI().rescueItem(ingr);
			}
		}
	}
}