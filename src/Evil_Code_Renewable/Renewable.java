 package Evil_Code_Renewable;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import Evil_Code_Renewable.commands.*;
import Evil_Code_Renewable.listeners.*;
import EvLib.EvPlugin;

public class Renewable extends EvPlugin{
	//Possible prize: Retain unrenewable items on death
	private static Renewable plugin; public static Renewable getPlugin(){return plugin;}
	private String punishCommand;
	private Location rescueLoc;
	private boolean normalizeRescuedItems, punishForRenewable;

	@Override public void onEvEnable(){
		plugin = this;
		new Utils(this);
		Utils.loadFractionalRescues();

		//read config
		punishCommand = config.getString("punish-command");
		String[] data = config.getString("store-items-at").split(",");
		normalizeRescuedItems = config.getBoolean("standardize-rescued-items", true);
		punishForRenewable = config.getBoolean("punish-rescued-renewables", false);
		World world = getServer().getWorld(data[0]);
		if(world != null) rescueLoc = new Location(world,
				Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]));

		if(world == null || rescueLoc == null){
			getLogger().warning("Unable to generate rescue location for destroyed items!");
			rescueLoc = null;
		}

		//register listeners
		getServer().getPluginManager().registerEvents(new BlockDeathListener(), this);//blocks
		getServer().getPluginManager().registerEvents(new BlockMineListener(), this);
		getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
		getServer().getPluginManager().registerEvents(new BucketEmptyListener(), this);

		getServer().getPluginManager().registerEvents(new FeedHorseListener(), this);//items
		getServer().getPluginManager().registerEvents(new ItemCraftListener(), this);
		getServer().getPluginManager().registerEvents(new ItemDeathListener(), this);
		getServer().getPluginManager().registerEvents(new ItemSmeltListener(), this);
		getServer().getPluginManager().registerEvents(new VillagerTradeListener(), this);

		if(config.getBoolean("renewable-mob-drops", false) == false)
			getServer().getPluginManager().registerEvents(new MobDeathListener(), this);//mobs

		//register commands
		new CommandRenewable(this);

		//Recipes for lava buckets & dirt to gravel
		loadRecipes();
	}

	@Override public void onEvDisable(){
		Utils.saveFractionalRescues();
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

	public void punish(UUID uuid, Material mat){
		//These items are marked as unrenewable so that they will be rescued, but aren't actually unrenewable
		if(!punishForRenewable && Utils.rescueList.contains(mat)) return /*false*/;

		if(uuid == null){
			getLogger().info("Unrenewable item destroyed, no player detected!");
//			return false;
		}
		else{
//			boolean success = true;
			for(String command : punishCommand.split("\n")){
				command = command.replaceAll("%name%", getServer().getPlayer(uuid).getName())
								.replaceAll("%type%", mat.name());
				getLogger().info("Executing command: "+command);
				if(!getServer().dispatchCommand(getServer().getConsoleSender(), command))/*success = false*/;
			}
//			return success;
		}
	}

	public void rescueItem(ItemStack item){
		if(rescueLoc == null) return;
		getLogger().fine("Rescuing: "+item.getType());

		if(item.getType() == Material.WRITTEN_BOOK){
			BookMeta meta = (BookMeta) item.getItemMeta();
			meta.setGeneration(Generation.TATTERED);
			item.setItemMeta(meta);
		}
		else if(normalizeRescuedItems){
			item = Utils.standardize(item);
			getLogger().fine("Standardized: "+item.getType());
		}

		Block block = rescueLoc.getBlock();
		if(block.getType() != Material.CHEST) block.setType(Material.CHEST);
		Chest chest = (Chest) block.getState();
		HashMap<Integer, ItemStack> extras = chest.getBlockInventory().addItem(item);
		while(!extras.isEmpty() && block.getY() < 256){
			block = block.getRelative(BlockFace.UP);
			if(block.getType() != Material.CHEST) break;
			chest = (Chest) block.getState();
			extras = chest.getBlockInventory().addItem(extras.values().toArray(new ItemStack[]{}));
		}
		if(!extras.isEmpty()){
			for(ItemStack extra : extras.values()) block.getWorld().dropItem(rescueLoc, extra);
		}
	}
}