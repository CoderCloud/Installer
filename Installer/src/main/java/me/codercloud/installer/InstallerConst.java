package me.codercloud.installer;

import me.codercloud.installer.util.BaseUtil;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class InstallerConst {	
	
	/**
	 * Permissions
	 */
	public static final String PERM_HELP = "installer.help";
	public static final String PERM_INSTALL = "installer.install";
	public static final String PERM_PLUGINMANAGER = "installer.pluginmanager";
	public static final String PERM_ENABLE = "installer.enable";
	public static final String PERM_DISABLE = "installer.disable";
	public static final String PERM_UNINSTALL = "installer.uninstall";
	public static final String PERM_INSTALL_UNSAFE = "installer.urlinstall";
	
	
	/**
	 * Items
	 */
	public static final ItemStack getNotAvailableItem(String ... lore) {
		if(lore == null)
			lore = new String[0];
		for(int i = 0; i<lore.length; i++) {
			lore[i] = ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + lore[i];
		}
		return BaseUtil.setNameAndLore(new ItemStack(Material.WEB), ChatColor.WHITE.toString() + ChatColor.UNDERLINE.toString() + "Not Available", lore);
	}
	
	public static final ItemStack getNoPermissionItem() {
		return InstallerConst.getNotAvailableItem(ChatColor.GRAY + "Missing permission");
	}
}
