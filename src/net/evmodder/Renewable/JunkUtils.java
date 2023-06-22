package net.evmodder.Renewable;

import org.bukkit.Material;

public abstract interface JunkUtils{
	public static boolean isPotterySherd(Material mat){
		switch(mat){
			case ANGLER_POTTERY_SHERD:
			case ARCHER_POTTERY_SHERD:
			case ARMS_UP_POTTERY_SHERD:
			case BLADE_POTTERY_SHERD:
			case BREWER_POTTERY_SHERD:
			case BURN_POTTERY_SHERD:
			case DANGER_POTTERY_SHERD:
			case EXPLORER_POTTERY_SHERD:
			case FRIEND_POTTERY_SHERD:
			case HEART_POTTERY_SHERD:
			case HEARTBREAK_POTTERY_SHERD:
			case HOWL_POTTERY_SHERD:
			case MINER_POTTERY_SHERD:
			case MOURNER_POTTERY_SHERD:
			case PLENTY_POTTERY_SHERD:
			case PRIZE_POTTERY_SHERD:
			case SHEAF_POTTERY_SHERD:
			case SHELTER_POTTERY_SHERD:
			case SKULL_POTTERY_SHERD:
			case SNORT_POTTERY_SHERD:
				return true;
			default:
				return false;
		}
	}
}