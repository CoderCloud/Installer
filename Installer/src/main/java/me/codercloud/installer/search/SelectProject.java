package me.codercloud.installer.search;

import java.util.Comparator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.codercloud.installer.data.PluginInformation;
import me.codercloud.installer.util.BaseUtil;
import me.codercloud.installer.util.tasks.menu.SelectOneMenuPoint;

public class SelectProject extends SelectOneMenuPoint<PluginInformation> implements Comparator<PluginInformation>  {

	private String search;
	
	public SelectProject(String search, List<PluginInformation> projects) {
		super(2, ChatColor.GREEN + "" + projects.size() + " Results for '" + search + "'");
		this.search = search;
		setObjects(projects);
		sortObjects(this);
		select(0);
	}

	public int compare(PluginInformation o1, PluginInformation o2) {
		return getScore(o2)-getScore(o1);
	}

	private int getScore(PluginInformation i) {
		int s = 0;
		
		if(i.getName().equalsIgnoreCase(search))
			s+=10;
		if(i.getName().toLowerCase().startsWith(search.toLowerCase()))
			s+=5;
		s+=(i.getFiles().size()+9)/10;
		return s;
	}
	
	@Override
	public ItemStack asItem(PluginInformation object, int slot, boolean selected) {
		if(object != null)
			return object.asItemStack(selected);
		return null;
	}

	public ItemStack getSubmitButton() {
		PluginInformation selected = getSelectedObject();
		if(selected != null)
			return BaseUtil.setNameAndLore(new ItemStack(Material.EMERALD_BLOCK), ChatColor.GREEN + "Select Project:",
					ChatColor.BOLD + "" + ChatColor.BLUE + selected.getName(),
					"Stage: " + selected.getStage()
					);
		else
			return BaseUtil.setNameAndLore(new ItemStack(Material.GLASS), ChatColor.BLUE + "Please select a file", ChatColor.GRAY.toString() + ChatColor.ITALIC + "Click to cancel");
	}
	
	@Override
	public boolean onLeftClickSubmit(boolean shift) {
		PluginInformation selected = getSelectedObject();
		if(selected != null)
			setNext(new SelectVersion(selected.getFiles()));
		else
			close();
		return false;
	}
	
	@Override
	public void onClose() {
		getPlayer().sendMessage(ChatColor.RED + "Your installation got canceled");
	}

}
