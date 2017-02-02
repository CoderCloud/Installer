package me.codercloud.installer.search;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.InvalidPluginException;

import me.codercloud.installer.data.PluginData;
import me.codercloud.installer.util.Loader;
import me.codercloud.installer.util.tasks.MenuTask.MenuPoint;

public class SearchURLPluginFile extends MenuPoint {

	private String url;
	
	public SearchURLPluginFile(String url) {
		super(0, ChatColor.BLUE + "Downloading File");
		this.url = url;
	}
	
	@Override
	public boolean run() {
		try {
			Loader l = new Loader(url, null);
			Collection<PluginData> files = parseData(l.readURLBytes());
			
			if(files.size()>0)
				setNext(new SelectPluginFile(files));
			else
				getPlayer().sendMessage(ChatColor.RED + "No plugins found");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			getPlayer().sendMessage(ChatColor.RED + "You didn't enter a valid URL");
		} catch (IOException e) {
			e.printStackTrace();
			getPlayer().sendMessage(ChatColor.RED + "Error while connecting to url");
		} catch (Exception e) {
			e.printStackTrace();
			getPlayer().sendMessage(ChatColor.RED + "Internal error! Check console for more information");
		}
		return false;
	}
	
	private Collection<PluginData> parseData(byte[] data) {
		ArrayList<PluginData> plugins = new ArrayList<PluginData>();
		try {
			plugins.add(new PluginData(data));
			return plugins;
		} catch (Exception e) {
		}
		
		try {
			ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(data));
			
			ArrayList<byte[]> files = new ArrayList<byte[]>();
			
			ZipEntry e;
			while((e = in.getNextEntry()) != null) {
				String name = e.getName();
				int index = name.lastIndexOf(".");
				if(index != -1 && name.length()>index+1 && name.substring(index+1, name.length()).equalsIgnoreCase("jar")) {
					ByteArrayOutputStream s = new ByteArrayOutputStream();
					byte[] buff = new byte[1024];
					for(int i = in.read(buff); i != -1; i = in.read(buff))
						s.write(buff, 0, i);
					files.add(s.toByteArray());
				}
			}
			
			
			for(byte[] f : files) 
				try {
					plugins.add(new PluginData(f));
				} catch (InvalidPluginException ex) {}
			
			return plugins;
		} catch (Exception e) {

		}
		
		return plugins;
	}
	
	@Override
	public ItemStack updateItem(int slot) {
		return null;
	}

	@Override
	public void onClose() {
		getPlayer().sendMessage(ChatColor.RED + "Your installation got canceled");
	}

}
