package me.codercloud.installer.util.tasks.menu;

import java.util.List;

import me.codercloud.installer.util.LogSet;
import me.codercloud.installer.util.LogSet.Log;
import me.codercloud.installer.util.LogSet.LogListener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class LogViewerMenuPoint extends PageMenuPoint {
	
	private LogSet logger;
	private final LogListener listener = new LogListener() {
		public void notifyChange(LogSet set) {
			if(logger != set) {
				set.removeListener(this);
				return;
			}
			update();
		}
	};
	
	public LogViewerMenuPoint(int size, String title) {
		this(size, title, null);
	}
	
	public LogViewerMenuPoint(int size, String title, LogSet logs) {
		super(size, title);
		setLogs(logs);
	}

	public ChatColor[] getNameColor() {
		return new ChatColor[]{ChatColor.DARK_GREEN, ChatColor.GOLD, ChatColor.DARK_RED};
	}
	
	public ChatColor[] getMessageColor() {
		return new ChatColor[]{ChatColor.GREEN, ChatColor.YELLOW, ChatColor.RED};
	}
	
	public Material[] getMaterials() {
		return new Material[]{Material.SUGAR, Material.SULPHUR, Material.REDSTONE};
	}
	
	public Short[] getDuriablities() {
		return new Short[]{};
	}
	
	@Override
	public final ItemStack updateItem(int slot) {
		List<Log> logs = this.logger.getLogs();
		if(slot>=0 && slot<logs.size())
			return logs.get(slot).toItemStack(getNameColor(), getMaterials(), getDuriablities(), getMessageColor());
		return null;
	}
	
	@Override
	public final int getMaxPage() {
		int i = (logger.getLogs().size()-1)/getPageSize()+1;
		if(i<=0)
			i = 1;
		return i;
	}
	
	public final LogSet getLogger() {
		return logger;
	}
	
	public final Log getLog(String name) {
		return getLogger().getLog(name);
	}
	
	public final Log getLogAtSlot(int slot) {
		List<Log> l = logger.getLogs();
		return slot>=0 && slot<l.size() ? l.get(slot) : null;
	}
	
	public final void setLogs(LogSet logger) {
		if(this.logger != null)
			this.logger.removeListener(listener);
		this.logger = logger == null ? new LogSet() : logger;
		this.logger.addListener(listener);
		update();
	}
	
}
