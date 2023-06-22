package net.evmodder.Renewable;

import java.util.Collection;
import java.util.UUID;
import java.util.Vector;
import org.bukkit.Material;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

public class CraftingUtil{
	final private Renewable pl;
	final private boolean RESCUE_ITEMS, PREVENT_IRREVERSIBLE_PROCESS;

	public CraftingUtil(){
		pl = Renewable.getPlugin();
		RESCUE_ITEMS = pl.getConfig().getBoolean("rescue-items", true);
		PREVENT_IRREVERSIBLE_PROCESS = pl.getConfig().getBoolean("prevent-irreversible-process", false);
	}

	public void handleProcess(Cancellable evt, Collection<ItemStack> inputs, ItemStack output, UUID player, boolean craftAll){
		Vector<ItemStack> unrenewIngr = new Vector<ItemStack>();
		// Assumes recipes will never require stacked ingredients
		int amtCrafted = craftAll ? 99999 : 1;
		for(ItemStack ingr : inputs){
			if(ingr != null && ingr.getType() != Material.AIR){
				amtCrafted = Math.min(amtCrafted, ingr.getAmount());
				if(pl.getAPI().isUnrenewable(ingr)) unrenewIngr.add(ingr);
			}
		}
		for(ItemStack ingr : unrenewIngr) ingr.setAmount(amtCrafted);
		output.setAmount(amtCrafted);

		if(!pl.getAPI().isUnrenewable(output)){ // Renewable output
			for(ItemStack ingr : unrenewIngr){
				pl.getAPI().punishDestroyed(player, ingr.getType());
				if(RESCUE_ITEMS) pl.getAPI().rescueItem(ingr);
			}
		}
		else{
			final ItemStack stdOutput = pl.getAPI().standardizer.standardize(output, /*mult=*/0);
			int amtLeft = output.getAmount(), stdAmtLeft = stdOutput.getAmount();
			Vector<ItemStack> destroyed = new Vector<>(), destroyedStd = new Vector<>();

//			plugin.getLogger().info("Amt of output: "+amtLeft);
//			plugin.getLogger().info("Amt of stdOutput: "+stdAmtLeft);

			for(ItemStack ingr : unrenewIngr){
				if(!pl.getAPI().isUnrenewableProcess(ingr, output)){
					if(PREVENT_IRREVERSIBLE_PROCESS) evt.setCancelled(true);
					pl.getAPI().punishIrreversible(player, ingr.getType());
				}

				ItemStack stdIngr = pl.getAPI().standardizer.standardize(ingr, /*mult=*/0);
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
					if(RESCUE_ITEMS) pl.getAPI().rescueItem(ingrStd);
				}
				for(ItemStack ingr : destroyed){
					pl.getAPI().punishDestroyed(player, ingr.getType());
				}
			}
		}
	}
}