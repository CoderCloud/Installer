package me.codercloud.installer.manager;

import java.util.HashSet;
import java.util.Iterator;

import me.codercloud.installer.util.BaseUtil;
import me.codercloud.installer.util.tasks.MenuTask.MenuPoint;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class PluginManager extends MenuPoint {
	
	private static HashSet<PluginManager> opned = new HashSet<PluginManager>();
	private static Plugin[] display = new Plugin[0];
	
	public static void updateOptions() {
		display = Bukkit.getPluginManager().getPlugins();
		Iterator<PluginManager> i = opned.iterator();
		while(i.hasNext()) {
			PluginManager m = i.next();
			if(!m.isActive()) {
				i.remove();
				continue;
			}
			m.displayed = display;
			m.update();
		}
	}
	
	private int page = 1;
	private Plugin[] displayed = new Plugin[0];
	
	public PluginManager() {
		super(3, ChatColor.BLUE + "Plugin Manager");
	}
	
	@Override
	public boolean run() {
		opned.add(this);
		updateOptions();
		return super.run();
	}
	
	@Override
	public ItemStack updateItem(int slot) {
		Plugin[] show = displayed;
		int maxPage = (show.length + 17) / 18;
		if (page > maxPage)
			page = maxPage;
		if (page < 1)
			page = 1;
		
		if (slot >= 0 && slot < 18) {
			int index = (page - 1) * 18 + slot;
			return index<show.length ? toItemStack(show[index]) : null;
		} else if(slot == 21) {
			return page == 1 ? null :getPrevPageIndicator(page);
		} else if(slot == 22) {
			return getPageIndicator(page, maxPage);
		} else if(slot == 23) {
			return page == maxPage ? null : getNextPageIndicator(page);
		} else if (slot == 18) {
			return getCancelIndicator();
		}
		return null;
	}
	
	private ItemStack toItemStack(Plugin p) {
		return BaseUtil.setNameAndLore(new ItemStack(p.isEnabled() ? Material.EYE_OF_ENDER : Material.ENDER_PEARL), (p.isEnabled() ? ChatColor.DARK_GREEN : ChatColor.DARK_PURPLE) + p.getName(), "" + ChatColor.DARK_BLUE + ChatColor.ITALIC + "v" + p.getDescription().getVersion());
	}
	
	private ItemStack getPageIndicator(int page, int maxpage) {
		return BaseUtil.setName(new ItemStack(Material.NAME_TAG), ChatColor.BLUE + "Page (" + page + "/" + maxpage + ")");
	}
	
	private ItemStack getNextPageIndicator(int current) {
		return BaseUtil.setName(new ItemStack(Material.PAPER), ChatColor.BLUE + "Next Page (" + (current+1) + ")");
	}
	
	private ItemStack getPrevPageIndicator(int current) {
		return BaseUtil.setName(new ItemStack(Material.PAPER), ChatColor.BLUE + "Previous Page (" + (current-1) + ")");
	}
	
	private ItemStack getCancelIndicator() {
		return BaseUtil.setName(new ItemStack(Material.REDSTONE_BLOCK), ChatColor.RED + "Close");
	}
	
	@Override
	public boolean onLeftClick(int slot, boolean shift) {
		boolean upd = false;
		if(slot>=0&&slot<18) {
			int loc = (page-1)*18+slot;
			if(loc<displayed.length)
				setNext(new PluginOverview(displayed[loc]));
		} else if (slot == 21) {
			page--;
			upd = true;
		} else if (slot == 23) {
			page++;
			upd = true;
		} else if (slot == 18) {
			close();
		}
		return upd;
	}
	
	@Override
	public void onClose() {
		opned.remove(this);
	}
	
}
