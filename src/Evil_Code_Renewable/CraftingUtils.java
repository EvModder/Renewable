package Evil_Code_Renewable;

import java.util.Collection;
import java.util.UUID;
import java.util.Vector;
import org.bukkit.Material;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

public class CraftingUtils{
	final Renewable plugin;
	final boolean saveItems, punishUnrenewableProcess, preventUnrenewableProcess;

	public CraftingUtils(){
		plugin = Renewable.getPlugin();
		saveItems = plugin.getConfig().getBoolean("rescue-items", true);
		punishUnrenewableProcess = plugin.getConfig().getBoolean("punish-for-irreversible-process", true);
		preventUnrenewableProcess = plugin.getConfig().getBoolean("prevent-irreversible-process", false);
	}

	@SuppressWarnings("deprecation")
	public void handleProcess(Cancellable evt, Collection<ItemStack> ingredients, ItemStack output, UUID player, int numCraft){
		Vector<ItemStack> destroyed = new Vector<ItemStack>();
		if(!Utils.isUnrenewable(output)) for(ItemStack ingr : ingredients){
			if(ingr != null && ingr.getType() != Material.AIR && Utils.isUnrenewable(ingr)){
				ingr.setAmount(numCraft * ingr.getAmount());
				destroyed.add(ingr);
			}
		}
		else{
			ItemStack stdOutput = Utils.standardize(output, true);
			int amtLeft = output.getAmount() * numCraft;
			int stdAmtLeft = stdOutput.getAmount() * numCraft;
			ItemDesc outDesc = new ItemDesc(output.getType(), output.getData().getData());
			ItemDesc stdOutDesc = new ItemDesc(stdOutput.getType(), stdOutput.getData().getData());
			Vector<Material> gotStandardized = new Vector<Material>();

			plugin.getLogger().info("Value of output: "+amtLeft);
			plugin.getLogger().info("Value of stdOutput: "+stdAmtLeft);

			for(ItemStack ingr : ingredients){
				if(ingr != null && ingr.getType() != Material.AIR && Utils.isUnrenewable(ingr)){
					if(Utils.isUnrenewableProcess(ingr, output) == false) continue;
					ItemStack stdIngr = Utils.standardize(ingr, true);
					ItemDesc ingrDesc = new ItemDesc(ingr.getType(), ingr.getData().getData());
					ItemDesc stdIngrDesc = new ItemDesc(stdIngr.getType(), stdIngr.getData().getData());
					int amt = ingr.getAmount(), stdAmt = stdIngr.getAmount();

					plugin.getLogger().info("Value of ingr: "+amt);
					plugin.getLogger().info("Value of stdIngr: "+stdAmt);

					if(ingrDesc.equals(outDesc)){
						if(amtLeft >= amt){
							amtLeft -= amt;
							amt = 0;
						}
						else{
							amt -= amtLeft;
							amtLeft = 0;
						}
					}
					if(stdIngrDesc.equals(stdOutDesc)){
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
						plugin.punish(player, ingr);
					}
					if(preventUnrenewableProcess){
						evt.setCancelled(true);
					}
				}
				return;
			}
		}
		for(ItemStack ingr : destroyed){
			plugin.punish(player, ingr.getType());
			if(saveItems){
				ingr.setAmount(ingr.getAmount()*numCraft);
				plugin.rescueItem(ingr);
			}
		}
	}
}