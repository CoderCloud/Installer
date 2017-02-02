package me.codercloud.installer.manager;

import me.codercloud.installer.InstallerConst;
import me.codercloud.installer.InstallerPlugin;
import me.codercloud.installer.util.BaseUtil;
import me.codercloud.installer.util.tasks.MenuTask.MenuPoint;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class PluginOverview extends MenuPoint {
	
	private final Plugin plugin;
	private boolean enabledD = false;
	
	public PluginOverview(Plugin p) {
		super(3, ChatColor.BLUE + p.getName());
		this.plugin = p;
	}
	
	private void updateDisplayed() {
		boolean change = false;
		if(enabledD != plugin.isEnabled()) {
			enabledD = plugin.isEnabled();
			change = true;
		}
		
		if(change)
			update();
	}
	
	@Override
	public boolean onDoubleClick(int slot) {
		boolean upd = false;
		if(slot == 24 && hasPermission(InstallerConst.PERM_ENABLE)) {
			if(plugin.isEnabled() && !(plugin instanceof InstallerPlugin))
				Bukkit.getPluginManager().disablePlugin(plugin);
			upd = true;
			PluginManager.updateOptions();
		} else if(slot == 25 && hasPermission(InstallerConst.PERM_DISABLE)) {
			if(!plugin.isEnabled())
				Bukkit.getPluginManager().enablePlugin(plugin);
			upd = true;
			PluginManager.updateOptions();
		}
		return upd;
	}
	
	@Override
	public boolean onLeftClick(int slot, boolean shift) {
		boolean upd = false;
		if(slot == 18) {
			setNext(new PluginManager());
		}
		return upd;
	}
	
	public boolean onQClick(int slot, boolean ctrl) {
		if(slot == 20 && ctrl && hasPermission(InstallerConst.PERM_UNINSTALL) && !(plugin instanceof InstallerPlugin)) {
			setNext(new UninstallPlugin(plugin));
		}
		return false;
	};
	
	@Override
	public ItemStack updateItem(int slot) {
		updateDisplayed();
		if(slot == 0) {
			return getEnabledIndicator();
		} else if(slot == 20) {
			return getUninstallButton();
		} else if(slot == 24) {
			return getDisableButton();
		} else if(slot == 25) {
			return getEnableButton();
		} else if(slot == 18) {
			return getBackButton();
		}
		return null;
	}
	
	public ItemStack getEnabledIndicator() {
		if(enabledD)
			return BaseUtil.setName(new ItemStack(Material.INK_SACK, 1, (short) 10), ChatColor.DARK_GREEN + "Enabled");
		else
			return BaseUtil.setName(new ItemStack(Material.INK_SACK, 1, (short) 5), ChatColor.DARK_PURPLE + "Disabled");
	}
	
	private ItemStack getBackButton() {
		return BaseUtil.setName(new ItemStack(Material.REDSTONE_BLOCK), ChatColor.RED + "Back");
	}
	
	private ItemStack getUninstallButton() {
		if(!getPlayer().hasPermission(InstallerConst.PERM_UNINSTALL))
			return InstallerConst.getNoPermissionItem();
		if(plugin instanceof InstallerPlugin)
			return InstallerConst.getNotAvailableItem("Can't uninstall this plugin");
		return BaseUtil.setNameAndLore(new ItemStack(Material.TNT), ChatColor.RED.toString() + ChatColor.UNDERLINE + "DELETE", ChatColor.GRAY + "Press 'ctrl' and 'Q'");
	}
	
	private ItemStack getDisableButton() {
		if(!getPlayer().hasPermission(InstallerConst.PERM_DISABLE))
			return InstallerConst.getNoPermissionItem();
		if(plugin instanceof InstallerPlugin)
			return InstallerConst.getNotAvailableItem("Can't disable this plugin");
		if(enabledD)
			return BaseUtil.setNameAndLore(new ItemStack(Material.INK_SACK, 1, (short) 9), ChatColor.LIGHT_PURPLE + "Disable", ChatColor.GRAY + "Double click");
		return BaseUtil.setName(new ItemStack(Material.INK_SACK, 1, (short) 8), ChatColor.GRAY + "Already disabled");
	}
	
	private ItemStack getEnableButton() {
		if(!getPlayer().hasPermission(InstallerConst.PERM_ENABLE))
			return InstallerConst.getNoPermissionItem();
		if(!enabledD)
			return BaseUtil.setNameAndLore(new ItemStack(Material.INK_SACK, 1, (short) 10), ChatColor.GREEN + "Enable", ChatColor.GRAY + "Double click");
		return BaseUtil.setName(new ItemStack(Material.INK_SACK, 1, (short) 8), ChatColor.GRAY + "Already enabled");
	}

	@Override
	public void onClose() {
		
	}
	
}
