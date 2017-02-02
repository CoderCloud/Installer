package me.codercloud.installer.search;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.UnknownDependencyException;

import me.codercloud.installer.InstallerPlugin;
import me.codercloud.installer.data.PluginData;
import me.codercloud.installer.manager.PluginManager;
import me.codercloud.installer.util.BaseUtil;
import me.codercloud.installer.util.PluginUtil;
import me.codercloud.installer.util.tasks.menu.LogViewerMenuPoint;
import me.codercloud.installer.util.tasks.menu.SynchronizedMenuPoint;

public class InstallPluginFiles extends LogViewerMenuPoint implements SynchronizedMenuPoint {

	private PluginData[] files;
	private boolean started = false;
	private boolean finished = false;
	private boolean closed = false;
	private InstallerPlugin upadteInstaller = null;
	
	public InstallPluginFiles(PluginData[] files) {
		super(2, ChatColor.DARK_PURPLE + "Waiting for access...");
		this.files = files;
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
		setTitle(ChatColor.BLUE + "Installing");
		
		HashSet<String> toUnload = new HashSet<String>(); 
		
		for(PluginData f : files) {
			Plugin p = Bukkit.getPluginManager().getPlugin(f.getName());
			if(p != null)
				toUnload.add(p.getName());
		}
		
		boolean dependencyFound = true;
		while(dependencyFound) {
			dependencyFound = false;
			for(Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
				if(!toUnload.contains(plugin.getName())) {
					List<String> depends = plugin.getDescription().getDepend();
					List<String> softdepends = plugin.getDescription().getSoftDepend();
					for(String depend : depends) {
						if(toUnload.contains(depend)) {
							toUnload.add(plugin.getName());
							dependencyFound = true;
						}
					}
					for(String s : softdepends) {
						if(toUnload.contains(s)) {
							toUnload.add(plugin.getName());
							dependencyFound = true;
						}
					}
				}
			}
		}
		
		HashMap<String, File> toLoad = new HashMap<String, File>();
		
		for(String name : toUnload) {
			Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
			if(plugin != null) {
				boolean success;
				if(plugin instanceof InstallerPlugin) {
					if(PluginUtil.bufferPluginClasses(plugin)) {
						log(plugin.getName(), "Buffered required classes");
						upadteInstaller = (InstallerPlugin) plugin;
						success = PluginUtil.unloadPlugin(plugin);
					} else {
						warning(plugin.getName(), "Could not buffer required classes");
						success = false;
					}
				} else {
					success = PluginUtil.unloadPlugin(plugin);
				}
				if(!success)
					warning(plugin.getName(), "Could not unload");
				else {
					toLoad.put(name, PluginUtil.getPluginFile(plugin));
					log(plugin.getName(), "Unloaded v" + plugin.getDescription().getVersion());
				}
			}
		}
		
		for(PluginData pluginfile : this.files) {
			
			try {
				Plugin oldVersion = Bukkit.getPluginManager().getPlugin(pluginfile.getName());
				boolean enabled = oldVersion != null;
				
				if(oldVersion instanceof InstallerPlugin) {
					error(pluginfile.getName(), "Can't update when buffering or unload failed");
				}
				
				File output = toLoad.get(pluginfile.getName());
				if(output == null)
					output = PluginUtil.findFileForPlugin(InstallerPlugin.getPluginsDir(), pluginfile.getName());
				if(output == null) {
					error(pluginfile.getName(), "Could not find targetlocation");
					continue;
				}
				
				BaseUtil.writeToFile(output, pluginfile.getData());
				
				log(pluginfile.getName(), "Installed v" + pluginfile.getVersion());
				
				if(!enabled) {
					toLoad.put(pluginfile.getName(), output);
				}
			} catch (Exception e) {
				e.printStackTrace();
				error(pluginfile.getName(), "Error while installing v" + pluginfile.getVersion());
			}
		}
				
		HashMap<PluginDescriptionFile, File> fileMap = new HashMap<PluginDescriptionFile, File>();
		
		for(Entry<String, File> e : toLoad.entrySet()) {
			File f = e.getValue();
			PluginDescriptionFile d = PluginUtil.getDescriptionFile(f);
			if(d != null)
				fileMap.put(d, f);
			else
				warning(e.getKey(), "Problem while reloading");
		}
		
		PluginDescriptionFile[] descriptions = setLoadOrder(fileMap.keySet());
		ArrayList<Plugin> loaded = new ArrayList<Plugin>();
		
		for(PluginDescriptionFile d : descriptions) {
			File f = fileMap.get(d);
			if(f != null) {
				try {
					loaded.add(Bukkit.getServer().getPluginManager().loadPlugin(f));
					log(d.getName(), "Reloaded v" + d.getVersion());
				} catch (UnknownDependencyException e) {
					warning(d.getName(), "Missing dependency: " + e.getMessage());
					continue;
				} catch (Exception e) {
					e.printStackTrace();
					warning(d.getName(), "Problem while reloading");
				}
				PluginManager.updateOptions();
			}
		}
		
		for(Plugin pl : loaded) {
			try {
				Bukkit.getServer().getPluginManager().enablePlugin(pl);
				log(pl.getName(), "Enabled v" + pl.getDescription().getVersion());
			} catch (Throwable e) {
				warning(pl.getName(), "Problem while enabling");
				continue;
			}
			PluginManager.updateOptions();
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
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
	
	private PluginDescriptionFile[] setLoadOrder(Collection<PluginDescriptionFile> files) {
		
		ArrayList<PluginDescriptionFile> result = new ArrayList<PluginDescriptionFile>();
		Map<String, PluginDescriptionFile> plugins = new HashMap<String, PluginDescriptionFile>();
		
		for(PluginDescriptionFile f : files) {
			plugins.put(f.getName(), f);
		}
		
		int lenbuffer = 0;
		
		while(!plugins.isEmpty()) {
			boolean skip = lenbuffer == plugins.size();
			lenbuffer = plugins.size();
			Iterator<PluginDescriptionFile> i = plugins.values().iterator();
			while(i.hasNext()) {
				PluginDescriptionFile d = i.next();
				boolean b = true;

				if(!skip) {
					for (String s : d.getDepend())
						if (plugins.containsKey(s))
							b = false;

					for (String s : d.getSoftDepend())
						if (plugins.containsKey(s))
							b = false;
				}
				
				if(b) {
					i.remove();
					result.add(d);
					break;
				}
			}
		}
		
		
		return result.toArray(new PluginDescriptionFile[result.size()]);
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
				return BaseUtil.setName(new ItemStack(Material.EMERALD_BLOCK), ChatColor.DARK_GREEN + "Finish");
			case 1:		
				return BaseUtil.setNameAndLore(new ItemStack(Material.GOLD_BLOCK), ChatColor.GOLD + "Finish" + ChatColor.YELLOW + " with problems", ChatColor.GRAY.toString() + ChatColor.ITALIC + "Double click");
			default:
				return BaseUtil.setNameAndLore(new ItemStack(Material.REDSTONE_BLOCK), ChatColor.DARK_RED + "Finish" + ChatColor.RED + " with errors", ChatColor.GRAY.toString() + ChatColor.ITALIC + "Double click");
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
			close();
		return false;
	}
	
	@Override
	public boolean onLeftClickButton(int slot, boolean shift) {
		if(finished && getLogger().getLevel() == 0)
			close();
		return false;
	}
	
	private void log(String plugin, String msg) {
		getLogger().log(plugin, 0, msg);
	}
	
	private void warning(String plugin, String msg) {
		getLogger().log(plugin, 1, msg);
	}
	
	private void error(String plugin, String msg) {
		getLogger().log(plugin, 2, msg);
	}

	@Override
	public void onClose() {
		closed = true;
		if(started && !finished) {
			if(upadteInstaller != null && !upadteInstaller.isEnabled())
				getPlayer().sendMessage(BaseUtil.prefix(ChatColor.GOLD + "Installer was disabled for updating:\n" + ChatColor.YELLOW + "  You will be notified when the update is finished"));
			else
				getPlayer().sendMessage(BaseUtil.prefix(ChatColor.YELLOW + "You will be notified when your installations finish"));
		} if(finished)
			getPlayer().sendMessage(BaseUtil.prefix(ChatColor.GREEN + "Installation complete"));
	}


}
