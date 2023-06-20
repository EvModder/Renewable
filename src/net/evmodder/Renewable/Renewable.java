package net.evmodder.Renewable;

import net.evmodder.EvLib.EvPlugin;
import net.evmodder.Renewable.commands.*;
import net.evmodder.Renewable.listeners.*;

public class Renewable extends EvPlugin{
	private static Renewable plugin; public static Renewable getPlugin(){return plugin;}
	private RenewableAPI api;
	public RenewableAPI getAPI(){return api;}

	@Override public void onEvEnable(){
		plugin = this;
		api = new RenewableAPI(this);
		api.standardizer.loadFractionalRescues();

		//register listeners
		registerListeners();

		//register commands
		new CommandRenewable(this);
	}

	@Override public void onEvDisable(){api.standardizer.saveFractionalRescues();}

	void registerListeners(){
		getServer().getPluginManager().registerEvents(new BlockDeathListener(), this);//blocks
		getServer().getPluginManager().registerEvents(new BlockMineListener(), this);
		getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);

		getServer().getPluginManager().registerEvents(new ItemCraftListener(), this);//items
		getServer().getPluginManager().registerEvents(new ItemDeathListener(), this);
		getServer().getPluginManager().registerEvents(new ItemConsumeListener(), this);
		getServer().getPluginManager().registerEvents(new ItemSmeltListener(), this);
		if(!config.getString("punish-command").isEmpty())
			getServer().getPluginManager().registerEvents(new ItemTrackingListener(), this);

//		getServer().getPluginManager().registerEvents(new VillagerTradeListener_UNUSED(), this);//mobs
//		getServer().getPluginManager().registerEvents(new EntityInteractListener(), this);
//		if(api.isUnrenewable(new ItemStack(Material.SHULKER_SHELL))){
//			getServer().getPluginManager().registerEvents(new MobDeathListener_UNUSED(), this);
//			if((config.getBoolean("creative-mode-ignore", true)
//					|| config.getBoolean("creative-unrenewable-supply"))
//					|| api.isUnrenewable(new ItemStack(Material.BAT_SPAWN_EGG)))
//				getServer().getPluginManager().registerEvents(new MobSpawnListener_UNUSED(), this);
//		}
		if(config.getBoolean("punish-for-irreversible-process", true))
			getServer().getPluginManager().registerEvents(new StonecutterListener(), this);//TODO: implement!!
	}
}