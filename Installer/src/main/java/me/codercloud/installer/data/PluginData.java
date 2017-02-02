package me.codercloud.installer.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import me.codercloud.installer.util.BaseUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public class PluginData {
	private final PluginDescriptionFile pluginyml;
	private final byte[] data;
	private final String name;
	private final String version;
	
	public PluginData(File f) throws InvalidPluginException {
		this(streamFromFile(f));
	}
	
	public PluginData(InputStream in) throws InvalidPluginException {
		this(readPlugin(in));
	}
	
	public PluginData(byte[] file) throws InvalidPluginException {
		try {
			data = file;
			ZipInputStream s = new ZipInputStream(new ByteArrayInputStream(file));
			ZipEntry e;
			while ((e = s.getNextEntry()) != null && !e.getName().equals("plugin.yml"));
			
			if (e == null)
				throw new InvalidPluginException();
			
			pluginyml = new PluginDescriptionFile(s);
			
			if(pluginyml == null)
				throw new InvalidPluginException();
			
			name = pluginyml.getName();
			version = pluginyml.getVersion();
			
			if(name == null || version == null)
				throw new InvalidPluginException();
		} catch (Exception e) {
			throw new InvalidPluginException();
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getVersion() {
		return version;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public boolean isUpdate() {
		return Bukkit.getServer().getPluginManager().getPlugin(name) != null;
	}
	
	public PluginDescriptionFile getDescription() {
		return pluginyml;
	}

	public ItemStack asItemStack(boolean install) {
		Plugin current = Bukkit.getServer().getPluginManager().getPlugin(name);
		boolean update = current != null;
		ItemStack i = new ItemStack(Material.INK_SACK);
		
		String version = ChatColor.BLUE + (update ? "from v" + current.getDescription().getVersion() + " to v" + this.version : ("v" + this.version));
		String status = (install ? "Will":"Won't") + " get " + (update ? "updated":"installed") + " (Click to change)";
		
		if(install && update) {
			i.setDurability((short) 5);
			BaseUtil.setName(i, ChatColor.LIGHT_PURPLE + name);
		} else if(install) {
			i.setDurability((short) 10);
			BaseUtil.setName(i, ChatColor.GREEN + name);
		} else {
			i.setDurability((short) 1);
			BaseUtil.setName(i, ChatColor.RED + name);
		}
		
		BaseUtil.setLore(i, version, status);
		
		return i;
	}
	
	private static InputStream streamFromFile(File f) throws InvalidPluginException {
		try {
			return new FileInputStream(f);
		} catch (FileNotFoundException e) {
			throw new InvalidPluginException();
		}
	}
	
	private static byte[] readPlugin(InputStream in) throws InvalidPluginException {
		try {
			return BaseUtil.readFully(in);
		} catch (Exception e) {
			throw new InvalidPluginException();
		}
	}
	
}
