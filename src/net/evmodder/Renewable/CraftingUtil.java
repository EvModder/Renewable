package net.evmodder.Renewable;

import java.util.Collection;
import java.util.UUID;
import java.util.Vector;
import org.bukkit.Material;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

public class CraftingUtil{
	final Renewable plugin;
	final boolean RESCUE_ITEMS, PUNISH_UNRENEWABLE_PROCESS, PREVENT_UNRENEWABLE_PROCESS;

	public CraftingUtil(){
		plugin = Renewable.getPlugin();
		RESCUE_ITEMS = plugin.getConfig().getBoolean("rescue-items", true);
		PUNISH_UNRENEWABLE_PROCESS = plugin.getConfig().getBoolean("punish-for-irreversible-process", true);
		PREVENT_UNRENEWABLE_PROCESS = plugin.getConfig().getBoolean("prevent-irreversible-process", false);
	}

	public void handleProcess(Cancellable evt, Collection<ItemStack> inputs, ItemStack output, UUID player, boolean craftAll){
		Vector<ItemStack> unrenewIngr = new Vector<ItemStack>();
		// Assumes recipes will never require stacked ingredients
		int amtCrafted = craftAll ? 99999 : 1;
		for(ItemStack ingr : inputs){
			if(ingr != null && ingr.getType() != Material.AIR){
				amtCrafted = Math.min(amtCrafted, ingr.getAmount());
				if(plugin.getAPI().isUnrenewable(ingr)) unrenewIngr.add(ingr);
			}
		}
		for(ItemStack ingr : unrenewIngr) ingr.setAmount(amtCrafted);
		output.setAmount(amtCrafted);

		if(!plugin.getAPI().isUnrenewable(output)){
			for(ItemStack ingr : unrenewIngr){
				plugin.getAPI().punish(player, ingr.getType());
				if(RESCUE_ITEMS && !PREVENT_UNRENEWABLE_PROCESS) plugin.getAPI().rescueItem(ingr);
			}
			if(PREVENT_UNRENEWABLE_PROCESS){
				evt.setCancelled(true);
				return;
			}
		}
		else{
			final ItemStack stdOutput = plugin.getAPI().standardizer.standardize(output, /*add=*/false);
			int amtLeft = output.getAmount(), stdAmtLeft = stdOutput.getAmount();
			Vector<ItemStack> destroyed = new Vector<>(), destroyedStd = new Vector<>();

//			plugin.getLogger().info("Amt of output: "+amtLeft);
//			plugin.getLogger().info("Amt of stdOutput: "+stdAmtLeft);

			for(ItemStack ingr : unrenewIngr){
				if(!plugin.getAPI().isUnrenewableProcess(ingr, output)){
					if(PREVENT_UNRENEWABLE_PROCESS) evt.setCancelled(true);
					if(PUNISH_UNRENEWABLE_PROCESS) plugin.getAPI().punish(player, ingr.getType());
				}

				ItemStack stdIngr = plugin.getAPI().standardizer.standardize(ingr, /*add=*/true);
				int amt = ingr.getAmount(), stdAmt = stdIngr.getAmount();

//				plugin.getLogger().info("Amt of ingr: "+amt);
//				plugin.getLogger().info("Amt of stdIngr: "+stdAmt);

				if(ingr.getType().equals(output.getType())){
					if(amtLeft >= amt){amtLeft -= amt; amt = 0;}
					else{ amt -= amtLeft; amtLeft = 0; }
				}
				if(stdIngr.getType().equals(stdOutput.getType())){
					if(stdAmtLeft >= stdAmt){stdAmtLeft -= stdAmt; stdAmt = 0;}
					else{stdAmt -= stdAmtLeft; stdAmtLeft = 0;}
				}
				if(amt > 0){
					ingr.setAmount(amt);
					destroyed.add(ingr);
					if(stdAmt > 0){
						stdIngr.setAmount(stdAmt);
						destroyedStd.add(stdIngr);
					}
				}
			}
//			plugin.getLogger().info("Left: "+amtLeft);
//			plugin.getLogger().info("Left std: "+stdAmtLeft);

			if(!evt.isCancelled() && !destroyedStd.isEmpty()){
				for(ItemStack ingrStd : destroyedStd){
					if(RESCUE_ITEMS) plugin.getAPI().rescueItem(ingrStd);
				}
				for(ItemStack ingr : destroyed){
					plugin.getAPI().punish(player, ingr.getType());
				}
			}
		}
	}
}