package net.evmodder.Renewable;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import net.evmodder.EvLib.EvPlugin;
import net.evmodder.Renewable.commands.*;
import net.evmodder.Renewable.listeners.*;

public class Renewable extends EvPlugin{
	private static Renewable plugin; public static Renewable getPlugin(){return plugin;}
	private RenewableAPI api;

	@Override public void onEvEnable(){
		plugin = this;
		api = new RenewableAPI(this);
		api.standardizer.loadFractionalRescues();

		//register listeners
		registerListeners();

		//register commands
		new CommandRenewable(this);

		//Recipes for lava buckets & dirt to gravel
		loadRecipes();
	}

	@Override public void onEvDisable(){api.standardizer.saveFractionalRescues();}

	public RenewableAPI getAPI(){return api;}

	void registerListeners(){
		getServer().getPluginManager().registerEvents(new BlockDeathListener(), this);//blocks
		getServer().getPluginManager().registerEvents(new BlockMineListener(), this);
		getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
		getServer().getPluginManager().registerEvents(new BucketEmptyListener(), this);

		getServer().getPluginManager().registerEvents(new ItemCraftListener(), this);//items
		getServer().getPluginManager().registerEvents(new ItemDeathListener(), this);
		getServer().getPluginManager().registerEvents(new ItemSmeltListener(), this);
		if(!config.getString("punish-command").isEmpty())
			getServer().getPluginManager().registerEvents(new ItemTrackingListener(), this);

		getServer().getPluginManager().registerEvents(new VillagerTradeListener(), this);//mobs
		getServer().getPluginManager().registerEvents(new EntityInteractListener(), this);
		if(api.isUnrenewable(new ItemStack(Material.SHULKER_SHELL))){
			getServer().getPluginManager().registerEvents(new MobDeathListener(), this);
			if((config.getBoolean("creative-mode-ignore", true) || config.getBoolean("creative-unrenewable-supply"))
					|| api.isUnrenewable(new ItemStack(Material.BAT_SPAWN_EGG)))
				getServer().getPluginManager().registerEvents(new MobSpawnListener(), this);
		}
	}

	void loadRecipes(){
		try{
			if(config.getBoolean("renewable-lava")){
				getServer().addRecipe(
						new ShapelessRecipe(new NamespacedKey(this, "lava_bucket"),
						new ItemStack(Material.LAVA_BUCKET))
						.addIngredient(Material.OBSIDIAN)
						.addIngredient(Material.BUCKET));
				getServer().addRecipe(
						new ShapelessRecipe(new NamespacedKey(this, "lava_bucket2"),
						new ItemStack(Material.LAVA_BUCKET))
						.addIngredient(Material.BLAZE_POWDER)
						.addIngredient(Material.BUCKET));
			}
			if(config.getBoolean("dirt-to-gravel"))
				getServer().addRecipe(
						new ShapelessRecipe(new NamespacedKey(this, "gravel_recipe"),
						new ItemStack(Material.GRAVEL))
						.addIngredient(Material.DIRT)
						.addIngredient(Material.COBBLESTONE));
		}
		catch(IllegalStateException ex){
			plugin.getLogger().warning("Tried to load recipes that were already loaded!");
			return;
		}
	}
}