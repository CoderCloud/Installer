package me.codercloud.installer.search;

import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import me.codercloud.installer.data.PluginInformation;
import me.codercloud.installer.data.PluginSearchResults;
import me.codercloud.installer.util.tasks.MenuTask.MenuPoint;

public class SearchProject extends MenuPoint {

	private String search;
	
	public SearchProject(String search) {
		super(0, ChatColor.GREEN + "Searching for '" + search + "'");
		this.search = search;
	}

	public boolean run() {
		try {
			PluginSearchResults r = PluginSearchResults.search(search);
			if(r.getResults().size()!=0)
				setNext(new SelectProject(this.search, r.getResults()));
			else
				getPlayer().sendMessage(ChatColor.RED + "No projects found");
		} catch (IOException e) {
			e.printStackTrace();
			getPlayer().sendMessage(ChatColor.RED + "Could not parse recieved data");
		}
		return false;
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
