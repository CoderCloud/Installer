package me.codercloud.installer.manager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import me.codercloud.installer.util.BaseUtil;
import me.codercloud.installer.util.PluginUtil;
import me.codercloud.installer.util.tasks.menu.LogViewerMenuPoint;
import me.codercloud.installer.util.tasks.menu.SynchronizedMenuPoint;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public class UninstallPlugin extends LogViewerMenuPoint implements SynchronizedMenuPoint  {
	
	private Plugin plugin;
	private boolean finished = false;
	private boolean closed = false;
	private boolean started = false;
	
	public UninstallPlugin(Plugin p) {
		super(2, ChatColor.DARK_PURPLE + "Waiting for access...");
		this.plugin = p;
	}
	
	public void preRun() {
		
	}
	
	public boolean postCancel() {
		getPlayer().sendMessage(ChatColor.RED + "Your installation got canceled");
		return false;
	}
	
	@Override
	public boolean run() {
		started = true;
		setTitle(ChatColor.RED + "Uninstalling");
		
		String name = plugin.getName();
		File file = PluginUtil.getPluginFile(plugin);
		
		if(PluginUtil.unloadPlugin(plugin)) {
			log(name, "Unloaded plugin");
			if(file != null) {
				if(file.delete()) {
					log(name, "Deleted file");
				} else {
					log(name, "File is locked by bukkit");
					
					PluginDescriptionFile pdf = PluginUtil.getDescriptionFile(file);
					
					if(pdf != null) {
						
						boolean mod = true;
						
						YamlConfiguration c = new YamlConfiguration();
						
						c.set("name", pdf.getName());
						c.set("version", "DELETED");
						c.set("main", "DELETED");
						ArrayList<String> depend = new ArrayList<String>();
						depend.add(pdf.getName());
						c.set("depend", new String[]{pdf.getName()});
						
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						ZipOutputStream zos = new ZipOutputStream(os);
						
						try {
							zos.putNextEntry(new ZipEntry("plugin.yml"));
							zos.write(c.saveToString().getBytes());
							zos.closeEntry();
							zos.close();
						} catch (Exception e) {
							error(name, "Could not modify plugin.yml");
							mod = false;
						}
						
						
						if(mod) {
							log(name, "Generated plugin.yml");
							try {
								BaseUtil.writeToFile(file, os.toByteArray());
								log(name, "Modified file");
							} catch (IOException e) {
								e.printStackTrace();
								error(name, "Could not write new data");
							}
						}
					} else {
						error(name, "Could not load plugin.yml");
					}
				}
			} else {
				error(name, "File not found");
			}
		} else {
			error(name, "Could not unload plugin");
		}
				
		finished = true;
		if(closed) {
			getPlayer().sendMessage(getLogger().toMessage(": ", ChatColor.WHITE + " -> ",
						new ChatColor[]{ChatColor.BLUE, ChatColor.LIGHT_PURPLE, ChatColor.RED},
						new ChatColor[]{ChatColor.DARK_BLUE, ChatColor.DARK_PURPLE, ChatColor.DARK_RED}));
		}
		update();
		setTitle(ChatColor.GREEN + "Installaton Finished");
				
		return true;
	}
	
	private void log(String plugin, String msg) {
		getLogger().log(plugin, 0, msg);
		update();
	}
	
	private void error(String plugin, String msg) {
		getLogger().log(plugin, 2, msg);
		update();
	}
	
	@Override
	public ItemStack getButton(int slot) {
		if(!started)
			return BaseUtil.setNameAndLore(new ItemStack(Material.WATCH), ChatColor.RED + "Stop waiting", ChatColor.GRAY .toString() + ChatColor.ITALIC + "Double click");
		else if(!finished)
			return null;
		else {
			switch (getLogger().getLevel()) {
			case 0:	
				return BaseUtil.setName(new ItemStack(Material.EMERALD_BLOCK), ChatColor.DARK_GREEN + "Back");
			case 1:		
				return BaseUtil.setNameAndLore(new ItemStack(Material.GOLD_BLOCK), ChatColor.GOLD + "Back" + ChatColor.YELLOW + " with problems", ChatColor.GRAY.toString() + ChatColor.ITALIC + "Double click");
			default:
				return BaseUtil.setNameAndLore(new ItemStack(Material.REDSTONE_BLOCK), ChatColor.DARK_RED + "Back" + ChatColor.RED + " with errors", ChatColor.GRAY.toString() + ChatColor.ITALIC + "Double click");
			}
		}
	}
	
	@Override
	public boolean onDoubleClickButton(int slot) {
		if(!started) {
			cancelSync();
			close();
		}
		if(finished && getLogger().getLevel()>0)
			setNext(new PluginManager());
		return false;
	}
	
	@Override
	public boolean onLeftClickButton(int slot, boolean shift) {
		if(finished && getLogger().getLevel() == 0)
			setNext(new PluginManager());
		return false;
	}

	@Override
	public void onClose() {
		closed = true;
		if(started && !finished)
			getPlayer().sendMessage(BaseUtil.prefix(ChatColor.YELLOW + "You will be notified when your installation finishes"));
		if(finished)
			getPlayer().sendMessage(BaseUtil.prefix(ChatColor.GREEN + "Installation complete"));
	}

}
