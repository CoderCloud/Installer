package me.codercloud.installer.search;

import java.io.IOException;
import java.net.URLEncoder;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import me.codercloud.installer.InstallerConst;
import me.codercloud.installer.data.PluginInformation;
import me.codercloud.installer.data.PluginSearchResults;
import me.codercloud.installer.util.tasks.MenuTask.MenuPoint;

public class LoadProject extends MenuPoint {
	
	private String link;
	private String name;
	
	public LoadProject(String link) {
		super(0, ChatColor.BLUE + "Searching Project");
		this.link = link;
	}
	
	@Override
	public boolean run() {
		if(link.startsWith("http://dev.bukkit.org/bukkit-plugins/")) {
			int offs = "http://dev.bukkit.org/bukkit-plugins/".length();
			if(link.length()<offs) {
				getPlayer().sendMessage(ChatColor.RED + "You have to post a bukkit project page:" + ChatColor.GRAY + "\ne.g. http://dev.bukkit.org/bukkit-plugins/...");
				return false;
			}
			int end = link.indexOf("/", offs);
			if(end == -1)
				end = link.length();
			this.name = link.substring(offs, end);
		} else if(link.startsWith("http://www.curse.com/bukkit-plugins/minecraft/")) {
			int offs = "http://www.curse.com/bukkit-plugins/minecraft/".length();
			if(link.length()<offs) {
				getPlayer().sendMessage(ChatColor.RED + "You have to post a bukkit project page:" + ChatColor.GRAY + "\ne.g. http://www.curse.com/bukkit-plugins/minecraft/...");
				return false;
			}
			int end = link.indexOf("/", offs);
			if(end == -1)
				end = link.length();
			this.name = link.substring(offs, end);
		} else {
			if(getPlayer().hasPermission(InstallerConst.PERM_INSTALL_UNSAFE)) {
				setNext(new SearchURLPluginFile(link));
			} else {
				getPlayer().sendMessage(ChatColor.RED + "You are not permitted to download from any URL");
			}
			return false;
		}
		
		
		try {
			PluginSearchResults r = PluginSearchResults.search(name);
			PluginInformation i = null;
			for(PluginInformation in : r.getResults())
				if(i.getSlug().equals(name)) {
					i = in;
					break;
				}
			
			if(i != null)
				setNext(new SelectVersion(i.getFiles()));
			else
				getPlayer().sendMessage(ChatColor.RED + "Project not found");
		} catch (IOException e) {
			e.printStackTrace();
			getPlayer().sendMessage(ChatColor.RED + "Could not parse recieved data");
		}
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private String getFilter() {
		JSONArray array = new JSONArray();
		JSONObject obj = new JSONObject();
		array.add(obj);
		
		obj.put("field", "slug");
		obj.put("action", "equals");
		obj.put("value", name);
		
		return array.toString();
	}
	
	private String getURL() {
		return "http://api.bukget.org/3/search/";
	}
	
	private byte[] getPostData() throws IOException {
		StringBuilder s = new StringBuilder();
		
		s.append("filters=").append(URLEncoder.encode(getFilter(), "UTF-8"));
		s.append("&fields=slug,plugin_name,stage,authors,curse_id,versions.date,versions.download");
		
		return s.toString().getBytes();
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
