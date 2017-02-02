package me.codercloud.installer.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class UtilConstants {
	
	public static ItemStack getPageIndicatorItem(int page, int maxPage) {
		return BaseUtil.setName(new ItemStack(Material.NAME_TAG), ChatColor.BLUE + "Page (" + page + "/" + maxPage + ")");
	}
	
	public static ItemStack getNextPageItem(int page) {
		return BaseUtil.setName(new ItemStack(Material.PAPER), ChatColor.BLUE + "Next page (" + (page+1) + ")");
	}

	public static ItemStack getPrevPageItem(int page) {
		return BaseUtil.setName(new ItemStack(Material.PAPER), ChatColor.BLUE + "Previous page (" + (page-1) + ")");
	}
	
}
