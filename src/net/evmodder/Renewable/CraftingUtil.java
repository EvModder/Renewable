package net.evmodder.Renewable;

import java.util.Collection;
import java.util.UUID;
import java.util.Vector;
import org.bukkit.Material;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;
import net.evmodder.Renewable.RenewableStandardizer.ItemWithFractionAmt;

public class CraftingUtil{
	final private Renewable pl;
	final private boolean RESCUE_ITEMS, PREVENT_IRREVERSIBLE_PROCESS;

	public CraftingUtil(){
		pl = Renewable.getPlugin();
		RESCUE_ITEMS = pl.getConfig().getBoolean("rescue-items", true);
		PREVENT_IRREVERSIBLE_PROCESS = pl.getConfig().getBoolean("prevent-irreversible-process", false);
	}

	public void handleProcess(Cancellable evt, Collection<ItemStack> inputs, ItemStack output, UUID player, boolean craftAll, int resultMult){
		Vector<ItemStack> unrenewIngr = new Vector<ItemStack>();
		// Assumes recipes will never require stacked ingredients
		int amtCrafted = craftAll ? 99999 : 1;
		for(ItemStack ingr : inputs){
			if(ingr != null && ingr.getType() != Material.AIR){
				amtCrafted = Math.min(amtCrafted, ingr.getAmount());
				if(pl.getAPI().isUnrenewable(ingr)) unrenewIngr.add(ingr.clone());
			}
		}
		for(ItemStack ingr : unrenewIngr) ingr.setAmount(amtCrafted);

		if(!pl.getAPI().isUnrenewable(output)){ // Renewable output
			for(ItemStack ingr : unrenewIngr){
				pl.getAPI().punishDestroyed(player, ingr.getType());
				if(RESCUE_ITEMS) pl.getAPI().rescueItem(ingr);
			}
		}
		else{
			output = output.clone();
			output.setAmount(amtCrafted*resultMult);
			final ItemWithFractionAmt[] stdOutput = pl.getAPI().standardizer.standardize(output);
			int amtLeft = output.getAmount()/*, stdAmtLeft = stdOutput.getAmount()*/;
			Vector<ItemStack> destroyed = new Vector<>(), destroyedStd = new Vector<>();

//			pl.getLogger().info("Amt of output: "+amtLeft);
//			pl.getLogger().info("Amt of stdOutput: "+stdAmtLeft);

			for(ItemStack ingr : unrenewIngr){
				if(pl.getAPI().isUnrenewableProcess(ingr, output)){
					if(PREVENT_IRREVERSIBLE_PROCESS) evt.setCancelled(true);
//					pl.getLogger().info("irreversible: "+ingr.getType()+" -> "+output.getType());
					pl.getAPI().punishIrreversible(player, ingr.getType());
				}
//				else pl.getLogger().info("non-irreversible: "+ingr.getType()+" -> "+output.getType());

				final ItemWithFractionAmt[] stdIngr = pl.getAPI().standardizer.standardize(ingr);
				int amt = ingr.getAmount()/*, stdAmt = stdIngr.getAmount()*/;

//				pl.getLogger().info("Amt of ingr: "+amt);
//				pl.getLogger().info("Amt of stdIngr: "+stdAmt);

				if(ingr.getType() == output.getType()){
//					pl.getLogger().info("type match: "+ingr.getType());
					if(amtLeft >= amt){amtLeft -= amt; amt = 0;}
					else{amt -= amtLeft; amtLeft = 0;}
				}
				for(ItemWithFractionAmt o : stdOutput){
					for(ItemWithFractionAmt i : stdIngr){
						if(o.item.getType() == i.item.getType()){
//							pl.getLogger().info("std-type match: "+o.item.getType());
							if(o.amt.compareTo(i.amt) >= 0){ // o.amt > i.amt
								o.amt.add(-i.amt.getNumerator(), i.amt.getDenominator());//o.amt -= i.amt;
								i.amt.add(-i.amt.getNumerator(), i.amt.getDenominator());//i.amt = 0;
							}
							else{
								i.amt.add(-o.amt.getNumerator(), o.amt.getDenominator());//i.amt -= o.amt
								o.amt.add(-o.amt.getNumerator(), o.amt.getDenominator());//o.amt = 0
							}
						}
					}
				}
//				if(stdIngr.getType() == stdOutput.getType()){
//					pl.getLogger().info("std-type match: "+stdIngr.getType());
//					if(stdAmtLeft >= stdAmt){stdAmtLeft -= stdAmt; stdAmt = 0;}
//					else{stdAmt -= stdAmtLeft; stdAmtLeft = 0;}
//				}
				if(amt > 0){
//					pl.getLogger().info("adding destroyed");
//					if(stdAmt > 0){
//						pl.getLogger().info("adding std destroyed");
//						stdIngr.setAmount(stdAmt);
//						destroyedStd.add(stdIngr);
//					}
					boolean unfilled = false;
					for(ItemWithFractionAmt i : stdIngr){
						if(i.amt.getNumerator() > 0){
//							pl.getLogger().info("adding std destroyed");
							i.item.setAmount(i.amt.take1s());
							destroyedStd.add(i.item);
							unfilled = true;
							if(RESCUE_ITEMS && i.amt.getNumerator() > 0){
								pl.getLogger().info("rescuing partial std ingr: "+i.item.getType());
								pl.getAPI().standardizer.addRescuedParts(i.item.getType(), i.amt.getNumerator(), i.amt.getDenominator());
							}
						}
					}
					if(unfilled){
						ingr.setAmount(amt);
						destroyed.add(ingr);
					}
				}
			}
//			pl.getLogger().info("Left: "+amtLeft);
//			pl.getLogger().info("Left std: "+stdAmtLeft);

			if(!evt.isCancelled() && !destroyedStd.isEmpty()){
				for(ItemStack ingrStd : destroyedStd){
//					pl.getLogger().info("rescuing: "+ingrStd.getType());
					if(RESCUE_ITEMS) pl.getAPI().rescueItem(ingrStd);
				}
				for(ItemStack ingr : destroyed){
//					pl.getLogger().info("punishing: "+ingr.getType());
					pl.getAPI().punishDestroyed(player, ingr.getType());
				}
			}
		}
	}
}